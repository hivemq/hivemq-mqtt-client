/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client2.internal.mqtt.datatypes;

import com.hivemq.client2.internal.util.ByteArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Iterator for a topic or topic filter.
 * <p>
 * equals and hashCode match the current level.
 *
 * @author Silvio Giebl
 */
public class MqttTopicIterator extends MqttTopicLevel {

    public static @NotNull MqttTopicIterator of(final @NotNull MqttTopicImpl topic) {
        final byte[] binary = topic.toBinary();
        return new MqttTopicIterator(binary, -1, -1, binary.length);
    }

    public static @NotNull MqttTopicIterator of(final @NotNull MqttTopicFilterImpl topicFilter) {
        final byte[] binary = topicFilter.toBinary();
        final int start = topicFilter.getFilterByteStart() - 1;
        return new MqttTopicIterator(
                binary, start, start, topicFilter.containsMultiLevelWildcard() ? (binary.length - 2) : binary.length);
    }

    private int start;
    private int end;
    private final int allEnd;

    private MqttTopicIterator(final byte @NotNull [] array, final int start, final int end, final int allEnd) {
        super(array);
        this.start = start;
        this.end = end;
        this.allEnd = allEnd;
    }

    @Override
    protected int getStart() {
        return start;
    }

    @Override
    protected int getEnd() {
        return end;
    }

    public boolean hasNext() {
        return end != allEnd;
    }

    public boolean hasMultiLevelWildcard() {
        return allEnd != array.length;
    }

    public @NotNull MqttTopicIterator fork() {
        return new MqttTopicIterator(array, start, end, allEnd);
    }

    public @NotNull MqttTopicLevel next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        start = end + 1;
        end = ByteArrayUtil.indexOf(array, start, (byte) MqttTopicImpl.TOPIC_LEVEL_SEPARATOR);
        return this;
    }

    @Override
    public @NotNull MqttTopicLevel trim() {
        if (!hasNext()) {
            return MqttTopicLevel.of(array, start, end);
        }
        final int start = this.start;
        final int end = this.end;
        this.start = this.end = allEnd;
        return new MqttTopicLevels(Arrays.copyOfRange(array, start, allEnd), end - start);
    }

    public boolean forwardIfEqual(final @NotNull MqttTopicLevels levels) {
        final byte[] levelsArray = levels.getArray();
        final int levelsEnd = levels.getEnd();
        final int to = end + levelsArray.length - levelsEnd;
        if ((to <= allEnd) && ((to == allEnd) || (array[to] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR)) &&
                ByteArrayUtil.equals(array, end + 1, to, levelsArray, levelsEnd + 1, levelsArray.length)) {
            start = end = to;
            return true;
        }
        return false;
    }

    public int forwardWhileEqual(final @NotNull MqttTopicLevels levels) {
        if (!hasNext()) {
            return levels.getEnd();
        }
        int branchIndex = end;
        int levelsBranchIndex = levels.getEnd();
        int index = branchIndex + 1;
        int levelsIndex = levelsBranchIndex + 1;
        final byte[] levelsArray = levels.getArray();
        while (true) {
            final boolean isEnd = index == allEnd;
            final boolean isLevelsEnd = levelsIndex == levelsArray.length;
            if (isLevelsEnd || isEnd) {
                if ((isLevelsEnd || (levelsArray[levelsIndex] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR)) &&
                        (isEnd || (array[index] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR))) {
                    branchIndex = index;
                    levelsBranchIndex = levelsIndex;
                }
                break;
            }
            final byte lb = levelsArray[levelsIndex];
            if (array[index] == lb) {
                if (lb == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR) {
                    branchIndex = index;
                    levelsBranchIndex = levelsIndex;
                }
                index++;
                levelsIndex++;
            } else {
                break;
            }
        }
        start = end = branchIndex;
        return levelsBranchIndex;
    }

    public boolean forwardIfMatch(final @NotNull MqttTopicLevels levels) {
        if (!hasNext()) {
            return false;
        }
        int index = end + 1;
        int levelsIndex = levels.getEnd() + 1;
        final byte[] levelsArray = levels.getArray();
        while (true) {
            final boolean isEnd = index == allEnd;
            final boolean isLevelsEnd = levelsIndex == levelsArray.length;
            if (isLevelsEnd) {
                if (isEnd || (array[index] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR)) {
                    start = end = index;
                    return true;
                }
                return false;
            }
            if (isEnd) {
                return false;
            }
            final byte lb = levelsArray[levelsIndex];
            if (array[index] == lb) {
                index++;
                levelsIndex++;
            } else if (lb == MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD) {
                while ((index < allEnd) && (array[index] != MqttTopicImpl.TOPIC_LEVEL_SEPARATOR)) {
                    index++;
                }
                levelsIndex++;
            } else {
                return false;
            }
        }
    }
}

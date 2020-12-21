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

import com.hivemq.client2.internal.util.ByteArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Single topic or topic filter level. May be the single level wildcard but must not be the multi level wildcard (the
 * multi level wildcard does not represent a topic level).
 *
 * @author Silvio Giebl
 */
public class MqttTopicLevel extends ByteArray {

    private static final @NotNull MqttTopicLevel SINGLE_LEVEL_WILDCARD =
            new MqttTopicLevel(new byte[]{MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD});

    static @NotNull MqttTopicLevel of(final byte @NotNull [] array, final int start, final int end) {
        if (isSingleLevelWildcard(array, start, end)) {
            return MqttTopicLevel.SINGLE_LEVEL_WILDCARD;
        }
        return new MqttTopicLevel(Arrays.copyOfRange(array, start, end));
    }

    private static boolean isSingleLevelWildcard(final byte @NotNull [] array, final int start, final int end) {
        return ((end - start) == 1) && (array[start] == MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD);
    }

    MqttTopicLevel(final byte @NotNull [] array) {
        super(array);
    }

    byte @NotNull [] getArray() {
        return array;
    }

    public boolean isSingleLevelWildcard() {
        return isSingleLevelWildcard(array, getStart(), getEnd());
    }

    public @NotNull MqttTopicLevel trim() {
        return this;
    }

    public static @Nullable MqttTopicFilterImpl toFilter(
            final byte @Nullable [] prefix,
            final @Nullable MqttTopicLevel topicLevel,
            final boolean multiLevelWildcard) {

        int length = 0;
        if (prefix != null) {
            length += prefix.length + 1;
        }
        if (topicLevel != null) {
            length += topicLevel.array.length;
        }
        if (multiLevelWildcard) {
            if (topicLevel != null) {
                length++;
            }
            length++;
        }
        final byte[] bytes = new byte[length];
        int cursor = 0;
        if (prefix != null) {
            System.arraycopy(prefix, 0, bytes, cursor, prefix.length);
            cursor += prefix.length;
            bytes[cursor] = MqttTopicImpl.TOPIC_LEVEL_SEPARATOR;
            cursor++;
        }
        if (topicLevel != null) {
            System.arraycopy(topicLevel.array, 0, bytes, cursor, topicLevel.array.length);
            cursor += topicLevel.array.length;
        }
        if (multiLevelWildcard) {
            if (topicLevel != null) {
                bytes[cursor] = MqttTopicImpl.TOPIC_LEVEL_SEPARATOR;
                cursor++;
            }
            bytes[cursor] = MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD;
        }
        return MqttTopicFilterImpl.of(bytes);
    }
}

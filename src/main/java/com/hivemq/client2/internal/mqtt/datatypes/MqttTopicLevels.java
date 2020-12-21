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

/**
 * Multiple (more than 1) topic or topic filter levels. May contain single level wildcard(s) but must not contain the
 * multi level wildcard (the multi level wildcard does not represent a topic level).
 * <p>
 * equals and hashCode match the first level.
 *
 * @author Silvio Giebl
 */
public class MqttTopicLevels extends MqttTopicLevel {

    public static @NotNull MqttTopicLevels concat(
            final @NotNull MqttTopicLevel level1, final @NotNull MqttTopicLevel level2) {

        final byte[] array1 = level1.trim().getArray();
        final byte[] array2 = level2.trim().getArray();
        final byte[] array = new byte[array1.length + 1 + array2.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        array[array1.length] = MqttTopicImpl.TOPIC_LEVEL_SEPARATOR;
        System.arraycopy(array2, 0, array, array1.length + 1, array2.length);
        return new MqttTopicLevels(array, level1.length());
    }

    private final int firstEnd;

    MqttTopicLevels(final byte @NotNull [] array, final int firstEnd) {
        super(array);
        this.firstEnd = firstEnd;
    }

    @Override
    protected int getEnd() {
        return firstEnd;
    }

    public @NotNull MqttTopicLevel before(final int index) {
        if (index == array.length) {
            return this;
        }
        assert array[index] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR : "topic levels must only be split on /";
        if (index == firstEnd) {
            return MqttTopicLevel.of(array, 0, firstEnd);
        }
        return new MqttTopicLevels(Arrays.copyOfRange(array, 0, index), firstEnd);
    }

    public @NotNull MqttTopicLevel after(final int index) {
        assert array[index] == MqttTopicImpl.TOPIC_LEVEL_SEPARATOR : "topic levels must only be split on /";
        final int start = index + 1;
        final int end = ByteArrayUtil.indexOf(array, start, (byte) MqttTopicImpl.TOPIC_LEVEL_SEPARATOR);
        if (end == array.length) {
            return MqttTopicLevel.of(array, start, array.length);
        }
        return new MqttTopicLevels(Arrays.copyOfRange(array, start, array.length), end - start);
    }
}

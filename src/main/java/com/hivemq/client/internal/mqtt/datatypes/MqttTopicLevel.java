/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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
 *
 */

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.util.ByteArray;
import com.hivemq.client.internal.util.ByteArrayUtil;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Silvio Giebl
 */
public class MqttTopicLevel extends ByteArray.Range {

    public static final @NotNull ByteArray SINGLE_LEVEL_WILDCARD =
            new ByteArray(new byte[]{MqttTopicFilter.SINGLE_LEVEL_WILDCARD});

    public static @NotNull MqttTopicLevel root(final @NotNull MqttTopicImpl topic) {
        final byte[] binary = topic.toBinary();
        final int end = nextEnd(binary, 0);
        return new MqttTopicLevel(binary, 0, end);
    }

    public static @NotNull MqttTopicLevel root(final @NotNull MqttTopicFilterImpl topicFilter) {
        final byte[] binary = topicFilter.toBinary();
        final int start = topicFilter.getFilterByteStart();
        final int end = nextEnd(binary, start);
        return new MqttTopicLevel(binary, start, end);
    }

    private static int nextEnd(final @NotNull byte[] array, final int start) {
        final int nextSeparator = ByteArrayUtil.indexOf(array, start, (byte) MqttTopic.TOPIC_LEVEL_SEPARATOR);
        return (nextSeparator == -1) ? array.length : nextSeparator;
    }

    private MqttTopicLevel(final @NotNull byte[] array, final int start, final int end) {
        super(array, start, end);
    }

    public @Nullable MqttTopicLevel next() {
        if (end == array.length) {
            return null;
        }
        start = end + 1;
        end = nextEnd(array, start);
        return this;
    }

    public @NotNull ByteArray copy() {
        if (isSingleLevelWildcard()) {
            return SINGLE_LEVEL_WILDCARD;
        }
        return new ByteArray(Arrays.copyOfRange(array, start, end));
    }

    public @NotNull MqttTopicLevel fork() {
        return new MqttTopicLevel(array, start, end);
    }

    public boolean isSingleLevelWildcard() {
        return (length() == 1) && (array[start] == MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
    }

    public boolean isMultiLevelWildcard() {
        return (length() == 1) && (array[start] == MqttTopicFilter.MULTI_LEVEL_WILDCARD);
    }
}

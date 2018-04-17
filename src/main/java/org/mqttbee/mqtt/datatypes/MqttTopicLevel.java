/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.datatypes;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.util.ByteArray;
import org.mqttbee.util.ByteArrayUtil;

import java.util.Arrays;

/**
 * @author Silvio Giebl
 */
public class MqttTopicLevel extends ByteArray {

    public static final MqttTopicLevel SINGLE_LEVEL_WILDCARD =
            new MqttTopicLevel(new byte[]{MqttTopicFilter.SINGLE_LEVEL_WILDCARD}, 0, 1);

    @NotNull
    public static MqttTopicLevel root(@NotNull final MqttTopicImpl topic) {
        final byte[] binary = topic.toBinary();
        final int end = nextEnd(binary, 0);
        return new MqttTopicLevel(binary, 0, end);
    }

    @NotNull
    public static MqttTopicLevel root(@NotNull final MqttTopicFilterImpl topicFilter) {
        final byte[] binary = topicFilter.toBinary();
        final int start = topicFilter.getFilterByteStart();
        final int end = nextEnd(binary, start);
        return new MqttTopicLevel(binary, start, end);
    }

    private static int nextEnd(final byte[] array, final int start) {
        final int nextSeparator = ByteArrayUtil.indexOf(array, start, (byte) MqttTopic.TOPIC_LEVEL_SEPARATOR);
        return (nextSeparator == -1) ? array.length : nextSeparator;
    }

    private MqttTopicLevel(@NotNull final byte[] array, final int start, final int end) {
        super(array, start, end);
    }

    @Nullable
    public MqttTopicLevel next() {
        if (end == array.length) {
            return null;
        }
        start = end + 1;
        end = nextEnd(array, start);
        return this;
    }

    @NotNull
    public ByteArray copy() {
        final byte[] copy = Arrays.copyOfRange(array, start, end);
        return new ByteArray(copy, 0, copy.length);
    }

    @NotNull
    public MqttTopicLevel fork() {
        return new MqttTopicLevel(array, start, end);
    }

    public boolean isSingleLevelWildcard() {
        return (length() == 1) && (array[start] == MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
    }

    public boolean isMultiLevelWildcard() {
        return (length() == 1) && (array[start] == MqttTopicFilter.MULTI_LEVEL_WILDCARD);
    }

}

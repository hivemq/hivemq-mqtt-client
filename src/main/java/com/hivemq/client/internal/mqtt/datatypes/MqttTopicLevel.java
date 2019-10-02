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
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Single topic or topic filter level. May be the single level wildcard but must not be the multi level wildcard (the
 * multi level wildcard does not represent a topic level).
 *
 * @author Silvio Giebl
 */
public class MqttTopicLevel extends ByteArray {

    private static final @NotNull MqttTopicLevel SINGLE_LEVEL_WILDCARD =
            new MqttTopicLevel(new byte[]{MqttTopicFilter.SINGLE_LEVEL_WILDCARD});

    static @NotNull MqttTopicLevel of(final @NotNull byte[] array, final int start, final int end) {
        if (isSingleLevelWildcard(array, start, end)) {
            return MqttTopicLevel.SINGLE_LEVEL_WILDCARD;
        }
        return new MqttTopicLevel(Arrays.copyOfRange(array, start, end));
    }

    private static boolean isSingleLevelWildcard(final @NotNull byte[] array, final int start, final int end) {
        return ((end - start) == 1) && (array[start] == MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
    }

    MqttTopicLevel(final @NotNull byte[] array) {
        super(array);
    }

    @NotNull byte[] getArray() {
        return array;
    }

    public boolean isSingleLevelWildcard() {
        return isSingleLevelWildcard(array, getStart(), getEnd());
    }

    public @NotNull MqttTopicLevel trim() {
        return this;
    }
}

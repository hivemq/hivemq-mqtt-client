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

package com.hivemq.client2.mqtt.datatypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * MQTT Quality of Service (QoS) according to the MQTT specification.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum MqttQos {

    /**
     * QoS for at most once delivery according to the capabilities of the underlying network.
     */
    AT_MOST_ONCE,
    /**
     * QoS for ensuring at least once delivery.
     */
    AT_LEAST_ONCE,
    /**
     * QoS for ensuring exactly once delivery.
     */
    EXACTLY_ONCE;

    private static final @NotNull MqttQos @NotNull [] VALUES = values();

    /**
     * @return the byte code of this QoS.
     */
    public @Range(from = 0, to = 2) int getCode() {
        return ordinal();
    }

    /**
     * Returns the QoS belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the QoS belonging to the given byte code or null if the byte code is not a valid QoS code.
     */
    public static @Nullable MqttQos fromCode(final int code) {
        if (code < 0 || code >= VALUES.length) {
            return null;
        }
        return VALUES[code];
    }
}

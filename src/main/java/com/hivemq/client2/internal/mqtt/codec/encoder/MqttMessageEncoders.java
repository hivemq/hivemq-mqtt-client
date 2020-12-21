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

package com.hivemq.client2.internal.mqtt.codec.encoder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Collection of encoders for MQTT messages which can be queried by the MQTT message type code.
 *
 * @author Silvio Giebl
 */
public abstract class MqttMessageEncoders {

    protected final @NotNull MqttMessageEncoder<?> @NotNull [] encoders = new MqttMessageEncoder[16];

    /**
     * Returns the corresponding encoder to the given MQTT message type code.
     *
     * @param code the MQTT message type code.
     * @return the corresponding decoder to the MQTT message type code or null if there is no encoder for the MQTT
     *         message type code.
     */
    public final @Nullable MqttMessageEncoder<?> get(final int code) {
        if (code < 0 || code >= encoders.length) {
            return null;
        }
        return encoders[code];
    }
}

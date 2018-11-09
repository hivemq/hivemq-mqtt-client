/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.api.mqtt.mqtt3.message.connect.connack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CONNACK Return Code according to the MQTT 3.1.1 specification.
 */
public enum Mqtt3ConnAckReturnCode {

    SUCCESS,
    UNSUPPORTED_PROTOCOL_VERSION,
    IDENTIFIER_REJECTED,
    SERVER_UNAVAILABLE,
    BAD_USER_NAME_OR_PASSWORD,
    NOT_AUTHORIZED;

    private static final @NotNull Mqtt3ConnAckReturnCode[] VALUES = values();

    /**
     * @return the byte code of this CONNACK Return Code.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the CONNACK Return Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the CONNACK Return Code belonging to the given byte code or null if the byte code is not a valid CONNACK
     *         Return Code code.
     */
    public static @Nullable Mqtt3ConnAckReturnCode fromCode(final int code) {
        if (code < 0 || code >= VALUES.length) {
            return null;
        }
        return VALUES[code];
    }
}

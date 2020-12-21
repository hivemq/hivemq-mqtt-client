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

package com.hivemq.client2.mqtt.mqtt3.message.connect;

import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3ReturnCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Return Code of an {@link Mqtt3ConnAck MQTT 3 ConnAck message}.
 *
 * @author Daniel Krüger
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt3ConnAckReturnCode implements Mqtt3ReturnCode {

    /**
     * The connection is accepted.
     */
    SUCCESS,
    /**
     * The server does not support the version of the MQTT protocol requested by the client.
     */
    UNSUPPORTED_PROTOCOL_VERSION,
    /**
     * The client identifier is formed correctly but is not accepted by the server.
     */
    IDENTIFIER_REJECTED,
    /**
     * The MQTT service is not available.
     */
    SERVER_UNAVAILABLE,
    /**
     * The server does not accept the user name or password specified by the client.
     */
    BAD_USER_NAME_OR_PASSWORD,
    /**
     * The client is not authorized to connect.
     */
    NOT_AUTHORIZED;

    private static final @NotNull Mqtt3ConnAckReturnCode @NotNull [] VALUES = values();

    @Override
    public int getCode() {
        return ordinal();
    }

    @Override
    public boolean isError() {
        return this != SUCCESS;
    }

    /**
     * Returns the ConnAck Return Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the ConnAck Return Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid ConnAck Return Code.
     */
    public static @Nullable Mqtt3ConnAckReturnCode fromCode(final int code) {
        if (code < 0 || code >= VALUES.length) {
            return null;
        }
        return VALUES[code];
    }
}

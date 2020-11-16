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

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 * @see MqttClientIdentifier
 * @see MqttUtf8StringImpl
 */
@Unmodifiable
public class MqttClientIdentifierImpl extends MqttUtf8StringImpl implements MqttClientIdentifier {

    /**
     * Placeholder for a Client Identifier to indicate that the MQTT broker should assign the Client Identifier.
     */
    public static final @NotNull MqttClientIdentifierImpl REQUEST_CLIENT_IDENTIFIER_FROM_SERVER =
            new MqttClientIdentifierImpl(new byte[0]);
    private static final int MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES = 1;
    private static final int MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES = 23;

    /**
     * Validates and creates a Client Identifier of the given UTF-16 encoded Java string.
     *
     * @param string the Client Identifier as a UTF-16 encoded Java string.
     * @return the created Client Identifier.
     * @throws IllegalArgumentException if the given string is not a valid Client Identifier.
     */
    @Contract("null -> fail")
    public static @NotNull MqttClientIdentifierImpl of(final @Nullable String string) {
        Checks.notNull(string, "Client identifier");
        checkLength(string, "Client identifier");
        checkWellFormed(string, "Client identifier");
        return new MqttClientIdentifierImpl(string);
    }

    /**
     * Validates and creates a Client Identifier of the given byte array with UTF-8 encoded data.
     *
     * @param binary the byte array with the UTF-8 encoded data.
     * @return the created Client Identifier or <code>null</code> if the byte array does not represent a valid Client
     *         Identifier.
     */
    public static @Nullable MqttClientIdentifierImpl of(final byte @NotNull [] binary) {
        return (!MqttBinaryData.isInRange(binary) || isWellFormed(binary)) ? null :
                new MqttClientIdentifierImpl(binary);
    }

    /**
     * Validates and decodes a Client Identifier from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Client Identifier or <code>null</code> if the byte buffer does not contain a valid Client
     *         Identifier.
     */
    public static @Nullable MqttClientIdentifierImpl decode(final @NotNull ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : of(binary);
    }

    private MqttClientIdentifierImpl(final byte @NotNull [] binary) {
        super(binary);
    }

    private MqttClientIdentifierImpl(final @NotNull String string) {
        super(string);
    }

    @Override
    public boolean mustBeAllowedByServer() {
        final byte[] binary = toBinary();
        final int length = binary.length;
        if ((length < MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES) || (length > MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES)) {
            return false;
        }
        for (final byte b : binary) {
            if (!(((b >= 'a') && (b <= 'z')) || ((b >= 'A') && (b <= 'Z')) || ((b >= '0') && (b <= '9')))) {
                return false;
            }
        }
        return true;
    }
}

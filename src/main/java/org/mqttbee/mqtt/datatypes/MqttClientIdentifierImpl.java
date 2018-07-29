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

package org.mqttbee.mqtt.datatypes;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;

import javax.annotation.concurrent.Immutable;
import java.util.regex.Pattern;

/**
 * @author Silvio Giebl
 * @see MqttClientIdentifier
 * @see MqttUTF8StringImpl
 */
@Immutable
public class MqttClientIdentifierImpl extends MqttUTF8StringImpl implements MqttClientIdentifier {

    /**
     * Placeholder for a Client Identifier to indicate that the MQTT broker should assign the Client Identifier.
     */
    @NotNull
    public static final MqttClientIdentifierImpl REQUEST_CLIENT_IDENTIFIER_FROM_SERVER =
            new MqttClientIdentifierImpl(encode(""));
    private static final Pattern MUST_BE_ALLOWED_BY_SERVER_PATTERN = Pattern.compile("([0-9]|[a-z]|[A-Z])*");
    private static final int MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES = 1;
    private static final int MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES = 23;

    /**
     * Validates and decodes a Client Identifier from the given byte array.
     *
     * @param binary the byte array with the UTF-8 encoded data to decode from.
     * @return the created Client Identifier or null if the byte array does not contain a well-formed encoded Client
     * Identifier.
     */
    @Nullable
    public static MqttClientIdentifierImpl from(@NotNull final byte[] binary) {
        return containsMustNotCharacters(binary) ? null : new MqttClientIdentifierImpl(binary);
    }

    /**
     * Validates and creates a Client Identifier from the given string.
     *
     * @param string the Client Identifier as a UTF-16 encoded Java string.
     * @return the created Client Identifier or null if the given string is not a valid Client Identifier.
     */
    @Nullable
    public static MqttClientIdentifierImpl from(@NotNull final String string) {
        return containsMustNotCharacters(string) ? null : new MqttClientIdentifierImpl(string);
    }

    /**
     * Validates and decodes a Client Identifier from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the byte buffer with the UTF-8 encoded data to decode from.
     * @return the created Client Identifier or null if the byte buffer does not contain a well-formed encoded Client
     * Identifier.
     */
    @Nullable
    public static MqttClientIdentifierImpl from(@NotNull final ByteBuf byteBuf) {
        final byte[] binary = MqttBinaryData.decode(byteBuf);
        return (binary == null) ? null : from(binary);
    }


    private MqttClientIdentifierImpl(@NotNull final byte[] binary) {
        super(binary);
    }

    private MqttClientIdentifierImpl(@NotNull final String string) {
        super(string);
    }

    @Override
    public boolean mustBeAllowedByServer() {
        final byte[] binary = toBinary();
        return binary.length >= MUST_BE_ALLOWED_BY_SERVER_MIN_BYTES &&
                binary.length <= MUST_BE_ALLOWED_BY_SERVER_MAX_BYTES &&
                MUST_BE_ALLOWED_BY_SERVER_PATTERN.matcher(toString()).matches();
    }

}

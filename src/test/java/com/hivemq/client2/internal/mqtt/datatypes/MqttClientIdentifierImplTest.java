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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttClientIdentifierImplTest {

    private @Nullable MqttClientIdentifierImpl from(final @NotNull String string, final boolean fromByteBuf) {
        if (fromByteBuf) {
            final ByteBuf byteBuf = Unpooled.buffer();
            final byte[] binary = string.getBytes(StandardCharsets.UTF_8);
            byteBuf.writeShort(binary.length);
            byteBuf.writeBytes(binary);
            final MqttClientIdentifierImpl mqtt5ClientIdentifier = MqttClientIdentifierImpl.decode(byteBuf);
            byteBuf.release();
            return mqtt5ClientIdentifier;
        } else {
            return MqttClientIdentifierImpl.of(string);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void test_must_be_allowed_by_server(final boolean fromByteBuf) {
        final String string = "abc123DEF";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string, fromByteBuf);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void test_must_be_allowed_by_server_length_23(final boolean fromByteBuf) {
        final String string = "abcdefghijklmnopqrstuvw";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string, fromByteBuf);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void test_must_not_be_allowed_by_server_length_24(final boolean fromByteBuf) {
        final String string = "abcdefghijklmnopqrstuvwx";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string, fromByteBuf);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void test_must_not_be_allowed_by_server_zero_length(final boolean fromByteBuf) {
        final String string = "";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string, fromByteBuf);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void test_must_not_be_allowed_by_server_character(final boolean fromByteBuf) {
        final String string = "abc123-DEF";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string, fromByteBuf);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

}
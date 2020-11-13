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

package com.hivemq.client.internal.mqtt.codec.decoder.mqtt3;

import com.google.common.primitives.Bytes;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Krüger
 */
class Mqtt3ConnAckDecoderTest extends AbstractMqtt3DecoderTest {

    private static final @NotNull byte[] WELLFORMED_CONNACK_BEGIN = {
            //   type, flags
            0b0010_0000,
            //remaining length
            0b0000_0010
    };
    private static final @NotNull byte[] MALFORMED_CONNACK_BEGIN_WORNG_FLAGS = {
            //   type, flags
            0b0010_0100,
            //remaining length
            0b0000_0010
    };
    private static final @NotNull byte[] MALFORMED_CONNACK_BEGIN_TOO_LONG_LENGTH = {
            //   type, flags
            0b0010_0100,
            //remaining length
            0b0000_0011
    };
    private static final @NotNull byte[] ENDING_TOO_LONG_MALFORMED = {0x01};
    private static final @NotNull byte[] SESSION_PRESENT_TRUE = {0b0000_0001};
    private static final @NotNull byte[] SESSION_PRESENT_FALSE = {0b0000_0000};
    private static final @NotNull byte[] REASON_CODE_SUCCESS = {0x00};
    private static final @NotNull byte[] REASON_CODE_UNACCEPTED_PROTOCOL_VERSION = {0x01};
    private static final @NotNull byte[] REASON_CODE_IDENTIFIER_REJECTED = {0x02};
    private static final @NotNull byte[] REASON_CODE_SERVER_UNAVAILABLE = {0x03};
    private static final @NotNull byte[] REASON_CODE_SERVER_BAD_USERNAME_OR_PASSWORD = {0x04};
    private static final @NotNull byte[] REASON_CODE_NOT_AUTHORIZED = {0x05};
    private static final @NotNull byte[] REASON_CODE_BAD = {0x13};

    Mqtt3ConnAckDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt3MessageType.CONNACK.getCode()] = new Mqtt3ConnAckDecoder();
        }});
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SUCCESS(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_SUCCESS);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        assertNotNull(connAck);
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode()); // Mqtt3ConnAckReturnCode.SUCCESS
        assertEquals(sessionPresent, connAck.isSessionPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_WRONG_PROTOCOL(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_UNACCEPTED_PROTOCOL_VERSION);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        if (sessionPresent) {
            assertNull(connAck);
        } else {
            assertNotNull(connAck);
            assertEquals(Mqtt5ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION, connAck.getReasonCode());
            // Mqtt3ConnAckReturnCode.UNSUPPORTED_PROTOCOL_VERSION
            assertFalse(connAck.isSessionPresent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SERVER_IDENTFIER_REJECTED(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_IDENTIFIER_REJECTED);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        if (sessionPresent) {
            assertNull(connAck);
        } else {
            assertNotNull(connAck);
            assertEquals(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, connAck.getReasonCode());
            // Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
            assertFalse(connAck.isSessionPresent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SERVER_UNAVAILABLE(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_SERVER_UNAVAILABLE);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        if (sessionPresent) {
            assertNull(connAck);
        } else {
            assertNotNull(connAck);
            assertEquals(Mqtt5ConnAckReasonCode.SERVER_UNAVAILABLE, connAck.getReasonCode());
            // Mqtt3ConnAckReturnCode.SERVER_UNAVAILABLE
            assertFalse(connAck.isSessionPresent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_SERVER_BAD_USERNAME_OR_PASSWORD(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_SERVER_BAD_USERNAME_OR_PASSWORD);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        if (sessionPresent) {
            assertNull(connAck);
        } else {
            assertNotNull(connAck);
            assertEquals(Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD, connAck.getReasonCode());
            // Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
            assertFalse(connAck.isSessionPresent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_NOT_AUTHORIZED(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_NOT_AUTHORIZED);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        if (sessionPresent) {
            assertNull(connAck);
        } else {
            assertNotNull(connAck);
            assertEquals(Mqtt5ConnAckReasonCode.NOT_AUTHORIZED, connAck.getReasonCode());
            // Mqtt3ConnAckReturnCode.NOT_AUTHORIZED
            assertFalse(connAck.isSessionPresent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_BAD_RETURNCODE(final boolean sessionPresent) {
        final byte[] encoded =
                Bytes.concat(WELLFORMED_CONNACK_BEGIN, sessionPresent ? SESSION_PRESENT_TRUE : SESSION_PRESENT_FALSE,
                        REASON_CODE_BAD);
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        assertNull(connAck);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void decode_ERROR_CASES(final int errorcase) throws Exception {
        final byte[] encoded;
        switch (errorcase) {
            case 1:
                encoded = Bytes.concat(MALFORMED_CONNACK_BEGIN_WORNG_FLAGS, SESSION_PRESENT_FALSE, REASON_CODE_BAD);
                break;
            case 2:
                encoded = Bytes.concat(MALFORMED_CONNACK_BEGIN_TOO_LONG_LENGTH, SESSION_PRESENT_FALSE, REASON_CODE_BAD,
                        ENDING_TOO_LONG_MALFORMED);
                break;
            default:
                throw new Exception();
        }

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        assertFalse(channel.isOpen());
        assertNull(connAck);
    }
}
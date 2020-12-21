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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnAckRestrictions;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5ConnAckReasonCode;
import com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5ConnAckDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5ConnAckDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.CONNACK.getCode()] = new Mqtt5ConnAckDecoder();
        }});
    }

    @Test
    void decode_big_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0010_0000,
                //   remaining length (138)
                (byte) (128 + 10), 1,
                // variable header
                //   connack flags
                0b0000_0001,
                //   reason code (success)
                0x00,
                //   properties (134)
                (byte) (128 + 6), 1,
                //     session expiry interval
                0x11, 0, 0, 0, 10,
                //     receive maximum
                0x21, 0, 100,
                //     maximum qos
                0x24, 1,
                //     retain available
                0x25, 0,
                //     maximum packet size
                0x27, 0, 0, 0, 100,
                //     assigned client identifier
                0x12, 0, 4, 't', 'e', 's', 't',
                //     topic alias maximum
                0x22, 0, 5,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                //     wildcard subscription available
                0x28, 0,
                //     subscription identifiers available
                0x29, 1,
                //     shared subscription available
                0x2A, 0,
                //     server keep alive
                0x13, 0, 10,
                //     response information
                0x1A, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     server reference
                0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r',
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();

        assertNotNull(connAck);

        assertTrue(connAck.isSessionPresent());
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
        assertTrue(connAck.getSessionExpiryInterval().isPresent());
        assertEquals(10, connAck.getSessionExpiryInterval().getAsLong());
        assertTrue(connAck.getAssignedClientIdentifier().isPresent());
        assertEquals("test", connAck.getAssignedClientIdentifier().get().toString());
        assertTrue(connAck.getReasonString().isPresent());
        assertEquals("success", connAck.getReasonString().get().toString());
        assertTrue(connAck.getServerKeepAlive().isPresent());
        assertEquals(10, connAck.getServerKeepAlive().getAsInt());
        assertTrue(connAck.getResponseInformation().isPresent());
        assertEquals("response", connAck.getResponseInformation().get().toString());
        assertTrue(connAck.getServerReference().isPresent());
        assertEquals("server", connAck.getServerReference().get().toString());

        final ImmutableList<MqttUserPropertyImpl> userProperties = connAck.getUserProperties().asList();
        assertEquals(3, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
        assertEquals("test2", userProperties.get(2).getName().toString());
        assertEquals("value", userProperties.get(2).getValue().toString());

        final Mqtt5ConnAckRestrictions restrictions = connAck.getRestrictions();
        assertEquals(100, restrictions.getReceiveMaximum());
        assertEquals(MqttQos.AT_LEAST_ONCE, restrictions.getMaximumQos());
        assertEquals(false, restrictions.isRetainAvailable());
        assertEquals(100, restrictions.getMaximumPacketSize());
        assertEquals(5, restrictions.getTopicAliasMaximum());
        assertEquals(false, restrictions.isWildcardSubscriptionAvailable());
        assertEquals(true, restrictions.areSubscriptionIdentifiersAvailable());
        assertEquals(false, restrictions.isSharedSubscriptionAvailable());

        assertTrue(connAck.getEnhancedAuth().isPresent());
        final Mqtt5EnhancedAuth auth = connAck.getEnhancedAuth().get();
        assertEquals("GS2-KRB5", auth.getMethod().toString());
        assertTrue(auth.getData().isPresent());
        assertEquals(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}), auth.getData().get());
    }

    @Test
    void decode_minimum_packet() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(3);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0000);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();

        assertNotNull(connAck);

        assertFalse(connAck.isSessionPresent());
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
        assertFalse(connAck.getSessionExpiryInterval().isPresent());
        assertFalse(connAck.getAssignedClientIdentifier().isPresent());
        assertFalse(connAck.getReasonString().isPresent());
        assertFalse(connAck.getServerKeepAlive().isPresent());
        assertFalse(connAck.getResponseInformation().isPresent());
        assertFalse(connAck.getServerReference().isPresent());

        final ImmutableList<MqttUserPropertyImpl> userProperties = connAck.getUserProperties().asList();
        assertEquals(0, userProperties.size());

        assertEquals(MqttConnAckRestrictions.DEFAULT, connAck.getRestrictions());

        assertFalse(connAck.getEnhancedAuth().isPresent());
    }

    @Test
    void decode_not_minimum_packet() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties missing

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID, 0, 10);

        channel.writeInbound(byteBuf);
        final Mqtt5ConnAck connAck = channel.readInbound();

        assertNull(connAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_not_enough_bytes_for_fixed_header() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(128);

        channel.writeInbound(byteBuf);
        final Mqtt5ConnAck connAck = channel.readInbound();

        assertNull(connAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_wrong_flags() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0100);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(121);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(123);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);
        // padding, e.g. next message
        byteBuf.writeByte(0b0010_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_not_minimum_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(128 + 122).writeByte(0);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_too_large() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_wrong_connack_flags() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_1001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_wrong_reason_code() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code
        byteBuf.writeByte(0x10);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5ConnAckReasonCode.class)
    void decode_reason_codes(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0010_0000,
                //   remaining length
                3,
                // variable header
                //   connack flags
                0b0000_0000,
                //   reason code placeholder
                (byte) 0xFF,
                //   properties
                0
        };

        encoded[3] = (byte) reasonCode.getCode();
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5ConnAck connAck = channel.readInbound();

        assertNotNull(connAck);
        assertEquals(reasonCode, connAck.getReasonCode());

    }

    @Test
    void decode_reason_code_not_0_session_present_must_be_0() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (protocol error)
        byteBuf.writeByte(0x82);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_property_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH - 1);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_property_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH + 1);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_property_length_not_minimum_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(123);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(128 + PROPERTIES_VALID_LENGTH).writeByte(0);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_property_length_too_large() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(122);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_wrong_property() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     wrong property
        byteBuf.writeByte(127).writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_malformed_property() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(7);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(4);
        //     malformed receive maximum identifier
        byteBuf.writeByte(128 + 0x21).writeByte(0).writeByte(0).writeByte(10);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_receive_maximum_0() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(6);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(3);
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x21, 0, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_receive_maximum_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     receive maximum should be 2 bytes long
        byteBuf.writeBytes(new byte[]{0x21, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_maximum_qos_not_0_or_1() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x24, 2});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_retain_available_not_0_or_1() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x25, 2});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_maximum_packet_size_0() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(8);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(5);
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x27, 0, 0, 0, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_maximum_packet_size_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(6);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(3);
        //     maximum packet size should be 4 bytes long
        byteBuf.writeBytes(new byte[]{0x27, 0, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_wildcard_subscription_available_not_0_or_1() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     wildcard subscription available
        byteBuf.writeBytes(new byte[]{0x28, 2});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_subscription_identifiers_available_not_0_or_1() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     subscription identifiers available
        byteBuf.writeBytes(new byte[]{0x29, 2});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_shared_subscription_available_not_0_or_1() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(5);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(2);
        //     shared subscription available
        byteBuf.writeBytes(new byte[]{0x2A, 2});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_session_expiry_interval() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     session expiry interval
        byteBuf.writeBytes(new byte[]{0x11, 0, 0, 0, 10});
        //     session expiry interval
        byteBuf.writeBytes(new byte[]{0x11, 0, 0, 0, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_receive_maximum() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(9);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(6);
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x21, 0, 100});
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x21, 0, 100});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_maximum_qos() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(7);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(4);
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x24, 1});
        //     receive maximum
        byteBuf.writeBytes(new byte[]{0x24, 1});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_invalid_maximum_qos_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(4);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(1);
        //     maximum qos should be 1 byte long
        byteBuf.writeBytes(new byte[]{0x24});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_must_not_multiple_retain_available() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(7);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(4);
        //     retain available
        byteBuf.writeBytes(new byte[]{0x25, 0});
        //     retain available
        byteBuf.writeBytes(new byte[]{0x25, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_maximum_packet_size() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     maximum packet size
        byteBuf.writeBytes(new byte[]{0x27, 0, 0, 0, 100});
        //     maximum packet size
        byteBuf.writeBytes(new byte[]{0x27, 0, 0, 0, 100});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_assigned_client_identifier() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     assigned client identifier
        byteBuf.writeBytes(new byte[]{0x12, 0, 4, 't', 'e', 's', 't'});
        //     assigned client identifier
        byteBuf.writeBytes(new byte[]{0x12, 0, 4, 't', 'e', 's', 't'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_topic_alias_maximum() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(9);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(6);
        //     topic alias maximum
        byteBuf.writeBytes(new byte[]{0x22, 0, 5});
        //     topic alias maximum
        byteBuf.writeBytes(new byte[]{0x22, 0, 5});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_reason_string() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(23);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(20);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'});
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_wildcard_subscription_available() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(7);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(4);
        //     wildcard subscription available
        byteBuf.writeBytes(new byte[]{0x28, 0});
        //     wildcard subscription available
        byteBuf.writeBytes(new byte[]{0x28, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_subscription_identifiers_available() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(7);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(4);
        //     subscription identifiers available
        byteBuf.writeBytes(new byte[]{0x29, 1});
        //     subscription identifiers available
        byteBuf.writeBytes(new byte[]{0x29, 1});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_shared_subscription_available() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(7);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(4);
        //     shared subscription available
        byteBuf.writeBytes(new byte[]{0x2A, 0});
        //     shared subscription available
        byteBuf.writeBytes(new byte[]{0x2A, 0});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_server_keep_alive() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(9);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(6);
        //     server keep alive
        byteBuf.writeBytes(new byte[]{0x13, 0, 10});
        //     server keep alive
        byteBuf.writeBytes(new byte[]{0x13, 0, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_response_information() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(25);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(22);
        //     response information
        byteBuf.writeBytes(new byte[]{0x1A, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e'});
        //     response information
        byteBuf.writeBytes(new byte[]{0x1A, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_server_reference() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(21);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(18);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r'});
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_auth_method() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(25);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(22);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_auth_data() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(29);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(26);
        //     auth data
        byteBuf.writeBytes(new byte[]{0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        //     auth data
        byteBuf.writeBytes(new byte[]{0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_can_include_multiple_user_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(47);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(44);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2'});
        byteBuf.writeBytes(new byte[]{0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e'});

        channel.writeInbound(byteBuf);
        final MqttConnAck connAck = channel.readInbound();
        assertNotNull(connAck);

        final ImmutableList<MqttUserPropertyImpl> userProperties = connAck.getUserProperties().asList();
        assertEquals(3, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
        assertEquals("test2", userProperties.get(2).getName().toString());
        assertEquals("value", userProperties.get(2).getValue().toString());
    }

    @Test
    void decode_reason_string_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 6, 's', 'u', 'c', 'c', 'e', 's', 's'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_reason_string_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 8, 's', 'u', 'c', 'c', 'e', 's', 's'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_reason_string_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_assigned_client_identifier_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(10);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(7);
        //     assigned client identifier
        byteBuf.writeBytes(new byte[]{0x12, 0, 3, 't', 'e', 's', 't'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_assigned_client_identifier_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(10);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(7);
        //     assigned client identifier
        byteBuf.writeBytes(new byte[]{0x12, 0, 5, 't', 'e', 's', 't'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_assigned_client_identifier_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(10);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(7);
        //     assigned client identifier
        byteBuf.writeBytes(new byte[]{0x12, 0, 4, 't', 'e', 's', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_response_information_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     response information
        byteBuf.writeBytes(new byte[]{0x1A, 0, 7, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_response_information_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     response information
        byteBuf.writeBytes(new byte[]{0x1A, 0, 9, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_response_information_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     response information
        byteBuf.writeBytes(new byte[]{0x1A, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_server_reference_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(9);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 5, 's', 'e', 'r', 'v', 'e', 'r'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_server_reference_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(9);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 7, 's', 'e', 'r', 'v', 'e', 'r'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_server_reference_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(9);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_auth_method_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 7, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_auth_method_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 9, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_auth_method_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_name_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 3, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_name_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 5, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_name_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', '\0', 0, 5, 'v', 'a', 'l', 'u', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_value_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 4, 'v', 'a', 'l', 'u', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_value_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_value_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_auth_data_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(24);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     auth data
        byteBuf.writeBytes(new byte[]{0x16, 0, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_auth_data_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(24);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     auth data
        byteBuf.writeBytes(new byte[]{0x16, 0, 11, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_must_not_include_authentication_data_without_method() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(13);
        //     auth data
        byteBuf.writeBytes(new byte[]{0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        // padding, e.g. next message
        byteBuf.writeByte(0x00);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_defaults() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0010_0000);
        //   remaining length
        byteBuf.writeByte(3);
        // variable header
        //   connack flags
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final Mqtt5ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);

        assertEquals(0, connAck.getRestrictions().getTopicAliasMaximum());
        assertEquals(65_535, connAck.getRestrictions().getReceiveMaximum());
        assertEquals(MqttQos.EXACTLY_ONCE, connAck.getRestrictions().getMaximumQos());
        assertEquals(true, connAck.getRestrictions().isRetainAvailable());
        assertEquals(true, connAck.getRestrictions().isWildcardSubscriptionAvailable());
        assertEquals(true, connAck.getRestrictions().areSubscriptionIdentifiersAvailable());
        assertEquals(true, connAck.getRestrictions().isSharedSubscriptionAvailable());
    }

    private void testDisconnect(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final Mqtt5ConnAck connAck = channel.readInbound();
        assertNull(connAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertTrue(disconnect.getReasonString().isPresent());
    }

    private static final byte PROPERTIES_VALID_LENGTH = 119;
    private static final @NotNull byte[] PROPERTIES_VALID = {
            //     session expiry interval
            0x11, 0, 0, 0, 10,
            //     receive maximum
            0x21, 0, 100,
            //     maximum qos
            0x24, 1,
            //     retain available
            0x25, 0,
            //     maximum packet size
            0x27, 0, 0, 0, 100,
            //     assigned client identifier
            0x12, 0, 4, 't', 'e', 's', 't',
            //     topic alias maximum
            0x22, 0, 5,
            //     reason string
            0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
            //     user properties
            0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
            0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
            //     wildcard subscription available
            0x28, 0,
            //     subscription identifiers available
            0x29, 1,
            //     shared subscription available
            0x2A, 0,
            //     server keep alive
            0x13, 0, 10,
            //     response information
            0x1A, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
            //     server reference
            0x1C, 0, 6, 's', 'e', 'r', 'v', 'e', 'r',
            //     auth method
            0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
            //     auth data
            0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
    };
}

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
import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5DisconnectDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5DisconnectDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.DISCONNECT.getCode()] = new Mqtt5DisconnectDecoder();
        }});
    }

    @Test
    void decode_big_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1110_0000,
                //   remaining length (165)
                (byte) (128 + 37), 1,
                // variable header
                //   reason code (normal disconnection)
                0x00,
                //   properties (162)
                (byte) (128 + 34), 1,
                //     session expiry interval
                0x11, 0, 0, 0, 10,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 5, 't', 'e', 's', 't', '0', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '3', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '4', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '5', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '6', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '7', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '8', 0, 5, 'v', 'a', 'l', 'u', 'e',
                //     server reference
                0x1C, 0, 9, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        final MqttDisconnect disconnect = testOk(byteBuf);

        assertEquals(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION, disconnect.getReasonCode());
        assertTrue(disconnect.getSessionExpiryInterval().isPresent());
        assertEquals(10, disconnect.getSessionExpiryInterval().getAsLong());
        assertTrue(disconnect.getReasonString().isPresent());
        assertEquals("success", disconnect.getReasonString().get().toString());
        assertTrue(disconnect.getServerReference().isPresent());
        assertEquals("reference", disconnect.getServerReference().get().toString());

        final ImmutableList<MqttUserPropertyImpl> userProperties = disconnect.getUserProperties().asList();
        assertEquals(9, userProperties.size());
        for (int i = 0; i < 9; i++) {
            assertEquals("test" + i, userProperties.get(i).getName().toString());
            assertEquals("value", userProperties.get(i).getValue().toString());
        }
    }

    @Test
    void decode_minimal_packet() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(0);

        final Mqtt5Disconnect disconnect = testOk(byteBuf);

        assertEquals(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION, disconnect.getReasonCode());
        assertFalse(disconnect.getSessionExpiryInterval().isPresent());
        assertFalse(disconnect.getReasonString().isPresent());
        assertFalse(disconnect.getServerReference().isPresent());
        assertEquals(0, disconnect.getUserProperties().asList().size());
    }

    @Test
    void decode_packet_without_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(1);
        // variable header
        //   reason code (disconnect with will message)
        byteBuf.writeByte(0x04);

        final Mqtt5Disconnect disconnect = testOk(byteBuf);

        assertEquals(Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE, disconnect.getReasonCode());
        assertFalse(disconnect.getSessionExpiryInterval().isPresent());
        assertFalse(disconnect.getReasonString().isPresent());
        assertFalse(disconnect.getServerReference().isPresent());
        assertEquals(0, disconnect.getUserProperties().asList().size());
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(1);

        channel.writeInbound(byteBuf);
        final Mqtt5Disconnect disconnect = channel.readInbound();

        assertNull(disconnect);

        final Mqtt5Disconnect disconnectOut = channel.readOutbound();

        assertNull(disconnectOut);
    }

    @Test
    void decode_not_enough_bytes_for_fixed_header() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(128);

        channel.writeInbound(byteBuf);
        final Mqtt5Disconnect disconnect = channel.readInbound();

        assertNull(disconnect);

        final Mqtt5Disconnect disconnectOut = channel.readOutbound();

        assertNull(disconnectOut);
    }

    @Test
    void decode_wrong_flags() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0100);
        //   remaining length
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(72);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(74);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);
        // padding, e.g. next message
        byteBuf.writeByte(0b1110_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_not_minimum_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(128 + 73).writeByte(0);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(73);
        // variable header
        //   reason code (unsupported protocol version)
        byteBuf.writeByte(0x84);
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(73);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(73);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH + 1);
        byteBuf.writeBytes(PROPERTIES_VALID);
        // padding, e.g. next message
        byteBuf.writeByte(0b1110_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_property_length_not_minimum_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(74);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(73);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(4);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     malformed reason string identifier
        byteBuf.writeByte(128 + 0x1F).writeByte(0).writeBytes(new byte[]{0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_must_not_multiple_session_expiry_interval() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     session expiry interval
        byteBuf.writeBytes(new byte[]{0x11, 0, 0, 0, 10});
        byteBuf.writeBytes(new byte[]{0x11, 0, 0, 0, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_server_reference() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(26);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(24);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 9, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'});
        byteBuf.writeBytes(new byte[]{0x1C, 0, 9, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_must_not_multiple_reason_string() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(22);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(20);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'});
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR);
    }

    @Test
    void decode_can_multiple_user_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(30);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(28);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});

        final MqttDisconnect disconnect = testOk(byteBuf);

        assertEquals(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION, disconnect.getReasonCode());
        assertFalse(disconnect.getSessionExpiryInterval().isPresent());
        assertFalse(disconnect.getReasonString().isPresent());
        assertFalse(disconnect.getServerReference().isPresent());

        final ImmutableList<MqttUserPropertyImpl> userProperties = disconnect.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value", userProperties.get(1).getValue().toString());
    }

    @Test
    void decode_server_reference_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(12);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 8, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_server_reference_length_too_long() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(12);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 10, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1110_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_server_reference_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(12);
        //     server reference
        byteBuf.writeBytes(new byte[]{0x1C, 0, 9, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_reason_string_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 8, 's', 'u', 'c', 'c', 'e', 's', 's'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1110_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_reason_string_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_name_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 5, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1110_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_name_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   reason code (normal disconnection)
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
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1110_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_value_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0000);
        //   remaining length
        byteBuf.writeByte(16);
        // variable header
        //   reason code (normal disconnection)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    private @NotNull MqttDisconnect testOk(final @NotNull ByteBuf byteBuf) {
        channel.pipeline().remove(disconnectHandler);
        channel.writeInbound(byteBuf);
        final MqttDisconnect disconnect = channel.readInbound();
        assertNotNull(disconnect);
        return disconnect;
    }

    private void testDisconnect(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final Mqtt5Disconnect disconnectIn = channel.readInbound();
        assertNull(disconnectIn);

        final Mqtt5Disconnect disconnectOut = channel.readOutbound();
        assertNotNull(disconnectOut);
        assertEquals(reasonCode, disconnectOut.getReasonCode());
        assertTrue(disconnectOut.getReasonString().isPresent());
    }

    private final int PROPERTIES_VALID_LENGTH = 71;
    private final @NotNull byte[] PROPERTIES_VALID = {
            //     session expiry interval
            0x11, 0, 0, 0, 10,
            //     reason string
            0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
            //     user properties
            0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
            0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
            0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
            //     server reference
            0x1C, 0, 9, 'r', 'e', 'f', 'e', 'r', 'e', 'n', 'c', 'e'
    };
}

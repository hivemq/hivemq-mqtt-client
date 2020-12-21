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
import com.hivemq.client2.internal.mqtt.message.publish.MqttPubComp;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubCompReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5PubCompDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5PubCompDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.PUBCOMP.getCode()] = new Mqtt5PubCompDecoder();
        }});
    }

    @Test
    void decode_big_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0111_0000,
                //   remaining length (150)
                (byte) (128 + 22), 1,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties (145)
                (byte) (128 + 17), 1,
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
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final MqttPubComp pubComp = channel.readInbound();

        assertNotNull(pubComp);

        assertEquals(5, pubComp.getPacketIdentifier());
        assertEquals(Mqtt5PubCompReasonCode.SUCCESS, pubComp.getReasonCode());
        assertTrue(pubComp.getReasonString().isPresent());
        assertEquals("success", pubComp.getReasonString().get().toString());

        final ImmutableList<MqttUserPropertyImpl> userProperties = pubComp.getUserProperties().asList();
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);

        channel.writeInbound(byteBuf);
        final MqttPubComp pubComp = channel.readInbound();

        assertNotNull(pubComp);

        assertEquals(5, pubComp.getPacketIdentifier());
        assertEquals(Mqtt5PubCompReasonCode.SUCCESS, pubComp.getReasonCode());
        assertFalse(pubComp.getReasonString().isPresent());
        assertEquals(0, pubComp.getUserProperties().asList().size());
    }

    @Test
    void decode_packet_without_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(3);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (packet identifier not found)
        byteBuf.writeByte(0x92);

        channel.writeInbound(byteBuf);
        final MqttPubComp pubComp = channel.readInbound();

        assertNotNull(pubComp);

        assertEquals(5, pubComp.getPacketIdentifier());
        assertEquals(Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND, pubComp.getReasonCode());
        assertFalse(pubComp.getReasonString().isPresent());
        assertEquals(0, pubComp.getUserProperties().asList().size());
    }

    @Test
    void decode_not_minimum_packet() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(1);
        // variable header
        //   packet identifier
        byteBuf.writeByte(5);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final MqttPubComp pubComp = channel.readInbound();

        assertNull(pubComp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_not_enough_bytes_for_fixed_header() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(128);

        channel.writeInbound(byteBuf);
        final MqttPubComp pubComp = channel.readInbound();

        assertNull(pubComp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_wrong_flags() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0100);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(57);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(59);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH);
        byteBuf.writeBytes(PROPERTIES_VALID);
        // padding, e.g. next message
        byteBuf.writeByte(0b0111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_remaining_length_not_minimum_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(128 + 58).writeByte(0);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(58);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (malformed packet)
        byteBuf.writeByte(0x81);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(58);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(58);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(PROPERTIES_VALID_LENGTH + 1);
        byteBuf.writeBytes(PROPERTIES_VALID);
        // padding, e.g. next message
        byteBuf.writeByte(0b0111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_property_length_not_minimum_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(59);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(58);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(6);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(15);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(11);
        //     malformed reason string identifier
        byteBuf.writeByte(128 + 0x1F).writeByte(0).writeBytes(new byte[]{0, 7, 's', 'u', 'c', 'c', 'e', 's', 's'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_must_not_multiple_reason_string() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(24);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(32);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(28);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});

        channel.writeInbound(byteBuf);
        final MqttPubComp pubComp = channel.readInbound();

        assertNotNull(pubComp);

        assertEquals(5, pubComp.getPacketIdentifier());
        assertEquals(Mqtt5PubCompReasonCode.SUCCESS, pubComp.getReasonCode());
        assertFalse(pubComp.getReasonString().isPresent());

        final ImmutableList<MqttUserPropertyImpl> userProperties = pubComp.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value", userProperties.get(1).getValue().toString());
    }

    @Test
    void decode_reason_string_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(10);
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 8, 's', 'u', 'c', 'c', 'e', 's', 's'});
        // padding, e.g. next message
        byteBuf.writeByte(0b0111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_reason_string_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
    void decode_user_property_name_length_too_short() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(18);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(18);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 5, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b0111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_name_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(18);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(18);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
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
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(18);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b0111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    @Test
    void decode_user_property_value_must_not_character() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0111_0000);
        //   remaining length
        byteBuf.writeByte(18);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(14);
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET);
    }

    private void testDisconnect(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        final MqttPubComp pubComp = channel.readInbound();
        assertNull(pubComp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertTrue(disconnect.getReasonString().isPresent());
    }

    private static final int PROPERTIES_VALID_LENGTH = 54;
    private static final @NotNull byte[] PROPERTIES_VALID = {
            //     reason string
            0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
            //     user properties
            0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
            0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
            0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e'
    };
}

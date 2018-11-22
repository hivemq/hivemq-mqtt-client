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

package org.mqttbee.mqtt.codec.decoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.netty.ChannelAttributes;
import org.mqttbee.util.collections.ImmutableList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5PubAckDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5PubAckDecoderTest() {
        super(new MqttMessageDecoders() {{
            decoders[Mqtt5MessageType.PUBACK.getCode()] = new Mqtt5PubAckDecoder();
        }});
    }

    @Test
    void decode_big_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0100_0000,
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
        final MqttPubAck pubAck = channel.readInbound();

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(Mqtt5PubAckReasonCode.SUCCESS, pubAck.getReasonCode());
        assertTrue(pubAck.getReasonString().isPresent());
        assertEquals("success", pubAck.getReasonString().get().toString());

        final ImmutableList<MqttUserPropertyImpl> userProperties = pubAck.getUserProperties().asList();
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
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);

        channel.writeInbound(byteBuf);
        final MqttPubAck pubAck = channel.readInbound();

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(Mqtt5PubAckReasonCode.SUCCESS, pubAck.getReasonCode());
        assertFalse(pubAck.getReasonString().isPresent());
        assertEquals(0, pubAck.getUserProperties().asList().size());
    }

    @Test
    void decode_packet_without_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(3);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);
        //   reason code (no matching subscribers)
        byteBuf.writeByte(0x10);

        channel.writeInbound(byteBuf);
        final MqttPubAck pubAck = channel.readInbound();

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, pubAck.getReasonCode());
        assertFalse(pubAck.getReasonString().isPresent());
        assertEquals(0, pubAck.getUserProperties().asList().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_not_minimum_packet(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(1);
        // variable header
        //   packet identifier
        byteBuf.writeByte(5);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final MqttPubAck pubAck = channel.readInbound();

        assertNull(pubAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_not_enough_bytes_for_fixed_header() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
        //   remaining length
        byteBuf.writeByte(128);

        channel.writeInbound(byteBuf);
        final MqttPubAck pubAck = channel.readInbound();

        assertNull(pubAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_flags(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0100);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   packet identifier
        byteBuf.writeByte(0).writeByte(5);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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
        byteBuf.writeByte(0b0100_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_not_minimum_bytes(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_too_large(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_reason_code(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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
        byteBuf.writeByte(0b0100_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_not_minimum_bytes(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_too_large(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_property(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_malformed_property(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_reason_string(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @Test
    void decode_can_multiple_user_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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
        final MqttPubAck pubAck = channel.readInbound();

        assertNotNull(pubAck);

        assertEquals(5, pubAck.getPacketIdentifier());
        assertEquals(Mqtt5PubAckReasonCode.SUCCESS, pubAck.getReasonCode());
        assertFalse(pubAck.getReasonString().isPresent());

        final ImmutableList<MqttUserPropertyImpl> userProperties = pubAck.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value", userProperties.get(1).getValue().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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
        byteBuf.writeByte(0b0100_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_must_not_character(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_name_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_name_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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
        byteBuf.writeByte(0b0100_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_name_must_not_character(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_value_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_value_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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
        byteBuf.writeByte(0b0100_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_value_must_not_character(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b0100_0000);
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    private void testDisconnect(final @NotNull Mqtt5DisconnectReasonCode reasonCode, final boolean sendReasonString) {
        final MqttPubAck pubAck = channel.readInbound();
        assertNull(pubAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertEquals(sendReasonString, disconnect.getReasonString().isPresent());
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

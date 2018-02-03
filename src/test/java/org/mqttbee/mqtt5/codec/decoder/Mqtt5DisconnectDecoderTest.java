package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5DisconnectDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5DisconnectTestMessageDecoders()));
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.close();
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
        channel.writeInbound(byteBuf);
        final Mqtt5DisconnectImpl disconnect = channel.readInbound();

        assertNotNull(disconnect);

        assertEquals(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION, disconnect.getReasonCode());
        assertTrue(disconnect.getSessionExpiryInterval().isPresent());
        assertEquals(10, (long) disconnect.getSessionExpiryInterval().get());
        assertTrue(disconnect.getReasonString().isPresent());
        assertEquals("success", disconnect.getReasonString().get().toString());
        assertTrue(disconnect.getServerReference().isPresent());
        assertEquals("reference", disconnect.getServerReference().get().toString());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = disconnect.getUserProperties().asList();
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

        channel.writeInbound(byteBuf);
        final Mqtt5Disconnect disconnect = channel.readInbound();

        assertNotNull(disconnect);

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

        channel.writeInbound(byteBuf);
        final Mqtt5Disconnect disconnect = channel.readInbound();

        assertNotNull(disconnect);

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

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_flags(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1110_0100);
        //   remaining length
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_not_minimum_bytes(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_too_large(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_reason_code(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_not_minimum_bytes(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_property_length_too_large(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_property(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_malformed_property(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_session_expiry_interval(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_server_reference(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_reason_string(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
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

        channel.writeInbound(byteBuf);
        final Mqtt5DisconnectImpl disconnect = channel.readInbound();

        assertNotNull(disconnect);

        assertEquals(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION, disconnect.getReasonCode());
        assertFalse(disconnect.getSessionExpiryInterval().isPresent());
        assertFalse(disconnect.getReasonString().isPresent());
        assertFalse(disconnect.getServerReference().isPresent());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = disconnect.getUserProperties().asList();
        assertEquals(2, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value", userProperties.get(1).getValue().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_server_reference_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_server_reference_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_server_reference_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_name_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_name_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_name_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_value_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_value_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_user_property_value_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    private void testDisconnect(final Mqtt5DisconnectReasonCode reasonCode, final boolean sendReasonString) {
        final Mqtt5Disconnect disconnectIn = channel.readInbound();
        assertNull(disconnectIn);

        final Mqtt5Disconnect disconnectOut = channel.readOutbound();
        assertNotNull(disconnectOut);
        assertEquals(reasonCode, disconnectOut.getReasonCode());
        assertEquals(sendReasonString, disconnectOut.getReasonString().isPresent());
    }

    private final int PROPERTIES_VALID_LENGTH = 71;
    private final byte[] PROPERTIES_VALID = {
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

    private static class Mqtt5DisconnectTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.DISCONNECT.getCode()) {
                return new Mqtt5DisconnectDecoder();
            }
            return null;
        }
    }

}
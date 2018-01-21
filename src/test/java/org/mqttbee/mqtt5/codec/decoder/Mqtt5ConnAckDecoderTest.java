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
import org.mqttbee.api.mqtt5.message.Mqtt5ConnAck;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.Mqtt5ExtendedAuth;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5ConnAckDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5ConnAckTestMessageDecoders()));
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
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
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
        final Mqtt5ConnAck connAck = channel.readInbound();

        assertNotNull(connAck);

        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
        assertTrue(connAck.getSessionExpiryInterval().isPresent());
        assertEquals(10, (long) connAck.getSessionExpiryInterval().get());
        assertTrue(connAck.getAssignedClientIdentifier().isPresent());
        assertEquals("test", connAck.getAssignedClientIdentifier().get().toString());
        assertTrue(connAck.getReasonString().isPresent());
        assertEquals("success", connAck.getReasonString().get().toString());
        assertTrue(connAck.getServerKeepAlive().isPresent());
        assertEquals(10, (long) connAck.getServerKeepAlive().get());
        assertTrue(connAck.getResponseInformation().isPresent());
        assertEquals("response", connAck.getResponseInformation().get().toString());
        assertTrue(connAck.getServerReference().isPresent());
        assertEquals("server", connAck.getServerReference().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = connAck.getUserProperties();
        assertEquals(3, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String test2 = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(test2);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test2, value)));

        final Mqtt5ConnAck.Restrictions restrictions = connAck.getRestrictions();
        assertEquals(100, restrictions.getReceiveMaximum());
        assertEquals(Mqtt5QoS.AT_LEAST_ONCE, restrictions.getMaximumQoS());
        assertEquals(false, restrictions.isRetainAvailable());
        assertEquals(100, restrictions.getMaximumPacketSize());
        assertEquals(5, restrictions.getTopicAliasMaximum());
        assertEquals(false, restrictions.isWildcardSubscriptionAvailable());
        assertEquals(true, restrictions.isSubscriptionIdentifierAvailable());
        assertEquals(false, restrictions.isSharedSubscriptionAvailable());

        assertTrue(connAck.getExtendedAuth().isPresent());
        final Mqtt5ExtendedAuth auth = connAck.getExtendedAuth().get();
        assertEquals("GS2-KRB5", auth.getMethod().toString());
        assertTrue(auth.getData().isPresent());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, auth.getData().get());
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
        byteBuf.writeByte(0b0000_0001);
        //   reason code (success)
        byteBuf.writeByte(0x00);
        //   properties
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final Mqtt5ConnAck connAck = channel.readInbound();

        assertNotNull(connAck);

        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
        assertFalse(connAck.getSessionExpiryInterval().isPresent());
        assertFalse(connAck.getAssignedClientIdentifier().isPresent());
        assertFalse(connAck.getReasonString().isPresent());
        assertFalse(connAck.getServerKeepAlive().isPresent());
        assertFalse(connAck.getResponseInformation().isPresent());
        assertFalse(connAck.getServerReference().isPresent());

        final ImmutableList<Mqtt5UserProperty> userProperties = connAck.getUserProperties();
        assertEquals(0, userProperties.size());

        assertEquals(Mqtt5ConnAckImpl.RestrictionsImpl.DEFAULT, connAck.getRestrictions());

        assertFalse(connAck.getExtendedAuth().isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_not_minimum_packet(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
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

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_wrong_flags(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_remaining_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_remaining_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_remaining_length_not_minimum_bytes(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_remaining_length_too_large(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_wrong_connack_flags(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_wrong_reason_code(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_reason_code_not_0_session_present_must_be_0(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_property_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_property_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_property_length_not_minimum_bytes(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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
        byteBuf.writeByte(128 + PROPERTIES_VALID_LENGTH).writeByte(0);
        byteBuf.writeBytes(PROPERTIES_VALID);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_property_length_too_large(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_wrong_property(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_malformed_property(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_receive_maximum_0(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_maximum_qos_not_0_or_1(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_retain_available_not_0_or_1(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_maximum_packet_size_0(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_wildcard_subscription_available_not_0_or_1(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_subscription_identifiers_available_not_0_or_1(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_shared_subscription_available_not_0_or_1(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_session_expiry_interval(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_receive_maximum(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_maximum_qos(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_retain_available(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_maximum_packet_size(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_assigned_client_identifier(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_topic_alias_maximum(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_reason_string(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multipe_wildcard_subscription_available(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_subscription_identifiers_available(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_shared_subscription_available(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_server_keep_alive(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_response_information(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_server_reference(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_auth_method(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_multiple_auth_data(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
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
        final Mqtt5ConnAck connAck = channel.readInbound();
        assertNotNull(connAck);

        final ImmutableList<Mqtt5UserProperty> userProperties = connAck.getUserProperties();
        assertEquals(3, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String test2 = Mqtt5UTF8String.from("test2");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(test2);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test2, value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_reason_string_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_reason_string_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_reason_string_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_assigned_client_identifier_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_assigned_client_identifier_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_assigned_client_identifier_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_response_information_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_response_information_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_response_information_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_server_reference_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_server_reference_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_server_reference_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_auth_method_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_auth_method_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_auth_method_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_user_property_name_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_user_property_name_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_user_property_name_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_user_property_value_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_user_property_value_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_user_property_value_must_not_character(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_auth_data_length_too_short(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_auth_data_length_too_long(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decode_must_not_include_authentication_data_without_method(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);
        
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

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
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
        assertEquals(Mqtt5QoS.EXACTLY_ONCE, connAck.getRestrictions().getMaximumQoS());
        assertEquals(true, connAck.getRestrictions().isRetainAvailable());
        assertEquals(true, connAck.getRestrictions().isWildcardSubscriptionAvailable());
        assertEquals(true, connAck.getRestrictions().isSubscriptionIdentifierAvailable());
        assertEquals(true, connAck.getRestrictions().isSharedSubscriptionAvailable());
    }

    private void testDisconnect(final Mqtt5DisconnectReasonCode reasonCode, final boolean sendReasonString) {
        final Mqtt5ConnAck connAck = channel.readInbound();
        assertNull(connAck);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertEquals(sendReasonString, disconnect.getReasonString().isPresent());
    }

    private static final byte PROPERTIES_VALID_LENGTH = 119;
    private static final byte[] PROPERTIES_VALID = {
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
            0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
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

    private static class Mqtt5ConnAckTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.CONNACK.getCode()) {
                return new Mqtt5ConnAckDecoder();
            }
            return null;
        }
    }

}
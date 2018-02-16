package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5Auth;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.netty.ChannelAttributes;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5AuthDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5AuthDecoderTest() {
        super(code -> {
            if (code == Mqtt5MessageType.AUTH.getCode()) {
                return new Mqtt5AuthDecoder();
            }
            return null;
        });
    }

    @Test
    void decode_big_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length (132)
                (byte) (128 + 4), 1,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties (129)
                (byte) (128 + 1), 1,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5',
                //     auth data
                0x16, 0, 60, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                //     reason string
                0x1F, 0, 8, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2', //
                0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e',
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5AuthImpl auth = channel.readInbound();

        assertNotNull(auth);

        assertEquals(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, auth.getReasonCode());
        assertEquals("GS2-KRB5", auth.getMethod().toString());
        assertTrue(auth.getData().isPresent());
        assertEquals(ByteBuffer.wrap(new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4,
                5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        }), auth.getData().get());
        assertTrue(auth.getReasonString().isPresent());
        assertEquals("continue", auth.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = auth.getUserProperties().asList();
        assertEquals(3, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
        assertEquals("test2", userProperties.get(2).getName().toString());
        assertEquals("value", userProperties.get(2).getValue().toString());
    }

    @Test
    void decode_minimum_packet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1111_0000,
                //   remaining length
                13,
                // variable header
                //   reason code (continue)
                0x18,
                //   properties
                11,
                //     auth method
                0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5AuthImpl auth = channel.readInbound();

        assertNotNull(auth);

        assertEquals(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, auth.getReasonCode());
        assertEquals("GS2-KRB5", auth.getMethod().toString());
        assertFalse(auth.getData().isPresent());
        assertFalse(auth.getReasonString().isPresent());

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = auth.getUserProperties().asList();
        assertEquals(0, userProperties.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_not_minimum_packet(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(1);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   missing properties

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B'});

        channel.writeInbound(byteBuf);
        final Mqtt5Auth auth = channel.readInbound();

        assertNull(auth);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @Test
    void decode_not_enough_bytes_for_fixed_header() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(128);

        channel.writeInbound(byteBuf);
        final Mqtt5Auth auth = channel.readInbound();

        assertNull(auth);

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
        byteBuf.writeByte(0b1111_0010);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(128 + 13).writeByte(0);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code
        byteBuf.writeByte(0x10);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(10);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(12);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(14);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(128 + 11).writeByte(0);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(12);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(15);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(13);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(17);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(15);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     malformed auth data identifier
        byteBuf.writeByte(128 + 0x16).writeByte(0).writeByte(0).writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_omit_auth_method(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(2);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_auth_method(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(24);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(22);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_auth_data(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(39);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(37);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     auth data
        byteBuf.writeBytes(new byte[]{0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        byteBuf.writeBytes(new byte[]{0x16, 0, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_must_not_multiple_reason_string(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(35);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(33);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 8, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e'});
        byteBuf.writeBytes(new byte[]{0x1F, 0, 8, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, sendReasonString);
    }

    @Test
    void decode_can_multiple_user_properties() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(57);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(55);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2'});
        byteBuf.writeBytes(new byte[]{0x26, 0, 5, 't', 'e', 's', 't', '2', 0, 5, 'v', 'a', 'l', 'u', 'e'});

        channel.writeInbound(byteBuf);
        final Mqtt5AuthImpl auth = channel.readInbound();
        assertNotNull(auth);

        final ImmutableList<Mqtt5UserPropertyImpl> userProperties = auth.getUserProperties().asList();
        assertEquals(3, userProperties.size());
        assertEquals("test", userProperties.get(0).getName().toString());
        assertEquals("value", userProperties.get(0).getValue().toString());
        assertEquals("test", userProperties.get(1).getName().toString());
        assertEquals("value2", userProperties.get(1).getValue().toString());
        assertEquals("test2", userProperties.get(2).getName().toString());
        assertEquals("value", userProperties.get(2).getValue().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_auth_method_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 7, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_auth_method_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 9, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_auth_method_must_not_character(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(13);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(11);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_auth_data_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(26);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(24);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     auth method
        byteBuf.writeBytes(new byte[]{0x16, 0, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_auth_data_length_too_long(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(26);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(24);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     auth method
        byteBuf.writeBytes(new byte[]{0x16, 0, 11, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_reason_string_length_too_short(final boolean sendReasonString) {
        ChannelAttributes.sendReasonString(sendReasonString, channel);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(24);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(22);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 7, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(24);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(22);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 9, 'c', 'o', 'n', 't', 'i', 'n', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(24);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(22);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     reason string
        byteBuf.writeBytes(new byte[]{0x1F, 0, 8, 'c', 'o', 'n', 't', 'i', 'n', 'u', '\0'});

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(25);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(25);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 5, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(25);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(25);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(25);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e'});
        // padding, e.g. next message
        byteBuf.writeByte(0b1111_0000);

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
        byteBuf.writeByte(0b1111_0000);
        //   remaining length
        byteBuf.writeByte(27);
        // variable header
        //   reason code (continue)
        byteBuf.writeByte(0x18);
        //   properties
        byteBuf.writeByte(25);
        //     auth method
        byteBuf.writeBytes(new byte[]{0x15, 0, 8, 'G', 'S', '2', '-', 'K', 'R', 'B', '5'});
        //     user properties
        byteBuf.writeBytes(new byte[]{0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', '\0'});

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    private void testDisconnect(final Mqtt5DisconnectReasonCode reasonCode, final boolean sendReasonString) {
        final Mqtt5Auth auth = channel.readInbound();
        assertNull(auth);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertEquals(sendReasonString, disconnect.getReasonString().isPresent());
    }

}
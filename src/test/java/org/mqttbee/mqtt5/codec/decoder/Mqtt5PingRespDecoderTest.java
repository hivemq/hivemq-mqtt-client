package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.Mqtt5PingResp;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5PingRespDecoderTest extends AbstractMqtt5DecoderTest {

    Mqtt5PingRespDecoderTest() {
        super(code -> {
            if (code == Mqtt5MessageType.PINGRESP.getCode()) {
                return new Mqtt5PingRespDecoder();
            }
            return null;
        });
    }

    @Test
    void decode_valid() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0000);
        //   remaining length
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);
        final Mqtt5PingResp pingResp = channel.readInbound();

        assertNotNull(pingResp);
    }

    @Test
    void decode_not_enough_bytes() {
        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0000);

        channel.writeInbound(byteBuf);
        final Mqtt5PingResp pingResp = channel.readInbound();

        assertNull(pingResp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();

        assertNull(disconnect);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_wrong_flags(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0100);
        //   remaining length
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true"})
    void decode_remaining_length_not_0(final boolean sendReasonString) {
        channel.attr(ChannelAttributes.SEND_REASON_STRING).set(sendReasonString);

        final ByteBuf byteBuf = channel.alloc().buffer();
        // fixed header
        //   type, flags
        byteBuf.writeByte(0b1101_0000);
        //   remaining length
        byteBuf.writeByte(1);
        byteBuf.writeByte(0);

        channel.writeInbound(byteBuf);

        testDisconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, sendReasonString);
    }

    private void testDisconnect(final Mqtt5DisconnectReasonCode reasonCode, final boolean sendReasonString) {
        final Mqtt5PingResp pingResp = channel.readInbound();
        assertNull(pingResp);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());
        assertEquals(sendReasonString, disconnect.getReasonString().isPresent());
    }

}
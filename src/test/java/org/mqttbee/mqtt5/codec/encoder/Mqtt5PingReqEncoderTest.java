package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author David Katz
 */
class Mqtt5PingReqEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5PingReqEncoderTest() {
        super(true);
    }

    @Test
    void encode_pingreq() {
        final byte[] expected = {
                // fixed header
                //   type, reserved
                (byte) 0b1100_0000,
                //   remaining length
                0
        };

        final Mqtt5PingReqImpl pingReq = Mqtt5PingReqImpl.INSTANCE;
        channel.writeOutbound(pingReq);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }
}

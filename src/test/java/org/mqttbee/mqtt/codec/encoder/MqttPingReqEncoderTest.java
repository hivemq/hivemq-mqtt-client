package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.message.ping.MqttPingReq;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author David Katz
 */
class MqttPingReqEncoderTest extends AbstractMqtt5EncoderTest {

    MqttPingReqEncoderTest() {
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

        final MqttPingReq pingReq = MqttPingReq.INSTANCE;
        channel.writeOutbound(pingReq);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

}

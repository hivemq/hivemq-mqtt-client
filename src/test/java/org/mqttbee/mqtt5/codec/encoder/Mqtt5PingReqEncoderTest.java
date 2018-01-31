package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author David Katz
 */
class Mqtt5PingReqEncoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Encoder());
    }

    @AfterEach
    void tearDown() {
        channel.close();
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

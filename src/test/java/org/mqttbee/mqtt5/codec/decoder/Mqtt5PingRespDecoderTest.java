package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PingResp;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingRespDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PingRespTestMessageDecoders()));
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public void test_example() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                (byte) 0b1101_0000,
                //   remaining length
                0
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5PingResp pingResp = channel.readInbound();

        assertNotNull(pingResp);
    }

    private static class Mqtt5PingRespTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PINGRESP.getCode()) {
                return new Mqtt5PingRespDecoder();
            }
            return null;
        }
    }

}
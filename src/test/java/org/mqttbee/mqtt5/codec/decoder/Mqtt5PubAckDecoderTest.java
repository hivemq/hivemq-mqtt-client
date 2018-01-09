package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubAck;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PubAckTestMessageDecoders()));
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
                0b0100_0000,
                //   remaining length
                0,
                // variable header
                //   properties
                0
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5PubAck pubAck = channel.readInbound();

        assertNotNull(pubAck);
    }

    private static class Mqtt5PubAckTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PUBACK.getCode()) {
                return new Mqtt5PubAckDecoder();
            }
            return null;
        }
    }

}
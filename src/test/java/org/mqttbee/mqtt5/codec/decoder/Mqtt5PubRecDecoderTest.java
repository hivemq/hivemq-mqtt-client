package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRec;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PubRecTestMessageDecoders()));
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
                0b0101_0000,
                //   remaining length
                0,
                // variable header
                //   properties
                0
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5PubRec pubRec = channel.readInbound();

        assertNotNull(pubRec);
    }

    private static class Mqtt5PubRecTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PUBREC.getCode()) {
                return new Mqtt5PubRecDecoder();
            }
            return null;
        }
    }

}
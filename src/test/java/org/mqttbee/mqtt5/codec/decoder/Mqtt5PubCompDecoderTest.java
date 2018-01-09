package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubComp;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PubCompTestMessageDecoders()));
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
                0b0111_0000,
                //   remaining length
                43,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code (success)
                0x00,
                //   properties
                39,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5PubCompInternal pubCompInternal = channel.readInbound();

        assertNotNull(pubCompInternal);

        assertEquals(5, pubCompInternal.getPacketIdentifier());

        final Mqtt5PubComp pubComp = pubCompInternal.getPubComp();

        assertEquals(Mqtt5PubCompReasonCode.SUCCESS, pubComp.getReasonCode());
        assertTrue(pubComp.getReasonString().isPresent());
        assertEquals("success", pubComp.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = pubComp.getUserProperties();
        assertEquals(2, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));
    }

    private static class Mqtt5PubCompTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PUBCOMP.getCode()) {
                return new Mqtt5PubCompDecoder();
            }
            return null;
        }
    }

}
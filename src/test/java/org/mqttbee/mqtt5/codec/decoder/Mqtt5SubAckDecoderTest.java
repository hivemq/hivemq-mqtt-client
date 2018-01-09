package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5SubAck;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckImpl;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckInternal;
import org.mqttbee.mqtt5.message.suback.Mqtt5SubAckReasonCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5SubAckTestMessageDecoders()));
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
                (byte) 0b1001_0000,
                //   remaining length
                45,
                // variable header
                //   packet identifier
                0, 3,
                //   properties
                39,
                //     reason string
                0x1F, 0, 7, 's', 'u', 'c', 'c', 'e', 's', 's',
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                // payload
                0x00, 0x02, 0x01
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5SubAckInternal subAckInternal = channel.readInbound();

        assertNotNull(subAckInternal);

        assertEquals(3, subAckInternal.getPacketIdentifier());

        final Mqtt5SubAckImpl subAck = subAckInternal.getSubAck();

        assertTrue(subAck.getReasonString().isPresent());
        assertEquals("success", subAck.getReasonString().get().toString());

        final ImmutableList<Mqtt5UserProperty> userProperties = subAck.getUserProperties();
        assertEquals(2, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));

        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        assertEquals(3, reasonCodes.size());
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_0, reasonCodes.get(0));
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_2, reasonCodes.get(1));
        assertEquals(Mqtt5SubAckReasonCode.GRANTED_QOS_1, reasonCodes.get(2));
    }

    private static class Mqtt5SubAckTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.SUBACK.getCode()) {
                return new Mqtt5SubAckDecoder();
            }
            return null;
        }
    }

}
package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PublishTestMessageDecoders()));
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
                0b0011_0011,
                //   remaining length
                89,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                69,
                //     payload format indicator
                0x01, 0,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     topic alias
                0x23, 0, 3,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e',
                0x26, 0, 4, 't', 'e', 's', 't', 0, 6, 'v', 'a', 'l', 'u', 'e', '2',
                //     subscription identifier
                0x0B, 123,
                0x0B, 121,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',
                // payload
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);
        final Mqtt5PublishInternal publishInternal = channel.readInbound();

        assertNotNull(publishInternal);

        assertEquals(false, publishInternal.isDup());
        assertEquals(12, publishInternal.getPacketIdentifier());

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(2, subscriptionIdentifiers.length());
        assertTrue(subscriptionIdentifiers.contains(123));
        assertTrue(subscriptionIdentifiers.contains(121));

        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        assertNotNull(publish);

        assertEquals("topic", publish.getTopic().toString());
        assertEquals(Mqtt5QoS.AT_LEAST_ONCE, publish.getQos());
        assertEquals(true, publish.isRetain());
        assertEquals(10, publish.getMessageExpiryInterval());
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UNSPECIFIED, publish.getPayloadFormatIndicator().get());
        assertTrue(publish.getContentType().isPresent());
        assertEquals("text", publish.getContentType().get().toString());
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("response", publish.getResponseTopic().get().toString());
        assertTrue(publish.getCorrelationData().isPresent());
        assertArrayEquals(new byte[]{5, 4, 3, 2, 1}, publish.getCorrelationData().get());

        final ImmutableList<Mqtt5UserProperty> userProperties = publish.getUserProperties();
        assertEquals(2, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        final Mqtt5UTF8String value2 = Mqtt5UTF8String.from("value2");
        assertNotNull(test);
        assertNotNull(value);
        assertNotNull(value2);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value2)));

        assertTrue(publish.getPayload().isPresent());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, publish.getPayload().get());
    }

    private static class Mqtt5PublishTestMessageDecoders implements Mqtt5MessageDecoders {
        @Nullable
        @Override
        public Mqtt5MessageDecoder get(final int code) {
            if (code == Mqtt5MessageType.PUBLISH.getCode()) {
                return new Mqtt5PublishDecoder();
            }
            return null;
        }
    }

}
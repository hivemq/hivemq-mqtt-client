package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
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

    @Test
    public void test_example() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0011,
                //   remaining length
                20,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                0,
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
        assertTrue(publishInternal.getSubscriptionIdentifiers().isEmpty());

        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        assertNotNull(publish);

        assertEquals("topic", publish.getTopic().toString());
        assertEquals(Mqtt5QoS.AT_LEAST_ONCE, publish.getQos());
        assertTrue(publish.getPayload().isPresent());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, publish.getPayload().get());
        assertEquals(true, publish.isRetain());
        assertEquals(Mqtt5PublishImpl.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY, publish.getMessageExpiryInterval());
        assertFalse(publish.getPayloadFormatIndicator().isPresent());
        assertFalse(publish.getContentType().isPresent());
        assertFalse(publish.getResponseTopic().isPresent());
        assertFalse(publish.getCorrelationData().isPresent());
        assertTrue(publish.getUserProperties().isEmpty());
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
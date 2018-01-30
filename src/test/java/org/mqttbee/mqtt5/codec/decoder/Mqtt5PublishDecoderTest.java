package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.message.*;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.*;

/**
 * @author Silvio Giebl
 * @author David Katz
 */
public class Mqtt5PublishDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        createChannel();
    }

    @After
    public void tearDown() {
        channel.close();
    }

    @Test
    public void decode_allProperties() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0011,
                //   remaining length
                72,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                52,
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
                //     subscription identifier
                0x0B, 123,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',
                // payload
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);

        assertNotNull(publishInternal);

        assertEquals(false, publishInternal.isDup());
        assertEquals(12, publishInternal.getPacketIdentifier());
        assertEquals(3, publishInternal.getTopicAlias());

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(1, subscriptionIdentifiers.length());
        assertTrue(subscriptionIdentifiers.contains(123));

        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        assertNotNull(publish);

        assertEquals("topic", publish.getTopic().toString());
        assertEquals(Mqtt5QoS.AT_LEAST_ONCE, publish.getQos());
        assertEquals(true, publish.isRetain());
        assertTrue(publish.getMessageExpiryInterval().isPresent());
        assertEquals(10, (long) publish.getMessageExpiryInterval().get());
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UNSPECIFIED, publish.getPayloadFormatIndicator().get());
        assertTrue(publish.getContentType().isPresent());
        assertEquals("text", publish.getContentType().get().toString());
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("response", publish.getResponseTopic().get().toString());
        assertTrue(publish.getCorrelationData().isPresent());
        assertArrayEquals(new byte[]{5, 4, 3, 2, 1}, publish.getCorrelationData().get());
        assertEquals(Mqtt5Publish.TopicAliasUsage.HAS, publish.getTopicAliasUsage());

        final ImmutableList<Mqtt5UserProperty> userProperties = publish.getUserProperties();
        assertEquals(1, userProperties.size());
        final Mqtt5UTF8String test = Mqtt5UTF8String.from("test");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        assertNotNull(test);
        assertNotNull(value);
        assertTrue(userProperties.contains(new Mqtt5UserProperty(test, value)));

        assertTrue(publish.getPayload().isPresent());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, publish.getPayload().get());
    }

    @Test
    public void decode_simple() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                20,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 0,
                // payload
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };

        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);

        assertEquals(false, publishInternal.isDup());

        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(0, subscriptionIdentifiers.length());

        final Mqtt5PublishImpl publish = publishInternal.getPublish();

        assertNotNull(publish);

        assertEquals("topic", publish.getTopic().toString());
        assertEquals(Mqtt5QoS.AT_MOST_ONCE, publish.getQos());
        assertEquals(true, publish.isRetain());
        assertFalse(publish.getMessageExpiryInterval().isPresent());
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UNSPECIFIED, publish.getPayloadFormatIndicator().get());
        assertEquals(Mqtt5Publish.TopicAliasUsage.HAS_NOT, publish.getTopicAliasUsage());

        assertTrue(publish.getPayload().isPresent());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, publish.getPayload().get());
    }

    @Test
    public void decode_minimal() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                4,
                // variable header
                //   topic name
                0, 1, 't',
                // properties
                0
        };
        final Mqtt5PublishImpl publish = decode(encoded);
        assertEquals("t", publish.getTopic().toString());
    }

    @Test
    public void decode_fixedHeaderQos() {
        final byte[] encodedQos0 = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishImpl decodeQos0 = decode(encodedQos0);
        assertEquals(Mqtt5QoS.AT_MOST_ONCE, decodeQos0.getQos());

        final byte[] encodedQos1 = {
                // fixed header
                //   type, flags
                0b0011_0011,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishImpl decodeQos1 = decode(encodedQos1);
        assertEquals(Mqtt5QoS.AT_LEAST_ONCE, decodeQos1.getQos());

        final byte[] encodedQos2 = {
                // fixed header
                //   type, flags
                0b0011_1101,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishImpl decodeQos2 = decode(encodedQos2);
        assertEquals(Mqtt5QoS.EXACTLY_ONCE, decodeQos2.getQos());
    }

    @Test
    public void decode_fixedHeaderQosInvalid_returnsNull() {
        final byte[] encodedQosInvalid = {
                // fixed header
                //   type, flags
                0b0011_0111,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encodedQosInvalid, MALFORMED_PACKET);
    }

    @Test
    public void decode_dupTrueForQos0_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_1000,
                //   remaining length
                8,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    public void decode_topicNameInvalidStringLength_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                3,
                // variable header
                //   topic name
                0, 5, 't'
        };
        decodeNok(encoded, TOPIC_NAME_INVALID);
    }

    @Test
    public void decode_messageExpiryInterval() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                16,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                7,
                //     payload format indicator
                0x01, 1,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                // payload
                0x00
        };
        final Mqtt5PublishImpl publish = decode(encoded);
        assertTrue(publish.getMessageExpiryInterval().isPresent());
        assertEquals(10, (long) publish.getMessageExpiryInterval().get());
    }

    @Test
    public void decode_messageExpiryIntervalDuplicate_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                12,
                //     payload format indicator
                0x01, 1,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                // payload
                0x00
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }


    @Test
    public void decode_payloadFormatIndicatorUtf8() throws UnsupportedEncodingException {
        channel.attr(ChannelAttributes.VALIDATE_PAYLOAD_FORMAT).set(true);
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                17,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 1,
                // payload
                (byte) 0xE4, (byte) 0xBD, (byte) 0xA0, 0x20, (byte) 0xE5, (byte) 0xA5, (byte) 0xBD
        };

        final Mqtt5PublishImpl publish = decode(encoded);
        assertTrue(publish.getPayloadFormatIndicator().isPresent());
        assertEquals(Mqtt5PayloadFormatIndicator.UTF_8, publish.getPayloadFormatIndicator().get());
        assertTrue(publish.getPayload().isPresent());
        assertEquals("你 好", new String(publish.getPayload().get(), StandardCharsets.UTF_8.name()));
    }

    @Test
    public void decode_invalidPayloadFormatIndicator_returnsNull() {
        channel.attr(ChannelAttributes.VALIDATE_PAYLOAD_FORMAT).set(true);
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 3
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_payloadFormatIndicatorMoreThanOnce_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                4,
                //     payload format indicator
                0x01, 0,
                //     payload format indicator
                0x01, 0,
                // payload
                0x00
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    public void decode_PayloadUtf8NotWellFormed_returnsNull() {
        channel.attr(ChannelAttributes.VALIDATE_PAYLOAD_FORMAT).set(true);
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0001,
                //   remaining length
                11,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 1,
                // payload
                (byte) 0xFF
        };

        decodeNok(encoded, PAYLOAD_FORMAT_INVALID);
    }

    @Test
    public void decode_contentType() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                15,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                7,
                //     content type
                0x03, 0, 4, 't', 'e', 'x', 't',

        };
        final Mqtt5PublishImpl publish = decode(encoded);
        assertTrue(publish.getContentType().isPresent());
        assertEquals("text", publish.getContentType().get().toString());
    }

    @Test
    public void decode_invalidContentType_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                11,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                3,
                //     content type
                0x03, 0, 4
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_userProperty() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                22,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                14,
                //     user properties
                0x26, 0, 4, 't', 'e', 's', 't', 0, 5, 'v', 'a', 'l', 'u', 'e'

        };
        final Mqtt5PublishImpl publish = decode(encoded);
        final ImmutableList<Mqtt5UserProperty> userProperties = publish.getUserProperties();
        assertEquals(1, userProperties.size());
        final Mqtt5UserProperty userProperty = new Mqtt5UserProperty(requireNonNull(Mqtt5UTF8String.from("test")),
                requireNonNull(Mqtt5UTF8String.from("value")));
        assertTrue(userProperties.contains(userProperty));
    }

    @Test
    public void decode_invalidUserProperty_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                11,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                3,
                //     user properties
                0x26, 0, 4

        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_subscriptionIdentifier() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                15,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier
                0x0B, 123,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);
        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(1, subscriptionIdentifiers.length());
        assertTrue(subscriptionIdentifiers.contains(123));
    }

    @Test
    public void decode_subscriptionIdentifierZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                15,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier
                0x0B, 0,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    public void decode_subscriptionIdentifierNegative_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                26,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                18,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier negative (invalid)
                0x0B, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_subscriptionIdentifierMax() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                26,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                18,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier max = 268435455
                0x0B, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);
        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(1, subscriptionIdentifiers.length());
        assertTrue(subscriptionIdentifiers.contains(268435455));
    }

    @Test
    public void decode_subscriptionIdentifierMultiple() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                26,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                18,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     subscription identifier 128
                0x0B, (byte) 0x80, (byte) 0x01,
                //     subscription identifier 128
                0x0B, 16,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);
        final ImmutableIntArray subscriptionIdentifiers = publishInternal.getSubscriptionIdentifiers();
        assertEquals(2, subscriptionIdentifiers.length());
        assertTrue(subscriptionIdentifiers.contains(128));
        assertTrue(subscriptionIdentifiers.contains(16));
    }

    @Test
    public void decode_packetIdentifierWithQos0isNotSet() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);
        assertEquals(-1, publishInternal.getPacketIdentifier());
    }

    @Test
    public void decode_packetIdentifierWithQos1() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);
        assertEquals(12, publishInternal.getPacketIdentifier());
    }

    @Test
    public void decode_packetIdentifierWithQos2() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0100,
                //   remaining length
                23,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   packet identifier
                0, 12,
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encoded);
        assertEquals(12, publishInternal.getPacketIdentifier());
    }

    @Test
    public void decode_correlationData() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                29,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                21,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishImpl publish = decode(encoded);
        assertTrue(publish.getCorrelationData().isPresent());
        assertArrayEquals(new byte[]{5, 4, 3, 2, 1}, publish.getCorrelationData().get());
    }

    @Test
    public void decode_correlationDataMoreThanOnce_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                37,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                29,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     correlation data
                0x09, 0, 5, 5, 4, 3, 2, 1,
                //     correlation data
                0x09, 0, 1, 2, 3, 4, 5, 6,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    public void decode_responseTopic() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishImpl publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("response", publish.getResponseTopic().get().toString());
    }

    @Test
    public void decode_responseTopicMoreThanOnce_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                32,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                24,
                //     response topic
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e', //
                0x08, 0, 8, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e',
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    public void decode_responseTopicWithWildcards_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                21,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                13,
                //     response topic
                0x08, 0, 8, 'r', 't', 'o', 'p', 'i', 'c', '/', 'a',
                //     payload format indicator
                0x01, 0
        };
        Mqtt5PublishImpl publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("rtopic/a", publish.getResponseTopic().get().toString());

        encoded[20] = '#';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[20] = 'b';
        publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("rtopic/b", publish.getResponseTopic().get().toString());

        encoded[20] = '+';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[20] = 'c';
        publish = decode(encoded);
        assertTrue(publish.getResponseTopic().isPresent());
        assertEquals("rtopic/c", publish.getResponseTopic().get().toString());
    }

    @Test
    public void decode_topicAlias() {
        final byte[] encodedWithTopicName = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encodedWithTopicName);
        assertEquals(3, publishInternal.getTopicAlias());

        final byte[] encodedWithTopicAliasOnly = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                8,
                // variable header
                //   topic name is empty
                0, 0,
                //   properties
                5,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternalTopicAliasOnly = decodeInternal(encodedWithTopicAliasOnly);
        assertEquals(3, publishInternalTopicAliasOnly.getTopicAlias());
    }

    @Test
    public void decode_topicAliasDuplicate_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                16,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                8,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0,
                //     topic alias duplicate (error!)
                0x23, 0, 3
        };
        decodeNok(encoded, PROTOCOL_ERROR);
    }

    @Test
    public void decode_noTopicAliasFound_returnsNull() {
        final byte[] encodedWithTopicName = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias
                0x23, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishInternal publishInternal = decodeInternal(encodedWithTopicName);
        assertEquals(3, publishInternal.getTopicAlias());

        final byte[] encodedWithWrongTopicAlias = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                8,
                // variable header
                //   topic name is empty
                0, 0,
                //   properties
                5,
                //     topic alias
                0x23, 0, 1,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encodedWithWrongTopicAlias, TOPIC_ALIAS_INVALID);
    }

    @Test
    public void decode_topicAliasZero_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias
                0x23, 0, 0,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, TOPIC_ALIAS_INVALID);
    }


    @Test
    public void decode_topicAliasTooLarge_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     topic alias too large
                0x23, 0, 4,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, TOPIC_ALIAS_INVALID);
    }

    @Test
    public void decode_topicWithWildcard_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 7, 't', 'o', 'p', 'i', 'c', '/', 'a',
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        Mqtt5PublishImpl decode = decode(encoded);
        assertEquals("topic/a", decode.getTopic().toString());

        encoded[10] = '#';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[10] = 'b';
        decode = decode(encoded);
        assertEquals("topic/b", decode.getTopic().toString());

        encoded[10] = '+';
        decodeNok(encoded, TOPIC_NAME_INVALID);

        encoded[10] = 'c';
        decode = decode(encoded);
        assertEquals("topic/c", decode.getTopic().toString());
    }

    @Test
    public void decode_negativePropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     invalid negative identifier
                (byte) 0xFF, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_invalidPropertyIdentifier_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                5,
                //     invalid identifier
                (byte) 0x05, 0, 3,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_propertyLengthNegative_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                8,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties length negative
                -1
        };
        decodeNok(encoded, MALFORMED_PACKET);
    }

    @Test
    public void decode_topicNullNoAlias_returnsNull() {
        final byte[] encoded = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                5,
                // variable header
                //   topic name null, no alias
                0, 0,
                //   properties length
                2,
                //     payload format indicator
                0x01, 0
        };
        decodeNok(encoded, TOPIC_ALIAS_INVALID);
    }

    @NotNull
    private Mqtt5PublishImpl decode(final byte[] encoded) {
        return decodeInternal(encoded).getPublish();
    }

    @NotNull
    private Mqtt5PublishInternal decodeInternal(final byte[] encoded) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final Mqtt5PublishInternal publishInternal = channel.readInbound();
        assertNotNull(publishInternal);

        return publishInternal;
    }

    private void decodeNok(final byte[] encoded, final Mqtt5DisconnectReasonCode reasonCode) {
        final ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(encoded);
        channel.writeInbound(byteBuf);

        final Mqtt5PublishInternal publishInternal = channel.readInbound();
        assertNull(publishInternal);

        final Mqtt5Disconnect disconnect = channel.readOutbound();
        assertNotNull(disconnect);
        assertEquals(reasonCode, disconnect.getReasonCode());

        createChannel();
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

    private void createChannel() {
        channel = new EmbeddedChannel(new Mqtt5Decoder(new Mqtt5PublishTestMessageDecoders()));
        channel.attr(ChannelAttributes.INCOMING_TOPIC_ALIAS_MAPPING).set(new Mqtt5Topic[3]);
    }

}

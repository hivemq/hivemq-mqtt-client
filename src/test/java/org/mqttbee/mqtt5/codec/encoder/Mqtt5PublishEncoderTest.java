package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PublishEncoder.Mqtt5WrappedPublishEncoder;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mqttbee.api.mqtt5.message.publish.Mqtt5Publish.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.api.mqtt5.message.publish.TopicAliasUsage.*;
import static org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;
import static org.mqttbee.mqtt5.message.publish.Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author David Katz
 */
class Mqtt5PublishEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5PublishEncoderTest() {
        super(true);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                15,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 0,
                // payload
                1, 2, 3, 4, 5
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), new byte[]{1, 2, 3, 4, 5},
                        Mqtt5QoS.AT_MOST_ONCE, false, Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY,
                        Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5WrappedPublishEncoder.PROVIDER);

        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_retainTrue() {
        final byte[] expected = {
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
                0x01, 0
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_MOST_ONCE, true,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());
    }

    @Test
    void encode_retainFalse() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_1010,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                // Packet Identifier
                0, 15,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, true, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_isDupFalse() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                // Packet Identifier
                0, 15,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_isDupTrue() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_1010,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                // Packet Identifier
                0, 17,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 17, true, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_formatIndicatorUtf8() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                // Packet Identifier
                0, 15,
                //   properties
                2,
                //     payload format indicator
                0x01, 1
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_expiryInterval() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                17,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                7,
                //     message expiry interval
                0x02, 0, 0, 0x3, (byte) 0xE8,
                //     payload format indicator
                0x01, 1
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        1000, Mqtt5PayloadFormatIndicator.UTF_8, null, null, null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_contentType() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                28,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                18,
                //     payload format indicator
                0x01, 1,
                //     message expiry interval
                0x03, 0, 13, 'm', 'y', 'C', 'o', 'n', 't', 'e', 'n', 't', 'T', 'y', 'p', 'e'
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8,
                        Mqtt5UTF8StringImpl.from("myContentType"), null, null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_responseTopic() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                28,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                18,
                //     payload format indicator
                0x01, 1,
                //     response topic
                0x08, 0, 13, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e', 'T', 'o', 'p', 'i', 'c'
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                        Mqtt5TopicImpl.from("responseTopic"), null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }


    @Test
    void encode_correlationData() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                20,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                10,
                //     payload format indicator
                0x01, 1,
                //     correlation data
                0x09, 0, 5, 1, 2, 3, 4, 5
        };

        final byte[] correlationData = {1, 2, 3, 4, 5};
        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                        null, correlationData, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    @Disabled("correlation data will be validated in the builder, remove this test")
    void encode_correlationDataTooLong_throwsEncoderException() {
        final byte[] correlationData = new byte[65536];
        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                        null, correlationData, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);

        final Mqtt5PublishInternal publishInternal =
                publish.wrap(-1, false, Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("binary data size exceeded for correlation data"));

    }

    @Test
    void encode_newTopicAlias() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                13,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                3,
                //     topic alias
                0x23, 0, 8
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, MAY,
                        NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, 8, true, ImmutableIntArray.of());
    }

    @Test
    void encode_withoutTopicAlias() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                0
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, MUST_NOT,
                        NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, ImmutableIntArray.of());
    }

    @Test
    void encode_withoutTopicAliasUsingDefault() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_1010,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 2,
                //   properties
                0
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null,
                        DEFAULT_TOPIC_ALIAS_USAGE, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 2, true, ImmutableIntArray.of());
    }

    @Test
    void encode_existingTopicAlias() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                8,
                // variable header
                //   topic name
                0, 0,
                //   Packet Identifier
                0, 15,
                //   properties
                3,
                //     topic alias
                0x23, 0, 8
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, MAY,
                        NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, 8, false, ImmutableIntArray.of());
    }

    @Test
    void encode_userProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                40,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                30,
                //     payload format indicator
                0x01, 1,
                //     user properties
                0x26, 0, 3, 'k', 'e', 'y', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 4, 'k', 'e', 'y', '2', 0, 6, 'v', 'a', 'l', 'u', 'e', '2'
        };

        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.of(ImmutableList
                .of(new Mqtt5UserPropertyImpl(requireNonNull(Mqtt5UTF8StringImpl.from("key")),
                                requireNonNull(Mqtt5UTF8StringImpl.from("value"))),
                        new Mqtt5UserPropertyImpl(requireNonNull(Mqtt5UTF8StringImpl.from("key2")),
                                requireNonNull(Mqtt5UTF8StringImpl.from("value2")))));

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                        null, null, HAS_NOT, userProperties, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_singleSubscriptionIdentifier() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                2,
                // subscription identifier
                0x0b, 3
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, HAS_NOT,
                        NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of(3));
    }

    @Test
    void encode_multipleSubscriptionIdentifiers() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                14,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                4,
                // subscription identifier
                0x0b, 3,
                // subscription identifier
                0x0b, 4
        };

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, HAS_NOT,
                        NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of(3, 4));
    }

    @Test
    void encode_qos() {
        final byte[] expectedQos0 = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length
                10,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };

        final byte[] expectedQos1 = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                // Packet Identifier
                0, 7,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };

        final byte[] expectedQos2 = {
                // fixed header
                //   type, flags
                0b0011_0100,
                //   remaining length
                12,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                // Packet Identifier
                0, 7,
                //   properties
                2,
                //     payload format indicator
                0x01, 0
        };
        final Mqtt5PublishImpl publishQos0 =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_MOST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expectedQos0, publishQos0, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());

        final Mqtt5PublishImpl publishQos1 =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_LEAST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expectedQos1, publishQos1, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());

        final Mqtt5PublishImpl publishQos2 =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.EXACTLY_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);
        encode(expectedQos2, publishQos2, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_maximumOutgoingPacketSizeExceeded_throwsEncoderException() {
        createServerData(100);
        final byte[] correlationData = new byte[100];
        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_MOST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, correlationData, HAS_NOT, NO_USER_PROPERTIES, Mqtt5WrappedPublishEncoder.PROVIDER);


        final Mqtt5PublishInternal publishInternal =
                publish.wrap(-1, false, Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("Mqtt5MaximumPacketSizeExceededException"));
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_MOST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, maxPacket.getCorrelationData(1), HAS_NOT, maxPacket.getMaxPossibleUserProperties(),
                        Mqtt5WrappedPublishEncoder.PROVIDER);


        final Mqtt5PublishInternal publishInternal =
                publish.wrap(-1, false, Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final Mqtt5PublishImpl publish =
                new Mqtt5PublishImpl(requireNonNull(Mqtt5TopicImpl.from("topic")), null, Mqtt5QoS.AT_MOST_ONCE, false,
                        Mqtt5PublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        null, null, maxPacket.getCorrelationData(), HAS_NOT, maxPacket.getMaxPossibleUserProperties(1),
                        Mqtt5WrappedPublishEncoder.PROVIDER);


        final Mqtt5PublishInternal publishInternal =
                publish.wrap(-1, false, Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encode(
            final byte[] expected, final Mqtt5PublishImpl publish, final int packetIdentifier, final boolean isDup,
            final ImmutableIntArray subscriptionIdentifiers) {
        final Mqtt5PublishInternal publishInternal =
                new Mqtt5PublishInternal(publish, packetIdentifier, isDup, channel, subscriptionIdentifiers);
        encodeInternal(expected, publishInternal);
    }

    private void encode(
            final byte[] expected, final Mqtt5PublishImpl publish, final int packetIdentifier, final boolean isDup,
            final int topicAlias, final boolean isNewTopicAlias, final ImmutableIntArray subscriptionIdentifiers) {
        final Mqtt5PublishInternal publishInternal =
                publish.wrap(packetIdentifier, isDup, topicAlias, isNewTopicAlias, subscriptionIdentifiers);
        encodeInternal(expected, publishInternal);
    }

    private void encodeInternal(final byte[] expected, final Mqtt5PublishInternal publishInternal) {
        channel.writeOutbound(publishInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder;
        final Mqtt5UTF8StringImpl user = requireNonNull(Mqtt5UTF8StringImpl.from("user"));
        final Mqtt5UTF8StringImpl property = requireNonNull(Mqtt5UTF8StringImpl.from("property"));
        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        // byte array initialized to 0's, used to pad max packet.
        private byte[] correlationData;

        MaximumPacketBuilder build() {
            final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, dup, qos, retain
                    - 4  // remaining length
                    - 7  // topic name 'topic'
                    - 4 // property length
                    - 2  // payload format
                    - 4; // correlation data id, 2 byte length and 1 byte data

            final int userPropertyBytes = 1 // identifier
                    + 2 // key length
                    + 4 // bytes to encode "user"
                    + 2 // value length
                    + 8; // bytes to encode "property"
            final int extraCorrelationDataBytes = maxPropertyLength % userPropertyBytes;

            correlationData = new byte[1 + extraCorrelationDataBytes];

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            final Mqtt5UserPropertyImpl userProperty = new Mqtt5UserPropertyImpl(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        byte[] getCorrelationData() {
            return getCorrelationData(0);
        }

        byte[] getCorrelationData(final int extraBytes) {
            return Arrays.copyOf(correlationData, correlationData.length + extraBytes);
        }

        Mqtt5UserPropertiesImpl getMaxPossibleUserProperties() {
            //return ImmutableList.of();
            return getMaxPossibleUserProperties(0);
        }

        Mqtt5UserPropertiesImpl getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new Mqtt5UserPropertyImpl(user, property));
            }
            return Mqtt5UserPropertiesImpl.of(userPropertiesBuilder.build());
        }
    }

}

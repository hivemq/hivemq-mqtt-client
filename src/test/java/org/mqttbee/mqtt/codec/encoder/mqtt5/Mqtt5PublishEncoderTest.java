package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish.DEFAULT_TOPIC_ALIAS_USAGE;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage.*;
import static org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl.NO_USER_PROPERTIES;
import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author David Katz
 */
class Mqtt5PublishEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5PublishEncoderTest() {
        super(true);
    }

    @Test
    void encode_allProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0000,
                //   remaining length (151)
                (byte) (128 + 23), 1,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   properties (137)
                (byte) (128 + 9), 1,
                //     message expiry interval
                0x02, 0, 0, 0, 10,
                //     payload format indicator
                0x01, 0,
                //     content type
                0x03, 0, 13, 'm', 'y', 'C', 'o', 'n', 't', 'e', 'n', 't', 'T', 'y', 'p', 'e',
                //     response topic
                0x08, 0, 13, 'r', 'e', 's', 'p', 'o', 'n', 's', 'e', 'T', 'o', 'p', 'i', 'c',
                //     correlation data
                0x09, 0, 5, 1, 2, 3, 4, 5,
                //     user properties
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                0x26, 0, 5, 't', 'e', 's', 't', '1', 0, 5, 'v', 'a', 'l', 'u', 'e', //
                // payload
                1, 2, 3, 4, 5
        };

        final MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("test1")),
                        requireNonNull(MqttUTF8StringImpl.from("value")));
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(
                ImmutableList.of(userProperty, userProperty, userProperty, userProperty, userProperty, userProperty));

        final MqttPublish publish = new MqttPublish(
                requireNonNull(MqttTopicImpl.from("topic")),
                ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}), MqttQoS.AT_MOST_ONCE, false, 10,
                Mqtt5PayloadFormatIndicator.UNSPECIFIED, requireNonNull(MqttUTF8StringImpl.from("myContentType")),
                requireNonNull(MqttTopicImpl.from("responseTopic")), ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
                HAS_NOT, userProperties, Mqtt5PublishEncoder.PROVIDER);

        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
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

        final MqttPublish publish = new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")),
                ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}), MqttQoS.AT_MOST_ONCE, false,
                MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null,
                null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);

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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_MOST_ONCE, true,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null, null,
                        null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        1000, Mqtt5PayloadFormatIndicator.UTF_8, null, null, null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5PublishEncoder.PROVIDER);
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
                //     content type
                0x03, 0, 13, 'm', 'y', 'C', 'o', 'n', 't', 'e', 'n', 't', 'T', 'y', 'p', 'e'
        };

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8,
                        MqttUTF8StringImpl.from("myContentType"), null, null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                        MqttTopicImpl.from("responseTopic"), null, HAS_NOT, NO_USER_PROPERTIES,
                        Mqtt5PublishEncoder.PROVIDER);
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

        final ByteBuffer correlationData = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null, null,
                        correlationData, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    @Disabled("correlation data will be validated in the builder, remove this test")
    void encode_correlationDataTooLong_throwsEncoderException() {
        final ByteBuffer correlationData = ByteBuffer.wrap(new byte[65536]);
        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null, null,
                        correlationData, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);

        final MqttPublishWrapper publishInternal =
                publish.wrap(-1, false, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, MAY,
                        NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, MUST_NOT,
                        NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null,
                        DEFAULT_TOPIC_ALIAS_USAGE, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, MAY,
                        NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("key")),
                        requireNonNull(MqttUTF8StringImpl.from("value"))),
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("key2")),
                        requireNonNull(MqttUTF8StringImpl.from("value2")))));

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UTF_8, null, null,
                        null, HAS_NOT, userProperties, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, HAS_NOT,
                        NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null, null, null, HAS_NOT,
                        NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
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
        final MqttPublish publishQos0 =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_MOST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
        encode(expectedQos0, publishQos0, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());

        final MqttPublish publishQos1 =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_LEAST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
        encode(expectedQos1, publishQos1, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());

        final MqttPublish publishQos2 =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.EXACTLY_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, null, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);
        encode(expectedQos2, publishQos2, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntArray.of());
    }

    @Test
    void encode_maximumOutgoingPacketSizeExceeded_throwsEncoderException() {
        createServerConnectionData(100);
        final ByteBuffer correlationData = ByteBuffer.wrap(new byte[100]);
        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_MOST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, correlationData, HAS_NOT, NO_USER_PROPERTIES, Mqtt5PublishEncoder.PROVIDER);


        final MqttPublishWrapper publishInternal =
                publish.wrap(-1, false, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("MqttMaximumPacketSizeExceededException"));
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_MOST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, maxPacket.getCorrelationData(1), HAS_NOT, maxPacket.getMaxPossibleUserProperties(),
                        Mqtt5PublishEncoder.PROVIDER);


        final MqttPublishWrapper publishInternal =
                publish.wrap(-1, false, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();

        final MqttPublish publish =
                new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), null, MqttQoS.AT_MOST_ONCE, false,
                        MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null,
                        null, maxPacket.getCorrelationData(), HAS_NOT, maxPacket.getMaxPossibleUserProperties(1),
                        Mqtt5PublishEncoder.PROVIDER);


        final MqttPublishWrapper publishInternal =
                publish.wrap(-1, false, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false, ImmutableIntArray.of());

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encode(
            final byte[] expected, final MqttPublish publish, final int packetIdentifier, final boolean isDup,
            final ImmutableIntArray subscriptionIdentifiers) {
        final MqttPublishWrapper publishInternal =
                new MqttPublishWrapper(publish, packetIdentifier, isDup, channel, subscriptionIdentifiers);
        encodeInternal(expected, publishInternal);
    }

    private void encode(
            final byte[] expected, final MqttPublish publish, final int packetIdentifier, final boolean isDup,
            final int topicAlias, final boolean isNewTopicAlias, final ImmutableIntArray subscriptionIdentifiers) {
        final MqttPublishWrapper publishInternal =
                publish.wrap(packetIdentifier, isDup, topicAlias, isNewTopicAlias, subscriptionIdentifiers);
        encodeInternal(expected, publishInternal);
    }

    private void encodeInternal(final byte[] expected, final MqttPublishWrapper publishInternal) {
        channel.writeOutbound(publishInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        // byte array initialized to 0's, used to pad max packet.
        private byte[] correlationData;

        MaximumPacketBuilder build() {
            final int maxPropertyLength =
                    MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, dup, qos, retain
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
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        ByteBuffer getCorrelationData() {
            return getCorrelationData(0);
        }

        ByteBuffer getCorrelationData(final int extraBytes) {
            return ByteBuffer.wrap(Arrays.copyOf(correlationData, correlationData.length + extraBytes));
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            //return ImmutableList.of();
            return getMaxPossibleUserProperties(0);
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new MqttUserPropertyImpl(user, property));
            }
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }
    }

}

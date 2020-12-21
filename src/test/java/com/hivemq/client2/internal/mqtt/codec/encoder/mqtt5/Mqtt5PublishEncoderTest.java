/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.datatypes.*;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublishProperty;
import com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client2.internal.util.collections.ImmutableIntList;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.exceptions.MqttEncodeException;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl.NO_USER_PROPERTIES;
import static com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS;
import static com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Katz
 */
class Mqtt5PublishEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    Mqtt5PublishEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.PUBLISH.getCode()] = new Mqtt5PublishEncoder();
        }}, true);
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
                new MqttUserPropertyImpl(MqttUtf8StringImpl.of("test1"), MqttUtf8StringImpl.of("value"));
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(
                ImmutableList.of(userProperty, userProperty, userProperty, userProperty, userProperty, userProperty));

        final MqttPublish publish =
                new MqttPublish(MqttTopicImpl.of("topic"), ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
                        MqttQos.AT_MOST_ONCE, false, 10, Mqtt5PayloadFormatIndicator.UNSPECIFIED,
                        MqttUtf8StringImpl.of("myContentType"), MqttTopicImpl.of("responseTopic"),
                        ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}), userProperties, null);

        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish =
                new MqttPublish(MqttTopicImpl.of("topic"), ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
                        MqttQos.AT_MOST_ONCE, false, MqttPublish.NO_MESSAGE_EXPIRY,
                        Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null, NO_USER_PROPERTIES, null);

        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_MOST_ONCE, true,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, false, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, true, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expected, publish, 17, true, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UTF_8, null, null, null, NO_USER_PROPERTIES,
                null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false, 1000,
                Mqtt5PayloadFormatIndicator.UTF_8, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UTF_8,
                MqttUtf8StringImpl.of("myContentType"), null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UTF_8, null,
                MqttTopicImpl.of("responseTopic"), null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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
        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UTF_8, null, null, correlationData,
                NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, 8, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 2, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, 8, false, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    @Test
    void encode_userProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0011_0010,
                //   remaining length
                46,
                // variable header
                //   topic name
                0, 5, 't', 'o', 'p', 'i', 'c',
                //   Packet Identifier
                0, 15,
                //   properties
                36,
                //     payload format indicator
                0x01, 1,
                //     user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', 0x26, 0, 4, 'u', 's', 'e',
                'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y'
        };

        final MqttUserPropertiesImpl userProperties = getUserProperties(2);

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UTF_8, null, null, null, userProperties,
                null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntList.of(3));
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

        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null, NO_USER_PROPERTIES, null);
        encode(expected, publish, 15, false, DEFAULT_NO_TOPIC_ALIAS, true, ImmutableIntList.of(3, 4));
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
        final MqttPublish publishQos0 = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_MOST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expectedQos0, publishQos0, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);

        final MqttPublish publishQos1 = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_LEAST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expectedQos1, publishQos1, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);

        final MqttPublish publishQos2 = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.EXACTLY_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                NO_USER_PROPERTIES, null);
        encode(expectedQos2, publishQos2, 7, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    @Test
    void encode_maximumOutgoingPacketSizeExceeded_throwsEncoderException() {
        connected(100);
        final ByteBuffer correlationData = ByteBuffer.wrap(new byte[100]);
        final MqttPublish publish = new MqttPublish(MqttTopicImpl.of("topic"), null, MqttQos.AT_MOST_ONCE, false,
                MqttPublish.NO_MESSAGE_EXPIRY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, correlationData,
                NO_USER_PROPERTIES, null);


        final MqttStatefulPublish publishInternal =
                publish.createStateful(-1, false, MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS,
                        DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);

        final Throwable exception =
                assertThrows(MqttEncodeException.class, () -> channel.writeOutbound(publishInternal));
        assertTrue(exception.getMessage()
                .contains("PUBLISH exceeded maximum packet size, minimal possible encoded length: 115, maximum: 100"));
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
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

        // big enough to fit one of the user properties, but not both. Should then omit both.
        connected(expected.length + 2 + userPropertyBytes);
        final MqttUserPropertiesImpl userProperties = getUserProperties(2);

        final MqttPublish publish =
                new MqttPublish(MqttTopicImpl.of("topic"), ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
                        MqttQos.AT_MOST_ONCE, false, MqttPublish.NO_MESSAGE_EXPIRY,
                        Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null, userProperties, null);

        encode(expected, publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final ByteBuf expected = Unpooled.buffer(5 + VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE,
                5 + VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE);

        // 7 topic, 4 property length, 2 payload format, 5 payload, 3 correlation data header
        final int correlationDataLength = VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE - 7 - 4 - 2 - 5 - 3;

        // fixed header
        //   type, flags
        expected.writeByte(0b0011_0000);
        //   remaining length
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        // variable header
        //   topic name
        expected.writeBytes(new byte[]{0, 5, 't', 'o', 'p', 'i', 'c'});
        //   properties
        expected.writeByte(0xef);
        expected.writeByte(0xff);
        expected.writeByte(0xff);
        expected.writeByte(0x7f);
        //     payload format indicator
        expected.writeBytes(new byte[]{0x01, 0});
        //     correlation data
        expected.writeByte(MqttPublishProperty.CORRELATION_DATA);
        expected.writeShort(correlationDataLength);
        for (int i = 0; i < correlationDataLength; i++) {
            expected.writeByte(i);
        }
        // payload
        expected.writeBytes(new byte[]{1, 2, 3, 4, 5});

        final ByteBuffer correlationData = ByteBuffer.wrap(expected.array(), 21, correlationDataLength);
        final MqttUserPropertiesImpl userProperties = getUserProperties(1);

        final MqttPublish publish =
                new MqttPublish(MqttTopicImpl.of("topic"), ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}),
                        MqttQos.AT_MOST_ONCE, false, MqttPublish.NO_MESSAGE_EXPIRY,
                        Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, correlationData, userProperties, null);

        encode(expected.array(), publish, -1, false, DEFAULT_NO_TOPIC_ALIAS, true, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
        expected.release();
    }

    private void encode(
            final @NotNull byte[] expected,
            final @NotNull MqttPublish publish,
            final int packetIdentifier,
            final boolean isDup,
            final @NotNull ImmutableIntList subscriptionIdentifiers) {

        final MqttStatefulPublish publishInternal =
                publish.createStateful(packetIdentifier, isDup, DEFAULT_NO_TOPIC_ALIAS, subscriptionIdentifiers);
        encodeInternal(expected, publishInternal);
    }

    private void encode(
            final @NotNull byte[] expected,
            final @NotNull MqttPublish publish,
            final int packetIdentifier,
            final boolean isDup,
            int topicAlias,
            final boolean isNewTopicAlias,
            final @NotNull ImmutableIntList subscriptionIdentifiers) {

        if (isNewTopicAlias) {
            topicAlias |= MqttStatefulPublish.TOPIC_ALIAS_FLAG_NEW;
        }
        final MqttStatefulPublish publishInternal =
                publish.createStateful(packetIdentifier, isDup, topicAlias, subscriptionIdentifiers);
        encodeInternal(expected, publishInternal);
    }

    private void encodeInternal(final @NotNull byte[] expected, final @NotNull MqttStatefulPublish publishInternal) {
        encode(publishInternal, expected);
    }

    @Override
    int getMaxPropertyLength() {
        return MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, dup, qos, retain
                - 4  // remaining length
                - 7  // topic name 'topic'
                - 4 // property length
                - 2  // payload format
                - 4; // correlation data id, 2 byte length and 1 byte data
    }
}

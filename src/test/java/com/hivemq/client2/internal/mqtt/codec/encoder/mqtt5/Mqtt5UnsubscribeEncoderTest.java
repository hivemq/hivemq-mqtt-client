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
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.exceptions.MqttEncodeException;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Katz
 */
class Mqtt5UnsubscribeEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5UnsubscribeEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.UNSUBSCRIBE.getCode()] = new Mqtt5UnsubscribeEncoder();
        }}, true);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1010_0010,
                // remaining length
                29,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                17,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#'
        };

        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final ImmutableList<MqttTopicFilterImpl> topicFilters = ImmutableList.of(MqttTopicFilterImpl.of("topic/#"));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    void encode_multipleUserProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1010_0010,
                // remaining length
                61,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                51,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', //
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', //
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 5, 't', 'o', 'p', 'i', 'c'
        };

        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty, mqtt5UserProperty));

        final ImmutableList<MqttTopicFilterImpl> topicFilters = ImmutableList.of(MqttTopicFilterImpl.of("topic"));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    void encode_maximumPacketSizeExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1010_0010,
                // remaining length
                12,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#'
        };

        connected(expected.length + 2);

        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final ImmutableList<MqttTopicFilterImpl> topicFilters = ImmutableList.of(MqttTopicFilterImpl.of("topic/#"));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {

        connected(12);

        final ImmutableList<MqttTopicFilterImpl> topicFilters = ImmutableList.of(MqttTopicFilterImpl.of("topic/#"));
        final MqttUnsubscribe unsubscribe =
                new MqttUnsubscribe(topicFilters, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        final int packetIdentifier = 1;
        final MqttStatefulUnsubscribe unsubscribeInternal = unsubscribe.createStateful(packetIdentifier);

        final Throwable exception =
                assertThrows(MqttEncodeException.class, () -> channel.writeOutbound(unsubscribeInternal));
        assertTrue(exception.getMessage().contains("UNSUBSCRIBE exceeded maximum packet size"));
    }

    @Test
    void encode_propertyLengthExceeded_omitUserProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1010_0010,
                // remaining length
                12,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#'
        };

        final ImmutableList<MqttTopicFilterImpl> topicFilters = ImmutableList.of(MqttTopicFilterImpl.of("topic/#"));
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        encodeUnsubscribe(expected, maxPacket.getTooManyUserProperties(), topicFilters);
    }

    private void encodeUnsubscribe(
            final @NotNull byte[] expected,
            final @NotNull MqttUserPropertiesImpl userProperties,
            final @NotNull ImmutableList<MqttTopicFilterImpl> topicFilters) {
        final MqttUnsubscribe unsubscribe = new MqttUnsubscribe(topicFilters, userProperties);
        final int packetIdentifier = 0x01;
        final MqttStatefulUnsubscribe unsubscribeInternal = unsubscribe.createStateful(packetIdentifier);

        encodeInternal(expected, unsubscribeInternal);
    }

    private void encodeInternal(
            final @NotNull byte[] expected, final @NotNull MqttStatefulUnsubscribe unsubscribeInternal) {
        encode(unsubscribeInternal, expected);
    }

    @SuppressWarnings("NullabilityAnnotations")
    private class MaximumPacketBuilder {

        private static final String TOPIC = "topic";
        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");

        MaximumPacketBuilder build() {
            final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                    - 4  // remaining length
                    - 4  // property length
                    - 2  // topic filter length
                    - TOPIC.length(); // default topic filter

            final int userPropertyBytes = 1 // identifier
                    + 2 // key length
                    + 4 // bytes to encode "user"
                    + 2 // value length
                    + 8; // bytes to encode "property"
            final int topicFilterBytes = maxPropertyLength % userPropertyBytes;

            reasonStringBuilder = new StringBuilder();
            for (int i = 0; i < topicFilterBytes; i++) {
                reasonStringBuilder.append(i);
            }

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = ImmutableList.builder();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        MqttUserPropertiesImpl getTooManyUserProperties() {
            userPropertiesBuilder.add(new MqttUserPropertyImpl(user, property));
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }
    }
}

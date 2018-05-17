/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author David Katz
 */
class Mqtt5UnsubscribeEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5UnsubscribeEncoderTest() {
        super(code -> new Mqtt5UnsubscribeEncoder(), true);
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

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic/#")));
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

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty, mqtt5UserProperty));

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic")));
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

        createServerConnectionData(expected.length + 2);

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic/#")));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {

        createServerConnectionData(12);

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic/#")));
        final MqttUnsubscribe unsubscribe =
                new MqttUnsubscribe(topicFilters, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        final int packetIdentifier = 1;
        final MqttStatefulUnsubscribe unsubscribeInternal = unsubscribe.createStateful(packetIdentifier);

        final Throwable exception = assertThrows(MqttMaximumPacketSizeExceededException.class,
                () -> channel.writeOutbound(unsubscribeInternal));
        assertTrue(exception.getMessage().contains("packet size exceeded for UNSUBSCRIBE"));
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

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic/#")));
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        encodeUnsubscribe(expected, maxPacket.getTooManyUserProperties(), topicFilters);
    }

    private void encodeUnsubscribe(
            final byte[] expected, final MqttUserPropertiesImpl userProperties,
            final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        final MqttUnsubscribe unsubscribe = new MqttUnsubscribe(topicFilters, userProperties);
        final int packetIdentifier = 0x01;
        final MqttStatefulUnsubscribe unsubscribeInternal = unsubscribe.createStateful(packetIdentifier);

        encodeInternal(expected, unsubscribeInternal);
    }

    private void encodeInternal(final byte[] expected, final MqttStatefulUnsubscribe unsubscribeInternal) {
        encode(unsubscribeInternal, expected);
    }

    private class MaximumPacketBuilder {

        private static final String TOPIC = "topic";
        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));

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
            userPropertiesBuilder = new ImmutableList.Builder<>();
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

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
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription.*;
import static org.mqttbee.mqtt.datatypes.MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;


/**
 * @author David Katz
 */
class Mqtt5SubscribeEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    private static final int UTF8_STRING_MAX_LENGTH = 65535;
    private static final int SUBSCRIPTION_OPTIONS_LENGTH = 1;
    private static final int UTF8_LENGTH_ENCODED = 2;

    Mqtt5SubscribeEncoderTest() {
        super(code -> new Mqtt5SubscribeEncoder(), true);
    }

    @Test
    void encode_allProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                30,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                17,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0001_1101
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, isNoLocal, retainHandling, isRetainAsPublished));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, userProperties);
        encode(expected, subscribe, 1);
    }

    @Test
    void encode_multipleUserProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                47,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                34,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', //
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0001
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, userProperties);
        encode(expected, subscribe, 10);
    }

    @Test
    void encode_userProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                30,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                17,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0001
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, userProperties);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @EnumSource(MqttQoS.class)
    void encode_subscriptionOptionsQos(final MqttQoS qos) {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0000
        };

        expected[14] |= qos.getCode();

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void encode_subscriptionOptionsNoLocal(final int noLocal) {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0000
        };

        expected[14] |= noLocal << 2;

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final boolean isNoLocal = noLocal == 1;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND;
        final MqttQoS qos = MqttQoS.AT_MOST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5RetainHandling.class)
    void encode_subscriptionOptionsRetain(final Mqtt5RetainHandling retainHandling) {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0101
        };

        expected[14] |= retainHandling.getCode() << 4;

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, subscribe, 10);
    }

    @Test
    void encode_subscriptionIdentifier() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                15,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                2,
                // subscription identifier
                0x0B, 111,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0001
        };

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttSubscribeWrapper subscribeInternal = subscribe.wrap(10, 111);

        encodeInternal(expected, subscribeInternal);
    }

    @Test
    void maximumPacketSizeExceeded_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0101
        };

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(requireNonNull(topicFiler), qos, isNoLocal, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, maxPacket.getTooManyUserProperties(1));
        encode(expected, subscribe, 10);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throws() {
        final int headerLength = 1 + // packet type
                4 + // remaining length
                2 + // packet identifier
                1 + // properties length
                2; // subscription identifier

        final int numberOfMaxTopicFilters = (MAXIMUM_PACKET_SIZE_LIMIT - headerLength) /
                (UTF8_LENGTH_ENCODED + UTF8_STRING_MAX_LENGTH + SUBSCRIPTION_OPTIONS_LENGTH);
        final int leftoverLength = (MAXIMUM_PACKET_SIZE_LIMIT - headerLength) %
                (UTF8_LENGTH_ENCODED + UTF8_STRING_MAX_LENGTH + SUBSCRIPTION_OPTIONS_LENGTH);

        final char[] topicBytes = new char[UTF8_STRING_MAX_LENGTH];
        Arrays.fill(topicBytes, 'x');
        final String topic = new String(topicBytes);
        // fill remaining bytes to max, and then add one more byte to make topic too long
        final char[] leftoverTopicBytes = new char[leftoverLength - SUBSCRIPTION_OPTIONS_LENGTH + 1];
        Arrays.fill(leftoverTopicBytes, 'x');
        final String leftoverTopic = new String(leftoverTopicBytes);

        final MqttTopicFilterImpl topicFilter = MqttTopicFilterImpl.from(topic);
        final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
        final List<MqttSubscription> maxSizeSubscriptions = Collections.nCopies(numberOfMaxTopicFilters,
                new MqttSubscription(requireNonNull(topicFilter), qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));

        final MqttTopicFilterImpl leftoverTopicFilter = MqttTopicFilterImpl.from(leftoverTopic);
        final List<MqttSubscription> leftoverSubscription = ImmutableList.of(
                (new MqttSubscription(requireNonNull(leftoverTopicFilter), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED)));

        final Iterable<MqttSubscription> subscriptions = Iterables.concat(maxSizeSubscriptions, leftoverSubscription);

        final MqttSubscribe subscribe =
                new MqttSubscribe(ImmutableList.copyOf(subscriptions), MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        final int packetIdentifier = 2;
        final MqttSubscribeWrapper subscribeInternal =
                subscribe.wrap(packetIdentifier, MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);

        final Throwable exception = assertThrows(MqttMaximumPacketSizeExceededException.class,
                () -> channel.writeOutbound(subscribeInternal));
        System.err.println(exception.getMessage());
        assertTrue(exception.getMessage()
                .contains(
                        "packet size exceeded for SUBSCRIBE, minimal possible encoded length: 268435461, maximum: 268435460"));
    }

    private void encode(final byte[] expected, final MqttSubscribe subscribe, final int packetIdentifier) {
        final MqttSubscribeWrapper subscribeInternal =
                subscribe.wrap(packetIdentifier, MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        encodeInternal(expected, subscribeInternal);
    }

    private void encodeInternal(final byte[] expected, final MqttSubscribeWrapper subscribeInternal) {
        encode(subscribeInternal, expected);
    }

    @Override
    int getMaxPropertyLength() {
        return MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                - 4  // remaining length
                - 2  // packet identifier
                - 4  // properties length
                - 2  // subscription identifier
                - 1;  // subscription options
    }
}

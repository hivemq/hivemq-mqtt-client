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

package com.hivemq.client.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.MqttEncodeException;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
import static com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Katz
 */
class Mqtt5SubscribeEncoderTest extends AbstractMqtt5EncoderWithUserPropertiesTest {

    private static final int UTF8_STRING_MAX_LENGTH = 65535;
    private static final int SUBSCRIPTION_OPTIONS_LENGTH = 1;
    private static final int UTF8_LENGTH_ENCODED = 2;

    Mqtt5SubscribeEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt5MessageType.SUBSCRIBE.getCode()] = new Mqtt5SubscribeEncoder();
        }}, true);
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

        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_NEW_SUBSCRIPTION;
        final boolean isRetainAsPublished = true;
        final ImmutableList<MqttSubscription> subscriptions =
                ImmutableList.of(new MqttSubscription(topicFiler, qos, isNoLocal, retainHandling, isRetainAsPublished));
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

        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
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

        final MqttUtf8StringImpl user = MqttUtf8StringImpl.of("user");
        final MqttUtf8StringImpl property = MqttUtf8StringImpl.of("property");
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, userProperties);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @EnumSource(MqttQos.class)
    void encode_subscriptionOptionsQos(final @NotNull MqttQos qos) {
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

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
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

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final boolean isNoLocal = noLocal == 1;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND;
        final MqttQos qos = MqttQos.AT_MOST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, isNoLocal, retainHandling, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5RetainHandling.class)
    void encode_subscriptionOptionsRetain(final @NotNull Mqtt5RetainHandling retainHandling) {
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

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, isNoLocal, retainHandling, DEFAULT_RETAIN_AS_PUBLISHED));
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

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final MqttStatefulSubscribe subscribeInternal = subscribe.createStateful(10, 111);

        encodeInternal(expected, subscribeInternal);
    }

    @Test
    void maximumPacketSizeExceeded_omitUserProperties() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder();
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

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.of("topic/#");
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscription(topicFiler, qos, isNoLocal, DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
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

        final MqttTopicFilterImpl topicFilter = MqttTopicFilterImpl.of(topic);
        final MqttQos qos = MqttQos.AT_LEAST_ONCE;
        final List<MqttSubscription> maxSizeSubscriptions = Collections.nCopies(numberOfMaxTopicFilters,
                new MqttSubscription(topicFilter, qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED));

        final MqttTopicFilterImpl leftoverTopicFilter = MqttTopicFilterImpl.of(leftoverTopic);
        final List<MqttSubscription> leftoverSubscription = ImmutableList.of(
                (new MqttSubscription(leftoverTopicFilter, qos, DEFAULT_NO_LOCAL, DEFAULT_RETAIN_HANDLING,
                        DEFAULT_RETAIN_AS_PUBLISHED)));

        final ImmutableList.Builder<MqttSubscription> subscriptionsBuilder = ImmutableList.builder();
        final ImmutableList<MqttSubscription> subscriptions =
                subscriptionsBuilder.addAll(maxSizeSubscriptions).addAll(leftoverSubscription).build();

        final MqttSubscribe subscribe = new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

        final int packetIdentifier = 2;
        final MqttStatefulSubscribe subscribeInternal =
                subscribe.createStateful(packetIdentifier, MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);

        final Throwable exception =
                assertThrows(MqttEncodeException.class, () -> channel.writeOutbound(subscribeInternal));
        assertTrue(exception.getMessage()
                .contains(
                        "SUBSCRIBE exceeded maximum packet size, minimal possible encoded length: 268435461, maximum: 268435460"));
    }

    private void encode(final @NotNull byte[] expected, final MqttSubscribe subscribe, final int packetIdentifier) {
        final MqttStatefulSubscribe subscribeInternal =
                subscribe.createStateful(packetIdentifier, MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        encodeInternal(expected, subscribeInternal);
    }

    private void encodeInternal(
            final @NotNull byte[] expected, final @NotNull MqttStatefulSubscribe subscribeInternal) {

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

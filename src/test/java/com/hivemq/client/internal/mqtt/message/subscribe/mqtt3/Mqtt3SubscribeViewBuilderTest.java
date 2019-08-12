/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.subscribe.mqtt3;

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Michael Walter
 */
class Mqtt3SubscribeViewBuilderTest {

    @Test
    void addSubscription_error_when_subscription_is_null() {
        assertThrows(NullPointerException.class, () -> Mqtt3Subscribe.builder().addSubscription(null));
    }

    @Test
    void addSubscription_error_when_subscription_is_implemented() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Mqtt3Subscribe.builder().addSubscription(mock(Mqtt3Subscription.class)));
    }

    @Test
    void addSubscription_correct_subscription_default_properties() {

        final Mqtt3Subscription subscription = Mqtt3Subscription.builder().topicFilter("test").build();

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().addSubscription(subscription).build();

        final List<? extends Mqtt3Subscription> subscriptions = subscribe.getSubscriptions();
        assertEquals(1, subscriptions.size());

        final Mqtt3Subscription mqtt3Subscription = subscriptions.get(0);

        assertEquals("test", mqtt3Subscription.getTopicFilter().toString());
        assertEquals(MqttQos.EXACTLY_ONCE, mqtt3Subscription.getQos());
    }

    @Test
    void addSubscription_correct_subscription_custom_properties() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().addSubscription(subscription).build();

        final List<? extends Mqtt3Subscription> subscriptions = subscribe.getSubscriptions();
        assertEquals(1, subscriptions.size());

        final Mqtt3Subscription mqtt3Subscription = subscriptions.get(0);

        assertEquals("test", mqtt3Subscription.getTopicFilter().toString());
        assertEquals(MqttQos.AT_LEAST_ONCE, mqtt3Subscription.getQos());
    }

    @Test
    void addSubscription_fluent_subscription_is_finished_if_addSubscription_is_used() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3SubscribeBuilder.Start subscribeBuilder = Mqtt3Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        final Mqtt3Subscribe Mqtt3Subscribe = subscribeBuilder.addSubscription(subscription).build();

        final List<? extends Mqtt3Subscription> subscriptions = Mqtt3Subscribe.getSubscriptions();
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscription, subscriptions.get(1));
    }

    @Test
    void addSubscription_fluent_subscription_is_finished_if_addSubscription_is_used_second_time() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("test2").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3SubscribeBuilder.Start subscribeBuilder = Mqtt3Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        subscribeBuilder.addSubscription(subscription);
        subscribeBuilder.topicFilter("again_fluent");
        final Mqtt3Subscribe Mqtt3Subscribe = subscribeBuilder.addSubscription(subscription2).build();

        final List<? extends Mqtt3Subscription> subscriptions = Mqtt3Subscribe.getSubscriptions();

        assertEquals(4, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscription, subscriptions.get(1));
        assertTrue(subscriptions.get(2).getTopicFilter().toString().contains("again_fluent"));
        assertEquals(subscription2, subscriptions.get(3));
    }

    @Test
    void addSubscription_fluent_subscription_is_finished_if_fluent_addSubscription_is_used() {

        final Mqtt3SubscribeBuilder.Complete subscribeBuilder = Mqtt3Subscribe.builder().topicFilter("fluent");
        final Mqtt3Subscribe Mqtt3Subscribe =
                subscribeBuilder.addSubscription().topicFilter("test").applySubscription().build();

        final List<? extends Mqtt3Subscription> subscriptions = Mqtt3Subscribe.getSubscriptions();
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertTrue(subscriptions.get(1).getTopicFilter().toString().contains("test"));
    }

    @Test
    void fluent_subscription_is_finished_when_building() {

        final Mqtt3Subscribe mqtt3Subscribe = Mqtt3Subscribe.builder().topicFilter("fluent").build();

        final List<? extends Mqtt3Subscription> subscriptions = mqtt3Subscribe.getSubscriptions();
        assertEquals(1, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
    }

    @Test
    void addSubscriptions_error_when_subscriptions_is_null() {
        assertThrows(
                NullPointerException.class,
                () -> Mqtt3Subscribe.builder().addSubscriptions((Stream<Mqtt3Subscription>) null));
    }

    @Test
    void addSubscriptions_error_when_list_is_empty() {
        assertThrows(IllegalStateException.class, () -> Mqtt3Subscribe.builder().addSubscriptions(ImmutableList.of()));
    }

    @Test
    void addSubscriptions_error_when_subscription_is_implemented() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final List<Mqtt3Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(mock(Mqtt3Subscription.class));

        final Mqtt3SubscribeBuilder.Complete builder = Mqtt3Subscribe.builder().topicFilter("first");
        assertThrows(IllegalArgumentException.class, () -> builder.addSubscriptions(subscriptions));
    }

    @Test
    void addSubscriptions_correct_use_list() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final List<Mqtt3Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().addSubscriptions(subscriptions).build();

        compareSubscriptions(subscriptions, subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_set() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final Set<Mqtt3Subscription> subscriptions = new HashSet<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().addSubscriptions(subscriptions).build();

        compareSubscriptions(subscriptions, subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_map() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final Map<String, Mqtt3Subscription> subscriptions = new LinkedHashMap<>();
        subscriptions.put("1", subscription);
        subscriptions.put("2", subscription2);
        subscriptions.put("3", subscription3);

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().addSubscriptions(subscriptions.values()).build();

        compareSubscriptions(subscriptions.values(), subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_array() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final Mqtt3Subscription[] subscriptions = new Mqtt3Subscription[]{
                subscription, subscription2, subscription3
        };

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().addSubscriptions(subscriptions).build();

        compareSubscriptions(subscriptions, subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_stream() {

        final List<String> subscriptions = new ArrayList<>();
        subscriptions.add("test");
        subscriptions.add("multiple");
        subscriptions.add("subscriptions");

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder()
                .addSubscriptions(subscriptions.stream()
                        .map(topicFilter -> Mqtt3Subscription.builder()
                                .topicFilter(topicFilter)
                                .qos(MqttQos.AT_LEAST_ONCE)
                                .build()))
                .build();

        final List<? extends Mqtt3Subscription> mqtt3Subscriptions = subscribe.getSubscriptions();
        assertEquals(3, mqtt3Subscriptions.size());
        assertTrue(mqtt3Subscriptions.get(0).getTopicFilter().toString().contains(subscriptions.get(0)));
        assertTrue(mqtt3Subscriptions.get(1).getTopicFilter().toString().contains(subscriptions.get(1)));
        assertTrue(mqtt3Subscriptions.get(2).getTopicFilter().toString().contains(subscriptions.get(2)));
    }

    @Test
    void addSubscriptions_fluent_subscription_is_finished_if_addSubscriptions_is_used() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final List<Mqtt3Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);

        final Mqtt3SubscribeBuilder.Start subscribeBuilder = Mqtt3Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        final Mqtt3Subscribe Mqtt3Subscribe = subscribeBuilder.addSubscriptions(subscriptions).build();

        final List<? extends Mqtt3Subscription> mqtt3Subscriptions = Mqtt3Subscribe.getSubscriptions();
        assertEquals(4, mqtt3Subscriptions.size());
        assertTrue(mqtt3Subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscriptions.get(0), mqtt3Subscriptions.get(1));
        assertEquals(subscriptions.get(1), mqtt3Subscriptions.get(2));
        assertEquals(subscriptions.get(2), mqtt3Subscriptions.get(3));
    }

    @Test
    void addSubscriptions_fluent_subscription_is_finished_if_addSubscriptions_is_used_second_time() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final ImmutableList<Mqtt3Subscription> subscriptions = ImmutableList.of(subscription, subscription2);

        final ImmutableList<Mqtt3Subscription> subscriptions2 = ImmutableList.of(subscription3);

        final Mqtt3SubscribeBuilder.Start subscribeBuilder = Mqtt3Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        subscribeBuilder.addSubscriptions(subscriptions);
        subscribeBuilder.topicFilter("again_fluent");
        final Mqtt3Subscribe Mqtt3Subscribe = subscribeBuilder.addSubscriptions(subscriptions2).build();

        final List<? extends Mqtt3Subscription> mqtt3Subscriptions = Mqtt3Subscribe.getSubscriptions();

        assertEquals(5, mqtt3Subscriptions.size());
        assertTrue(mqtt3Subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscriptions.get(0), mqtt3Subscriptions.get(1));
        assertEquals(subscriptions.get(1), mqtt3Subscriptions.get(2));
        assertTrue(mqtt3Subscriptions.get(3).getTopicFilter().toString().contains("again_fluent"));
        assertEquals(subscriptions2.get(0), mqtt3Subscriptions.get(4));
    }

    @Test
    void addSubscriptions_fluent_subscription_and_addSubscription() {

        final Mqtt3Subscription subscription =
                Mqtt3Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt3Subscription subscription2 =
                Mqtt3Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt3Subscription subscription3 =
                Mqtt3Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final List<Mqtt3Subscription> subscriptions = new LinkedList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);

        final Mqtt3SubscribeBuilder.Start subscribeBuilder = Mqtt3Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        subscribeBuilder.addSubscriptions(subscriptions);
        subscribeBuilder.topicFilter("again_fluent");
        final Mqtt3Subscribe Mqtt3Subscribe = subscribeBuilder.addSubscription(subscription3).build();

        final List<? extends Mqtt3Subscription> mqtt3Subscriptions = Mqtt3Subscribe.getSubscriptions();

        assertEquals(5, mqtt3Subscriptions.size());
        assertTrue(mqtt3Subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscriptions.get(0), mqtt3Subscriptions.get(1));
        assertEquals(subscriptions.get(1), mqtt3Subscriptions.get(2));
        assertTrue(mqtt3Subscriptions.get(3).getTopicFilter().toString().contains("again_fluent"));
        assertEquals(subscription3, mqtt3Subscriptions.get(4));
    }

    void compareSubscriptions(
            final @NotNull Mqtt3Subscription[] expected, final @NotNull List<? extends Mqtt3Subscription> actual) {

        assertEquals(expected.length, actual.size());

        for (final Mqtt3Subscription expectedSubscription : expected) {
            assertTrue(actual.contains(expectedSubscription));
        }
    }

    void compareSubscriptions(
            final @NotNull Collection<Mqtt3Subscription> expected,
            final @NotNull List<? extends Mqtt3Subscription> actual) {

        assertEquals(expected.size(), actual.size());

        for (final Mqtt3Subscription expectedSubscription : expected) {
            assertTrue(actual.contains(expectedSubscription));
        }
    }
}
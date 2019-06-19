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

package com.hivemq.client.internal.mqtt.message.subscribe;

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import util.implementations.CustomMqtt5Subscription;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Michael Walter
 */
class MqttSubscribeBuilderTest {

    @Test
    void addSubscription_error_when_subscription_is_null() {
        assertThrows(NullPointerException.class, () -> Mqtt5Subscribe.builder().addSubscription(null));
    }

    @Test
    void addSubscription_error_when_subscription_is_implemented() {

        assertThrows(IllegalArgumentException.class,
                () -> Mqtt5Subscribe.builder().addSubscription(new CustomMqtt5Subscription()));
    }

    @Test
    void addSubscription_correct_subscription_default_properties() {

        final Mqtt5Subscription subscription = Mqtt5Subscription.builder().topicFilter("test").build();

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().addSubscription(subscription).build();

        final List<@NotNull ? extends Mqtt5Subscription> subscriptions = subscribe.getSubscriptions();
        assertEquals(1, subscriptions.size());

        final Mqtt5Subscription mqtt5Subscription = subscriptions.get(0);

        assertEquals("test", mqtt5Subscription.getTopicFilter().toString());
        assertEquals(MqttQos.EXACTLY_ONCE, mqtt5Subscription.getQos());
        assertFalse(mqtt5Subscription.isNoLocal());
        assertFalse(mqtt5Subscription.isRetainAsPublished());
        assertEquals(Mqtt5RetainHandling.SEND, mqtt5Subscription.getRetainHandling());
    }

    @Test
    void addSubscription_correct_subscription_custom_properties() {

        final Mqtt5Subscription subscription = Mqtt5Subscription.builder()
                .topicFilter("test")
                .qos(MqttQos.AT_LEAST_ONCE)
                .noLocal(true)
                .retainAsPublished(true)
                .retainHandling(Mqtt5RetainHandling.DO_NOT_SEND)
                .build();

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().addSubscription(subscription).build();

        final List<@NotNull ? extends Mqtt5Subscription> subscriptions = subscribe.getSubscriptions();
        assertEquals(1, subscriptions.size());

        final Mqtt5Subscription mqtt5Subscription = subscriptions.get(0);

        assertEquals("test", mqtt5Subscription.getTopicFilter().toString());
        assertEquals(MqttQos.AT_LEAST_ONCE, mqtt5Subscription.getQos());
        assertTrue(mqtt5Subscription.isNoLocal());
        assertTrue(mqtt5Subscription.isRetainAsPublished());
        assertEquals(Mqtt5RetainHandling.DO_NOT_SEND, mqtt5Subscription.getRetainHandling());
    }

    @Test
    void addSubscription_fluent_subscription_is_finished_if_addSubscription_is_used() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5SubscribeBuilder.Start subscribeBuilder = Mqtt5Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        final Mqtt5Subscribe mqtt5Subscribe = subscribeBuilder.addSubscription(subscription).build();

        final List<@NotNull ? extends Mqtt5Subscription> subscriptions = mqtt5Subscribe.getSubscriptions();
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscription, subscriptions.get(1));
    }

    @Test
    void addSubscription_fluent_subscription_is_finished_if_addSubscription_is_used_second_time() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("test2").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5SubscribeBuilder.Start subscribeBuilder = Mqtt5Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        subscribeBuilder.addSubscription(subscription);
        subscribeBuilder.topicFilter("again_fluent");
        final Mqtt5Subscribe mqtt5Subscribe = subscribeBuilder.addSubscription(subscription2).build();

        final List<@NotNull ? extends Mqtt5Subscription> subscriptions = mqtt5Subscribe.getSubscriptions();

        assertEquals(4, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscription, subscriptions.get(1));
        assertTrue(subscriptions.get(2).getTopicFilter().toString().contains("again_fluent"));
        assertEquals(subscription2, subscriptions.get(3));
    }

    @Test
    void addSubscription_fluent_subscription_is_finished_if_fluent_addSubscription_is_used() {

        final Mqtt5SubscribeBuilder.Complete subscribeBuilder = Mqtt5Subscribe.builder().topicFilter("fluent");
        final Mqtt5Subscribe mqtt5Subscribe =
                subscribeBuilder.addSubscription().topicFilter("test").applySubscription().build();

        final List<@NotNull ? extends Mqtt5Subscription> subscriptions = mqtt5Subscribe.getSubscriptions();
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertTrue(subscriptions.get(1).getTopicFilter().toString().contains("test"));
    }

    @Test
    void fluent_subscription_is_finished_when_building() {

        final Mqtt5Subscribe mqtt5Subscribe = Mqtt5Subscribe.builder().topicFilter("fluent").build();

        final List<@NotNull ? extends Mqtt5Subscription> subscriptions = mqtt5Subscribe.getSubscriptions();
        assertEquals(1, subscriptions.size());
        assertTrue(subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
    }

    @Test
    void addSubscriptions_error_when_subscriptions_is_null() {
        assertThrows(
                NullPointerException.class,
                () -> Mqtt5Subscribe.builder().addSubscriptions((Stream<Mqtt5Subscription>) null));
    }

    @Test
    void addSubscriptions_error_when_list_is_empty() {
        assertThrows(
                IllegalArgumentException.class, () -> Mqtt5Subscribe.builder().addSubscriptions(ImmutableList.of()));
    }

    @Test
    void addSubscriptions_error_when_subscription_is_implemented() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final List<Mqtt5Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(new CustomMqtt5Subscription());

        final Mqtt5SubscribeBuilder.Complete builder = Mqtt5Subscribe.builder().topicFilter("first");
        assertThrows(IllegalArgumentException.class, () -> {
            builder.addSubscriptions(subscriptions);
        });

        final Mqtt5Subscribe subscribe = builder.build();

        assertEquals(1, subscribe.getSubscriptions().size());
        assertTrue(subscribe.getSubscriptions().get(0).getTopicFilter().toString().contains("first"));
    }

    @Test
    void addSubscriptions_correct_use_list() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final List<Mqtt5Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().addSubscriptions(subscriptions).build();

        final List<@NotNull ? extends Mqtt5Subscription> mqtt5Subscriptions = subscribe.getSubscriptions();
        assertEquals(subscriptions.size(), mqtt5Subscriptions.size());
        assertEquals(subscription, mqtt5Subscriptions.get(0));
        assertEquals(subscription2, mqtt5Subscriptions.get(1));
        assertEquals(subscription3, mqtt5Subscriptions.get(2));
    }

    @Test
    void addSubscriptions_correct_use_set() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final Set<Mqtt5Subscription> subscriptions = new HashSet<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscriptions.add(subscription3);

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().addSubscriptions(subscriptions).build();

        compareSubscriptions(subscriptions, subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_map() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final Map<String, Mqtt5Subscription> subscriptions = new LinkedHashMap<>();
        subscriptions.put("1", subscription);
        subscriptions.put("2", subscription2);
        subscriptions.put("3", subscription3);

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().addSubscriptions(subscriptions.values()).build();

        compareSubscriptions(subscriptions.values(), subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_array() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final Mqtt5Subscription[] subscriptions = new Mqtt5Subscription[]{
                subscription, subscription2, subscription3
        };

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().addSubscriptions(subscriptions).build();

        compareSubscriptions(subscriptions, subscribe.getSubscriptions());
    }

    @Test
    void addSubscriptions_correct_use_stream() {

        final List<String> subscriptions = new ArrayList<>();
        subscriptions.add("test");
        subscriptions.add("multiple");
        subscriptions.add("subscriptions");

        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder()
                .addSubscriptions(subscriptions.stream()
                        .map(topicFilter -> Mqtt5Subscription.builder()
                                .topicFilter(topicFilter)
                                .qos(MqttQos.AT_LEAST_ONCE)
                                .build()))
                .build();

        final List<@NotNull ? extends Mqtt5Subscription> Mqtt5Subscriptions = subscribe.getSubscriptions();
        assertEquals(3, Mqtt5Subscriptions.size());
        assertTrue(Mqtt5Subscriptions.get(0).getTopicFilter().toString().contains(subscriptions.get(0)));
        assertTrue(Mqtt5Subscriptions.get(1).getTopicFilter().toString().contains(subscriptions.get(1)));
        assertTrue(Mqtt5Subscriptions.get(2).getTopicFilter().toString().contains(subscriptions.get(2)));
    }

    @Test
    void addSubscriptions_fluent_subscription_is_finished_if_addSubscriptions_is_used() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final ImmutableList<Mqtt5Subscription> subscriptions =
                ImmutableList.of(subscription, subscription2, subscription3);

        final Mqtt5SubscribeBuilder.Start subscribeBuilder = Mqtt5Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        final Mqtt5Subscribe mqtt5Subscribe = subscribeBuilder.addSubscriptions(subscriptions).build();

        final List<@NotNull ? extends Mqtt5Subscription> mqtt5Subscriptions = mqtt5Subscribe.getSubscriptions();
        assertEquals(4, mqtt5Subscriptions.size());
        assertTrue(mqtt5Subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscriptions.get(0), mqtt5Subscriptions.get(1));
        assertEquals(subscriptions.get(1), mqtt5Subscriptions.get(2));
        assertEquals(subscriptions.get(2), mqtt5Subscriptions.get(3));
    }

    @Test
    void addSubscriptions_fluent_subscription_is_finished_if_addSubscriptions_is_used_second_time() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final List<Mqtt5Subscription> subscriptions = new LinkedList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);

        final ImmutableList<Mqtt5Subscription> subscriptions2 = ImmutableList.of(subscription3);

        final Mqtt5SubscribeBuilder.Start subscribeBuilder = Mqtt5Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        subscribeBuilder.addSubscriptions(subscriptions);
        subscribeBuilder.topicFilter("again_fluent");
        final Mqtt5Subscribe mqtt5Subscribe = subscribeBuilder.addSubscriptions(subscriptions2).build();

        final List<@NotNull ? extends Mqtt5Subscription> mqtt5Subscriptions = mqtt5Subscribe.getSubscriptions();

        assertEquals(5, mqtt5Subscriptions.size());
        assertTrue(mqtt5Subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscriptions.get(0), mqtt5Subscriptions.get(1));
        assertEquals(subscriptions.get(1), mqtt5Subscriptions.get(2));
        assertTrue(mqtt5Subscriptions.get(3).getTopicFilter().toString().contains("again_fluent"));
        assertEquals(subscriptions2.get(0), mqtt5Subscriptions.get(4));
    }

    @Test
    void addSubscriptions_fluent_subscription_and_addSubscription() {

        final Mqtt5Subscription subscription =
                Mqtt5Subscription.builder().topicFilter("test").qos(MqttQos.AT_LEAST_ONCE).build();

        final Mqtt5Subscription subscription2 =
                Mqtt5Subscription.builder().topicFilter("multiple").qos(MqttQos.AT_MOST_ONCE).build();

        final Mqtt5Subscription subscription3 =
                Mqtt5Subscription.builder().topicFilter("subscriptions").qos(MqttQos.EXACTLY_ONCE).build();

        final List<Mqtt5Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);

        final Mqtt5SubscribeBuilder.Start subscribeBuilder = Mqtt5Subscribe.builder();
        subscribeBuilder.topicFilter("fluent");
        subscribeBuilder.addSubscriptions(subscriptions);
        subscribeBuilder.topicFilter("again_fluent");
        final Mqtt5Subscribe mqtt5Subscribe = subscribeBuilder.addSubscription(subscription3).build();

        final List<@NotNull ? extends Mqtt5Subscription> mqtt5Subscriptions = mqtt5Subscribe.getSubscriptions();

        assertEquals(5, mqtt5Subscriptions.size());
        assertTrue(mqtt5Subscriptions.get(0).getTopicFilter().toString().contains("fluent"));
        assertEquals(subscriptions.get(0), mqtt5Subscriptions.get(1));
        assertEquals(subscriptions.get(1), mqtt5Subscriptions.get(2));
        assertTrue(mqtt5Subscriptions.get(3).getTopicFilter().toString().contains("again_fluent"));
        assertEquals(subscription3, mqtt5Subscriptions.get(4));
    }

    void compareSubscriptions(
            final @NotNull Mqtt5Subscription[] expected, final List<@NotNull ? extends Mqtt5Subscription> actual) {

        assertEquals(expected.length, actual.size());

        for (final Mqtt5Subscription expectedSubscription : expected) {
            assertTrue(actual.contains(expectedSubscription));
        }
    }

    void compareSubscriptions(
            final @NotNull Collection<Mqtt5Subscription> expected,
            final List<@NotNull ? extends Mqtt5Subscription> actual) {

        assertEquals(expected.size(), actual.size());

        for (final Mqtt5Subscription expectedSubscription : expected) {
            assertTrue(actual.contains(expectedSubscription));
        }
    }
}
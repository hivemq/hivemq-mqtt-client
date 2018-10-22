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

package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscriptionView;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscribeBuilder<P> extends FluentBuilder<Mqtt3Subscribe, P> {

    private final @NotNull ImmutableList.Builder<MqttSubscription> subscriptionBuilder;
    private @Nullable Mqtt3SubscriptionBuilder<Void> firstSubscriptionBuilder;

    public Mqtt3SubscribeBuilder(@Nullable final Function<? super Mqtt3Subscribe, P> parentConsumer) {
        super(parentConsumer);
        subscriptionBuilder = ImmutableList.builder();
    }

    Mqtt3SubscribeBuilder(final @NotNull Mqtt3Subscribe subscribe) {
        super(null);
        final Mqtt3SubscribeView subscribeView =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, Mqtt3SubscribeView.class);
        final ImmutableList<MqttSubscription> subscriptions = subscribeView.getDelegate().getSubscriptions();
        subscriptionBuilder = ImmutableList.builderWithExpectedSize(subscriptions.size() + 1);
        subscriptionBuilder.addAll(subscriptions);
    }

    private @NotNull Mqtt3SubscriptionBuilder<Void> getFirstSubscriptionBuilder() {
        if (firstSubscriptionBuilder == null) {
            firstSubscriptionBuilder = Mqtt3Subscription.builder();
        }
        return firstSubscriptionBuilder;
    }

    public @NotNull Mqtt3SubscribeBuilder<P> topicFilter(final @NotNull String topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return this;
    }

    public @NotNull Mqtt3SubscribeBuilder<P> topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<? extends Mqtt3SubscribeBuilder<P>> topicFilter() {
        return new MqttTopicFilterBuilder<>("", this::topicFilter);
    }

    public @NotNull Mqtt3SubscribeBuilder<P> qos(final @NotNull MqttQos qos) {
        getFirstSubscriptionBuilder().qos(qos);
        return this;
    }

    public @NotNull Mqtt3SubscribeBuilder<P> addSubscription(final @NotNull Mqtt3Subscription subscription) {
        final Mqtt3SubscriptionView subscriptionView =
                MustNotBeImplementedUtil.checkNotImplemented(subscription, Mqtt3SubscriptionView.class);
        subscriptionBuilder.add(subscriptionView.getDelegate());
        return this;
    }

    public @NotNull Mqtt3SubscriptionBuilder<? extends Mqtt3SubscribeBuilder<P>> addSubscription() {
        return new Mqtt3SubscriptionBuilder<>(this::addSubscription);
    }

    @Override
    public @NotNull Mqtt3Subscribe build() {
        if (firstSubscriptionBuilder != null) {
            addSubscription(firstSubscriptionBuilder.build()); // TODO add as first subscription #192
        }
        final ImmutableList<MqttSubscription> subscriptions = subscriptionBuilder.build();
        Preconditions.checkState(!subscriptions.isEmpty());
        return Mqtt3SubscribeView.of(subscriptions);
    }

    public @NotNull P applySubscribe() {
        return apply();
    }

}

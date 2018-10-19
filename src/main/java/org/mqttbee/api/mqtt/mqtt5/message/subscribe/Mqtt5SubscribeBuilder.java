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

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeBuilder<P> extends FluentBuilder<Mqtt5Subscribe, P> {

    private final @NotNull ImmutableList.Builder<MqttSubscription> subscriptionBuilder;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
    private @Nullable Mqtt5SubscriptionBuilder<Void> firstSubscriptionBuilder;

    public Mqtt5SubscribeBuilder(final @Nullable Function<? super Mqtt5Subscribe, P> parentConsumer) {
        super(parentConsumer);
        subscriptionBuilder = ImmutableList.builder();
    }

    Mqtt5SubscribeBuilder(final @NotNull Mqtt5Subscribe subscribe) {
        super(null);
        final MqttSubscribe subscribeImpl =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, MqttSubscribe.class);
        final ImmutableList<MqttSubscription> subscriptions = subscribeImpl.getSubscriptions();
        subscriptionBuilder = ImmutableList.builderWithExpectedSize(subscriptions.size() + 1);
        subscriptionBuilder.addAll(subscriptions);
    }

    private @NotNull Mqtt5SubscriptionBuilder<Void> getFirstSubscriptionBuilder() {
        if (firstSubscriptionBuilder == null) {
            firstSubscriptionBuilder = Mqtt5Subscription.builder();
        }
        return firstSubscriptionBuilder;
    }

    public @NotNull Mqtt5SubscribeBuilder<P> topicFilter(final @NotNull String topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return this;
    }

    public @NotNull Mqtt5SubscribeBuilder<P> topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<? extends Mqtt5SubscribeBuilder<P>> topicFilter() {
        return new MqttTopicFilterBuilder<>("", this::topicFilter);
    }

    public @NotNull Mqtt5SubscribeBuilder<P> qos(final @NotNull MqttQos qos) {
        getFirstSubscriptionBuilder().qos(qos);
        return this;
    }

    public @NotNull Mqtt5SubscribeBuilder<P> noLocal(final boolean noLocal) {
        getFirstSubscriptionBuilder().noLocal(noLocal);
        return this;
    }

    public @NotNull Mqtt5SubscribeBuilder<P> retainHandling(final @NotNull Mqtt5RetainHandling retainHandling) {
        getFirstSubscriptionBuilder().retainHandling(retainHandling);
        return this;
    }

    public @NotNull Mqtt5SubscribeBuilder<P> retainAsPublished(final boolean retainAsPublished) {
        getFirstSubscriptionBuilder().retainAsPublished(retainAsPublished);
        return this;
    }

    public @NotNull Mqtt5SubscribeBuilder<P> addSubscription(final @NotNull Mqtt5Subscription subscription) {
        subscriptionBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(subscription, MqttSubscription.class));
        return this;
    }

    public @NotNull Mqtt5SubscriptionBuilder<? extends Mqtt5SubscribeBuilder<P>> addSubscription() {
        return new Mqtt5SubscriptionBuilder<>(this::addSubscription);
    }

    public @NotNull Mqtt5SubscribeBuilder<P> userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    public @NotNull Mqtt5UserPropertiesBuilder<? extends Mqtt5SubscribeBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @Override
    public @NotNull Mqtt5Subscribe build() {
        if (firstSubscriptionBuilder != null) {
            addSubscription(firstSubscriptionBuilder.build()); // TODO add as first subscription #192
        }
        final ImmutableList<MqttSubscription> subscriptions = subscriptionBuilder.build();
        Preconditions.checkState(!subscriptions.isEmpty());
        return new MqttSubscribe(subscriptions, userProperties);
    }

}

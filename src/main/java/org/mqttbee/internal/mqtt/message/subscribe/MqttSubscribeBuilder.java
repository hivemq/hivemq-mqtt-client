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

package org.mqttbee.internal.mqtt.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import org.mqttbee.internal.mqtt.util.MqttChecks;
import org.mqttbee.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import org.mqttbee.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.util.Checks;
import org.mqttbee.util.collections.ImmutableList;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttSubscribeBuilder<B extends MqttSubscribeBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttSubscription> subscriptionsBuilder;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
    private @Nullable MqttSubscriptionBuilder.Default firstSubscriptionBuilder;

    protected MqttSubscribeBuilder() {
        subscriptionsBuilder = ImmutableList.builder();
    }

    MqttSubscribeBuilder(final @NotNull MqttSubscribe subscribe) {
        final ImmutableList<MqttSubscription> subscriptions = subscribe.getSubscriptions();
        subscriptionsBuilder = ImmutableList.builder(subscriptions.size() + 1);
        subscriptionsBuilder.addAll(subscriptions);
    }

    protected abstract @NotNull B self();

    public @NotNull B addSubscription(final @Nullable Mqtt5Subscription subscription) {
        buildFirstSubscription();
        subscriptionsBuilder.add(Checks.notImplemented(subscription, MqttSubscription.class, "Subscription"));
        return self();
    }

    public @NotNull MqttSubscriptionBuilder.Nested<B> addSubscription() {
        return new MqttSubscriptionBuilder.Nested<>(this::addSubscription);
    }

    public @NotNull B userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return self();
    }

    private @NotNull MqttSubscriptionBuilder.Default getFirstSubscriptionBuilder() {
        if (firstSubscriptionBuilder == null) {
            firstSubscriptionBuilder = new MqttSubscriptionBuilder.Default();
        }
        return firstSubscriptionBuilder;
    }

    private void buildFirstSubscription() {
        if (firstSubscriptionBuilder != null) {
            subscriptionsBuilder.add(firstSubscriptionBuilder.build());
            firstSubscriptionBuilder = null;
        }
    }

    public @NotNull MqttUserPropertiesImplBuilder.Nested<B> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(this::userProperties);
    }

    public @NotNull B topicFilter(final @Nullable String topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self();
    }

    public @NotNull B topicFilter(final @Nullable MqttTopicFilter topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self();
    }

    public @NotNull MqttTopicFilterImplBuilder.Nested<B> topicFilter() {
        return new MqttTopicFilterImplBuilder.Nested<>(this::topicFilter);
    }

    public @NotNull B qos(final @Nullable MqttQos qos) {
        getFirstSubscriptionBuilder().qos(qos);
        return self();
    }

    public @NotNull B noLocal(final boolean noLocal) {
        getFirstSubscriptionBuilder().noLocal(noLocal);
        return self();
    }

    public @NotNull B retainHandling(final @Nullable Mqtt5RetainHandling retainHandling) {
        getFirstSubscriptionBuilder().retainHandling(retainHandling);
        return self();
    }

    public @NotNull B retainAsPublished(final boolean retainAsPublished) {
        getFirstSubscriptionBuilder().retainAsPublished(retainAsPublished);
        return self();
    }

    public @NotNull MqttSubscribe build() {
        buildFirstSubscription();
        final ImmutableList<MqttSubscription> subscriptions = subscriptionsBuilder.build();
        if (subscriptions.isEmpty()) {
            throw new IllegalStateException("At least one subscription must be added.");
        }
        return new MqttSubscribe(subscriptions, userProperties);
    }

    public static class Default extends MqttSubscribeBuilder<Default> implements Mqtt5SubscribeBuilder.Start.Complete {

        public Default() {}

        Default(final @NotNull MqttSubscribe subscribe) {
            super(subscribe);
        }

        @Override
        protected @NotNull MqttSubscribeBuilder.Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttSubscribeBuilder<Nested<P>>
            implements Mqtt5SubscribeBuilder.Nested.Start.Complete<P> {

        private final @NotNull Function<? super MqttSubscribe, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttSubscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull MqttSubscribeBuilder.Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applySubscribe() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends MqttSubscribeBuilder<Send<P>>
            implements Mqtt5SubscribeBuilder.Send.Start.Complete<P> {

        private final @NotNull Function<? super MqttSubscribe, P> parentConsumer;

        public Send(final @NotNull Function<? super MqttSubscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull MqttSubscribeBuilder.Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}

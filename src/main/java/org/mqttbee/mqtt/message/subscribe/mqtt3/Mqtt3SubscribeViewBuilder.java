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

package org.mqttbee.mqtt.message.subscribe.mqtt3;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttChecks;
import org.mqttbee.util.Checks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3SubscribeViewBuilder<B extends Mqtt3SubscribeViewBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttSubscription> subscriptionsBuilder;
    private @Nullable Mqtt3SubscriptionViewBuilder.Default firstSubscriptionBuilder;

    protected Mqtt3SubscribeViewBuilder() {
        subscriptionsBuilder = ImmutableList.builder();
    }

    Mqtt3SubscribeViewBuilder(final @Nullable Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        final ImmutableList<MqttSubscription> subscriptions = mqttSubscribe.getSubscriptions();
        subscriptionsBuilder = ImmutableList.builderWithExpectedSize(subscriptions.size() + 1);
        subscriptionsBuilder.addAll(subscriptions);
    }

    protected abstract @NotNull B self();

    public @NotNull B addSubscription(final @Nullable Mqtt3Subscription subscription) {
        buildFirstSubscription();
        subscriptionsBuilder.add(
                Checks.notImplemented(subscription, Mqtt3SubscriptionView.class, "Subscription").getDelegate());
        return self();
    }

    public @NotNull Mqtt3SubscriptionViewBuilder.Nested<B> addSubscription() {
        return new Mqtt3SubscriptionViewBuilder.Nested<>(this::addSubscription);
    }

    private @NotNull Mqtt3SubscriptionViewBuilder.Default getFirstSubscriptionBuilder() {
        if (firstSubscriptionBuilder == null) {
            firstSubscriptionBuilder = new Mqtt3SubscriptionViewBuilder.Default();
        }
        return firstSubscriptionBuilder;
    }

    private void buildFirstSubscription() {
        if (firstSubscriptionBuilder != null) {
            subscriptionsBuilder.add(firstSubscriptionBuilder.build().getDelegate());
            firstSubscriptionBuilder = null;
        }
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

    public @NotNull Mqtt3SubscribeView build() {
        buildFirstSubscription();
        final ImmutableList<MqttSubscription> subscriptions = subscriptionsBuilder.build();
        if (subscriptions.isEmpty()) {
            throw new IllegalStateException("At least one subscription must be added.");
        }
        return Mqtt3SubscribeView.of(subscriptions);
    }

    public static class Default extends Mqtt3SubscribeViewBuilder<Default>
            implements Mqtt3SubscribeBuilder.Start.Complete {

        public Default() {}

        public Default(final @Nullable Mqtt3Subscribe subscribe) {
            super(subscribe);
        }

        @Override
        protected @NotNull Mqtt3SubscribeViewBuilder.Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3SubscribeViewBuilder<Nested<P>>
            implements Mqtt3SubscribeBuilder.Nested.Start.Complete<P> {

        private final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applySubscribe() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends Mqtt3SubscribeViewBuilder<Send<P>>
            implements Mqtt3SubscribeBuilder.Send.Start.Complete<P> {

        private final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer;

        public Send(final @NotNull Function<? super Mqtt3Subscribe, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        protected @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}

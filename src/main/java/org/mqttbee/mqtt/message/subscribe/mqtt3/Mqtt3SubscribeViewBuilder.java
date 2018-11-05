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
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

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

    Mqtt3SubscribeViewBuilder(final @NotNull Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = Mqtt3SubscribeView.delegate(subscribe);
        final ImmutableList<MqttSubscription> subscriptions = mqttSubscribe.getSubscriptions();
        subscriptionsBuilder = ImmutableList.builderWithExpectedSize(subscriptions.size() + 1);
        subscriptionsBuilder.addAll(subscriptions);
    }

    protected abstract @NotNull B self();

    public @NotNull B addSubscription(final @NotNull Mqtt3Subscription subscription) {
        buildFirstSubscription();
        subscriptionsBuilder.add(Mqtt3SubscriptionView.delegate(subscription));
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
            subscriptionsBuilder.add(Mqtt3SubscriptionView.delegate(firstSubscriptionBuilder.build()));
            firstSubscriptionBuilder = null;
        }
    }

    public @NotNull B topicFilter(final @NotNull String topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self();
    }

    public @NotNull B topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        getFirstSubscriptionBuilder().topicFilter(topicFilter);
        return self();
    }

    public @NotNull MqttTopicFilterBuilder<B> topicFilter() {
        return new MqttTopicFilterBuilder<>(this::topicFilter);
    }

    public @NotNull B qos(final @NotNull MqttQos qos) {
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

    // @formatter:off
    public static class Default
            extends Mqtt3SubscribeViewBuilder<Default>
            implements Mqtt3SubscribeBuilder.Complete,
                       Mqtt3SubscribeBuilder.Start.Complete {
    // @formatter:on

        public Default() {}

        public Default(final @NotNull Mqtt3Subscribe subscribe) {
            super(subscribe);
        }

        @Override
        protected @NotNull Mqtt3SubscribeViewBuilder.Default self() {
            return this;
        }
    }

    // @formatter:off
    public static class Nested<P>
            extends Mqtt3SubscribeViewBuilder<Nested<P>>
            implements Mqtt3SubscribeBuilder.Nested.Complete<P>,
                       Mqtt3SubscribeBuilder.Nested.Start.Complete<P> {
    // @formatter:on

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

    // @formatter:off
    public static class Send<P>
            extends Mqtt3SubscribeViewBuilder<Send<P>>
            implements Mqtt3SubscribeBuilder.Send.Complete<P>,
                       Mqtt3SubscribeBuilder.Send.Start.Complete<P> {
    // @formatter:on

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

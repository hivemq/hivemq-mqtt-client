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

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3SubscribeViewBuilder<B extends Mqtt3SubscribeViewBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttSubscription> subscriptionsBuilder;
    private @Nullable Mqtt3SubscriptionViewBuilder.Default firstSubscriptionBuilder;

    protected Mqtt3SubscribeViewBuilder() {
        subscriptionsBuilder = ImmutableList.builder();
    }

    Mqtt3SubscribeViewBuilder(final @NotNull Mqtt3SubscribeView subscribe) {
        final ImmutableList<MqttSubscription> subscriptions = subscribe.getDelegate().getSubscriptions();
        subscriptionsBuilder = ImmutableList.builder(subscriptions.size() + 1);
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

    public @NotNull B addSubscriptions(final @Nullable Mqtt3Subscription @Nullable ... subscriptions) {
        Checks.notNull(subscriptions, "Subscriptions");
        buildFirstSubscription();
        subscriptionsBuilder.ensureFree(subscriptions.length);
        for (final Mqtt3Subscription subscription : subscriptions) {
            addSubscription(subscription);
        }
        ensureAtLeastOneSubscription();
        return self();
    }

    public @NotNull B addSubscriptions(
            final @Nullable Collection<@Nullable ? extends Mqtt3Subscription> subscriptions) {

        Checks.notNull(subscriptions, "Subscriptions");
        buildFirstSubscription();
        subscriptionsBuilder.ensureFree(subscriptions.size());
        subscriptions.forEach(this::addSubscription);
        ensureAtLeastOneSubscription();
        return self();
    }

    public @NotNull B addSubscriptions(final @Nullable Stream<@Nullable ? extends Mqtt3Subscription> subscriptions) {
        Checks.notNull(subscriptions, "Subscriptions");
        buildFirstSubscription();
        subscriptions.forEach(this::addSubscription);
        ensureAtLeastOneSubscription();
        return self();
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

    private void ensureAtLeastOneSubscription() {
        Checks.state(subscriptionsBuilder.getSize() > 0, "At least one subscription must be added.");
    }

    public @NotNull Mqtt3SubscribeView build() {
        buildFirstSubscription();
        ensureAtLeastOneSubscription();
        return Mqtt3SubscribeView.of(subscriptionsBuilder.build());
    }

    public static class Default extends Mqtt3SubscribeViewBuilder<Default>
            implements Mqtt3SubscribeBuilder.Start.Complete {

        public Default() {}

        Default(final @NotNull Mqtt3SubscribeView subscribe) {
            super(subscribe);
        }

        @Override
        protected @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3SubscribeViewBuilder<Nested<P>>
            implements Mqtt3SubscribeBuilder.Nested.Start.Complete<P> {

        private final @NotNull Function<? super Mqtt3SubscribeView, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3SubscribeView, P> parentConsumer) {
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

        private final @NotNull Function<? super Mqtt3SubscribeView, P> parentConsumer;

        public Send(final @NotNull Function<? super Mqtt3SubscribeView, P> parentConsumer) {
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

    public static abstract class Publishes<P> extends Mqtt3SubscribeViewBuilder<Publishes<P>>
            implements Mqtt3SubscribeBuilder.Publishes.Start.Complete<P>, Mqtt3SubscribeBuilder.Publishes.Args<P> {

        protected boolean manualAcknowledgement;

        @Override
        protected @NotNull Publishes<P> self() {
            return this;
        }

        @Override
        public @NotNull Publishes<P> manualAcknowledgement(final boolean manualAcknowledgement) {
            this.manualAcknowledgement = manualAcknowledgement;
            return this;
        }
    }
}

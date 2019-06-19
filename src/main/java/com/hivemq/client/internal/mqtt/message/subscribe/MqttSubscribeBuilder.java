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

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public @NotNull B addSubscriptions(final @Nullable Collection<Mqtt5Subscription> subscriptions) {
        buildFirstSubscription();

        Checks.notNull(subscriptions, "Subscriptions");
        Checks.atLeastOneElement(subscriptions, "Subscriptions");

        subscriptionsBuilder.addAll(
                Checks.elementsNotNullAndNotImplemented(subscriptions, MqttSubscription.class, "Subscriptions"));
        return self();
    }

    public @NotNull B addSubscriptions(final @Nullable Mqtt5Subscription... subscriptions) {
        buildFirstSubscription();

        Checks.notNull(subscriptions, "Subscriptions");
        Checks.atLeastOneElement(subscriptions, "Subscriptions");

        //noinspection NullableProblems
        subscriptionsBuilder.addAll(
                Checks.elementsNotNullAndNotImplemented(subscriptions, MqttSubscription.class, "Subscriptions"));
        return self();
    }

    public @NotNull B addSubscriptions(final @Nullable Stream<Mqtt5Subscription> subscriptions) {
        buildFirstSubscription();

        Checks.notNull(subscriptions, "Subscriptions");

        final ImmutableList<MqttSubscription> subscriptionImmutableList =
                Checks.elementsNotNullAndNotImplemented(subscriptions, MqttSubscription.class, "Subscriptions");

        Checks.atLeastOneElement(subscriptionImmutableList, "Subscriptions");

        subscriptionsBuilder.addAll(subscriptionImmutableList);
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
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
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

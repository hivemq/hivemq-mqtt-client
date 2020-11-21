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

package com.hivemq.client.internal.mqtt.message.subscribe;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscriptionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttSubscriptionBuilder<B extends MqttSubscriptionBuilder<B>> {

    private @Nullable MqttTopicFilterImpl topicFilter;
    private @NotNull MqttQos maxQos = MqttSubscription.DEFAULT_QOS;
    private boolean noLocal = MqttSubscription.DEFAULT_NO_LOCAL;
    private @NotNull Mqtt5RetainHandling retainHandling = MqttSubscription.DEFAULT_RETAIN_HANDLING;
    private boolean retainAsPublished = MqttSubscription.DEFAULT_RETAIN_AS_PUBLISHED;

    MqttSubscriptionBuilder() {}

    MqttSubscriptionBuilder(final @NotNull MqttSubscription subscription) {
        topicFilter = subscription.getTopicFilter();
        maxQos = subscription.getMaxQos();
        noLocal = subscription.isNoLocal();
        retainHandling = subscription.getRetainHandling();
        retainAsPublished = subscription.isRetainAsPublished();
    }

    abstract @NotNull B self();

    public @NotNull B topicFilter(final @Nullable String topicFilter) {
        this.topicFilter = MqttTopicFilterImpl.of(topicFilter);
        return self();
    }

    public @NotNull B topicFilter(final @Nullable MqttTopicFilter topicFilter) {
        this.topicFilter = MqttChecks.topicFilter(topicFilter);
        return self();
    }

    public MqttTopicFilterImplBuilder.@NotNull Nested<B> topicFilterWith() {
        return new MqttTopicFilterImplBuilder.Nested<>(this::topicFilter);
    }

    public @NotNull B maxQos(final @Nullable MqttQos maxQos) {
        this.maxQos = Checks.notNull(maxQos, "Maximum QoS");
        return self();
    }

    public @NotNull B noLocal(final boolean noLocal) {
        this.noLocal = noLocal;
        return self();
    }

    public @NotNull B retainHandling(final @Nullable Mqtt5RetainHandling retainHandling) {
        this.retainHandling = Checks.notNull(retainHandling, "Retain handling");
        return self();
    }

    public @NotNull B retainAsPublished(final boolean retainAsPublished) {
        this.retainAsPublished = retainAsPublished;
        return self();
    }

    public @NotNull MqttSubscription build() {
        Checks.notNull(topicFilter, "Topic filter");
        Checks.state(
                !(topicFilter.isShared() && noLocal),
                "It is a Protocol Error to set no local to true on a Shared Subscription.");
        return new MqttSubscription(topicFilter, maxQos, noLocal, retainHandling, retainAsPublished);
    }

    public static class Default extends MqttSubscriptionBuilder<Default> implements Mqtt5SubscriptionBuilder.Complete {

        public Default() {}

        Default(final @NotNull MqttSubscription subscription) {
            super(subscription);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttSubscriptionBuilder<Nested<P>>
            implements Mqtt5SubscriptionBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super Mqtt5Subscription, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt5Subscription, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applySubscription() {
            return parentConsumer.apply(build());
        }
    }
}

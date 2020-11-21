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

package com.hivemq.client.internal.mqtt.message.subscribe.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscriptionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3SubscriptionViewBuilder<B extends Mqtt3SubscriptionViewBuilder<B>> {

    private @Nullable MqttTopicFilterImpl topicFilter;
    private @NotNull MqttQos maxQos = Mqtt3SubscriptionView.DEFAULT_QOS;

    Mqtt3SubscriptionViewBuilder() {}

    Mqtt3SubscriptionViewBuilder(final @NotNull Mqtt3SubscriptionView subscription) {
        final MqttSubscription delegate = subscription.getDelegate();
        topicFilter = delegate.getTopicFilter();
        maxQos = delegate.getMaxQos();
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

    public @NotNull Mqtt3SubscriptionView build() {
        Checks.notNull(topicFilter, "Topic filter");
        return Mqtt3SubscriptionView.of(topicFilter, maxQos);
    }

    public static class Default extends Mqtt3SubscriptionViewBuilder<Default>
            implements Mqtt3SubscriptionBuilder.Complete {

        public Default() {}

        Default(final @NotNull Mqtt3SubscriptionView subscription) {
            super(subscription);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3SubscriptionViewBuilder<Nested<P>>
            implements Mqtt3SubscriptionBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super Mqtt3Subscription, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3Subscription, P> parentConsumer) {
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

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

package org.mqttbee.mqtt.message.subscribe;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscriptionBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttSubscriptionBuilder<B extends MqttSubscriptionBuilder<B>> {

    private @Nullable MqttTopicFilterImpl topicFilter;
    private @NotNull MqttQos qos = Mqtt5Subscription.DEFAULT_QOS;
    private boolean noLocal = Mqtt5Subscription.DEFAULT_NO_LOCAL;
    private @NotNull Mqtt5RetainHandling retainHandling = Mqtt5Subscription.DEFAULT_RETAIN_HANDLING;
    private boolean retainAsPublished = Mqtt5Subscription.DEFAULT_RETAIN_AS_PUBLISHED;

    abstract @NotNull B self();

    public @NotNull B topicFilter(final @NotNull String topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return self();
    }

    public @NotNull B topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return self();
    }

    public @NotNull MqttTopicFilterBuilder<B> topicFilter() {
        return new MqttTopicFilterBuilder<>(this::topicFilter);
    }

    public @NotNull B qos(final @NotNull MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos, "QoS must not be null.");
        return self();
    }

    public @NotNull B noLocal(final boolean noLocal) {
        this.noLocal = noLocal;
        return self();
    }

    public @NotNull B retainHandling(final @NotNull Mqtt5RetainHandling retainHandling) {
        this.retainHandling = Preconditions.checkNotNull(retainHandling, "Retain handling must not be null.");
        return self();
    }

    public @NotNull B retainAsPublished(final boolean retainAsPublished) {
        this.retainAsPublished = retainAsPublished;
        return self();
    }

    public @NotNull MqttSubscription build() {
        Objects.requireNonNull(topicFilter, "Topic filter must not be null.");
        if (topicFilter.isShared() && noLocal) {
            throw new IllegalStateException("It is a Protocol Error to set no local to true on a Shared Subscription.");
        }
        return new MqttSubscription(topicFilter, qos, noLocal, retainHandling, retainAsPublished);
    }

    // @formatter:off
    public static class Default
            extends MqttSubscriptionBuilder<Default>
            implements Mqtt5SubscriptionBuilder,
                       Mqtt5SubscriptionBuilder.Complete {
    // @formatter:on

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    // @formatter:off
    public static class Nested<P>
            extends MqttSubscriptionBuilder<Nested<P>>
            implements Mqtt5SubscriptionBuilder.Nested<P>,
                       Mqtt5SubscriptionBuilder.Nested.Complete<P> {
    // @formatter:on

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

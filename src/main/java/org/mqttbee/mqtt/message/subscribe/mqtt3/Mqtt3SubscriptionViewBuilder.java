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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscriptionBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3SubscriptionViewBuilder<B extends Mqtt3SubscriptionViewBuilder<B>> {

    private @Nullable MqttTopicFilterImpl topicFilter;
    private @NotNull MqttQos qos = Mqtt3Subscription.DEFAULT_QOS;

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
        this.qos = Objects.requireNonNull(qos, "QoS must not be null.");
        return self();
    }

    public @NotNull Mqtt3SubscriptionView build() {
        Objects.requireNonNull(topicFilter, "Topic filter must be given.");
        return Mqtt3SubscriptionView.of(topicFilter, qos);
    }

    // @formatter:off
    public static class Default
            extends Mqtt3SubscriptionViewBuilder<Default>
            implements Mqtt3SubscriptionBuilder,
                       Mqtt3SubscriptionBuilder.Complete {
    // @formatter:on

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    // @formatter:off
    public static class Nested<P>
            extends Mqtt3SubscriptionViewBuilder<Nested<P>>
            implements Mqtt3SubscriptionBuilder.Nested<P>,
                       Mqtt3SubscriptionBuilder.Nested.Complete<P> {
    // @formatter:on

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

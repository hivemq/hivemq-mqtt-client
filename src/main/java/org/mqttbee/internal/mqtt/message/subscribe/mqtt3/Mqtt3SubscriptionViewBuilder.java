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

package org.mqttbee.internal.mqtt.message.subscribe.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.mqttbee.internal.mqtt.util.MqttChecks;
import org.mqttbee.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.mqtt3.message.subscribe.Mqtt3SubscriptionBuilder;
import org.mqttbee.util.Checks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3SubscriptionViewBuilder<B extends Mqtt3SubscriptionViewBuilder<B>> {

    private @Nullable MqttTopicFilterImpl topicFilter;
    private @NotNull MqttQos qos = Mqtt3SubscriptionView.DEFAULT_QOS;

    abstract @NotNull B self();

    public @NotNull B topicFilter(final @Nullable String topicFilter) {
        this.topicFilter = MqttTopicFilterImpl.of(topicFilter);
        return self();
    }

    public @NotNull B topicFilter(final @Nullable MqttTopicFilter topicFilter) {
        this.topicFilter = MqttChecks.topicFilter(topicFilter);
        return self();
    }

    public @NotNull MqttTopicFilterImplBuilder.Nested<B> topicFilter() {
        return new MqttTopicFilterImplBuilder.Nested<>(this::topicFilter);
    }

    public @NotNull B qos(final @Nullable MqttQos qos) {
        this.qos = Checks.notNull(qos, "QoS");
        return self();
    }

    public @NotNull Mqtt3SubscriptionView build() {
        Checks.notNull(topicFilter, "Topic filter");
        return Mqtt3SubscriptionView.of(topicFilter, qos);
    }

    public static class Default extends Mqtt3SubscriptionViewBuilder<Default>
            implements Mqtt3SubscriptionBuilder.Complete {

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

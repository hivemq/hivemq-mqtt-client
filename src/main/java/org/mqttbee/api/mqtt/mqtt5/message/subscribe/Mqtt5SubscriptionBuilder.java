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

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscriptionBuilder<P> extends FluentBuilder<Mqtt5Subscription, P> {

    private @Nullable MqttTopicFilterImpl topicFilter;
    private @NotNull MqttQos qos = Mqtt5Subscription.DEFAULT_QOS;
    private boolean noLocal = Mqtt5Subscription.DEFAULT_NO_LOCAL;
    private @NotNull Mqtt5RetainHandling retainHandling = Mqtt5Subscription.DEFAULT_RETAIN_HANDLING;
    private boolean retainAsPublished = Mqtt5Subscription.DEFAULT_RETAIN_AS_PUBLISHED;

    public Mqtt5SubscriptionBuilder(final @Nullable Function<? super Mqtt5Subscription, P> parentConsumer) {
        super(parentConsumer);
    }

    public @NotNull Mqtt5SubscriptionBuilder<P> topicFilter(final @NotNull String topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    public @NotNull Mqtt5SubscriptionBuilder<P> topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<Mqtt5SubscriptionBuilder<P>> topicFilter() {
        return new MqttTopicFilterBuilder<>(this::topicFilter);
    }

    public @NotNull Mqtt5SubscriptionBuilder<P> qos(final @NotNull MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos, "QoS must not be null.");
        return this;
    }

    public @NotNull Mqtt5SubscriptionBuilder<P> noLocal(final boolean noLocal) {
        this.noLocal = noLocal;
        return this;
    }

    public @NotNull Mqtt5SubscriptionBuilder<P> retainHandling(final @NotNull Mqtt5RetainHandling retainHandling) {
        this.retainHandling = Preconditions.checkNotNull(retainHandling, "Retain handling must not be null.");
        return this;
    }

    public @NotNull Mqtt5SubscriptionBuilder<P> retainAsPublished(final boolean retainAsPublished) {
        this.retainAsPublished = retainAsPublished;
        return this;
    }

    @Override
    public @NotNull Mqtt5Subscription build() {
        Preconditions.checkNotNull(topicFilter, "Topic filter must not be null.");
        Preconditions.checkArgument(
                !(topicFilter.isShared() && noLocal),
                "It is a Protocol Error to set no local to true on a Shared Subscription.");
        return new MqttSubscription(topicFilter, qos, noLocal, retainHandling, retainAsPublished);
    }

    public @NotNull P applySubscription() {
        return apply();
    }
}

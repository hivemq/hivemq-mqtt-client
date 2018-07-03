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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
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

    private MqttTopicFilterImpl topicFilter;
    private MqttQos qos;
    private boolean noLocal = Mqtt5Subscription.DEFAULT_NO_LOCAL;
    private Mqtt5RetainHandling retainHandling = Mqtt5Subscription.DEFAULT_RETAIN_HANDLING;
    private boolean retainAsPublished = Mqtt5Subscription.DEFAULT_RETAIN_AS_PUBLISHED;

    public Mqtt5SubscriptionBuilder(@Nullable final Function<? super Mqtt5Subscription, P> parentConsumer) {
        super(parentConsumer);
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<P> topicFilter(@NotNull final String topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<P> topicFilter(@NotNull final MqttTopicFilter topicFilter) {
        this.topicFilter = MqttBuilderUtil.topicFilter(topicFilter);
        return this;
    }

    @NotNull
    public MqttTopicFilterBuilder<? extends Mqtt5SubscriptionBuilder<P>> topicFilter() {
        return new MqttTopicFilterBuilder<>("", this::topicFilter);
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<P> qos(@NotNull final MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<P> noLocal(final boolean noLocal) {
        this.noLocal = noLocal;
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<P> retainHandling(@NotNull final Mqtt5RetainHandling retainHandling) {
        this.retainHandling = Preconditions.checkNotNull(retainHandling);
        return this;
    }

    @NotNull
    public Mqtt5SubscriptionBuilder<P> retainAsPublished(final boolean retainAsPublished) {
        this.retainAsPublished = retainAsPublished;
        return this;
    }

    @NotNull
    @Override
    public Mqtt5Subscription build() {
        Preconditions.checkNotNull(topicFilter);
        Preconditions.checkNotNull(qos);
        Preconditions.checkArgument(!(topicFilter.isShared() && noLocal));
        return new MqttSubscription(topicFilter, qos, noLocal, retainHandling, retainAsPublished);
    }

}

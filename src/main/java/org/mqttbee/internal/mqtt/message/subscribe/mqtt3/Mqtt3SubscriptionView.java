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
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.internal.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscriptionView implements Mqtt3Subscription {

    private static @NotNull MqttSubscription delegate(
            final @NotNull MqttTopicFilterImpl topicFilter, final @NotNull MqttQos qos) {

        return new MqttSubscription(topicFilter, qos, false, Mqtt5RetainHandling.SEND, false);
    }

    static @NotNull Mqtt3SubscriptionView of(
            final @NotNull MqttTopicFilterImpl topicFilter, final @NotNull MqttQos qos) {

        return new Mqtt3SubscriptionView(delegate(topicFilter, qos));
    }

    static @NotNull Mqtt3SubscriptionView of(final @NotNull MqttSubscription delegate) {
        return new Mqtt3SubscriptionView(delegate);
    }

    private final @NotNull MqttSubscription delegate;

    private Mqtt3SubscriptionView(final @NotNull MqttSubscription delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull MqttTopicFilter getTopicFilter() {
        return delegate.getTopicFilter();
    }

    @Override
    public @NotNull MqttQos getQos() {
        return delegate.getQos();
    }

    public @NotNull MqttSubscription getDelegate() {
        return delegate;
    }

    private @NotNull String toAttributeString() {
        return "topicFilter=" + getTopicFilter() + ", qos=" + getQos();
    }

    @Override
    public @NotNull String toString() {
        return "MqttSubscription{" + toAttributeString() + '}';
    }
}

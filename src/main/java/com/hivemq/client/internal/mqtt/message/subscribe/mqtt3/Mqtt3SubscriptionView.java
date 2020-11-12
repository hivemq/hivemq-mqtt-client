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
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscriptionView implements Mqtt3Subscription {

    private static @NotNull MqttSubscription delegate(
            final @NotNull MqttTopicFilterImpl topicFilter, final @NotNull MqttQos maxQos) {

        return new MqttSubscription(topicFilter, maxQos, false, Mqtt5RetainHandling.SEND, false);
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
    public @NotNull MqttQos getMaxQos() {
        return delegate.getMaxQos();
    }

    public @NotNull MqttSubscription getDelegate() {
        return delegate;
    }

    @Override
    public Mqtt3SubscriptionViewBuilder.@NotNull Default extend() {
        return new Mqtt3SubscriptionViewBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        return "topicFilter=" + getTopicFilter() + ", maxQos=" + getMaxQos();
    }

    @Override
    public @NotNull String toString() {
        return "MqttSubscription{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt3SubscriptionView)) {
            return false;
        }
        final Mqtt3SubscriptionView that = (Mqtt3SubscriptionView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}

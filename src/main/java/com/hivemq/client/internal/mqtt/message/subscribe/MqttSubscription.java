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
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttSubscription implements Mqtt5Subscription {

    private final @NotNull MqttTopicFilterImpl topicFilter;
    private final @NotNull MqttQos maxQos;
    private final boolean noLocal;
    private final @NotNull Mqtt5RetainHandling retainHandling;
    private final boolean retainAsPublished;

    public MqttSubscription(
            final @NotNull MqttTopicFilterImpl topicFilter,
            final @NotNull MqttQos maxQos,
            final boolean noLocal,
            final @NotNull Mqtt5RetainHandling retainHandling,
            final boolean retainAsPublished) {

        this.topicFilter = topicFilter;
        this.maxQos = maxQos;
        this.noLocal = noLocal;
        this.retainHandling = retainHandling;
        this.retainAsPublished = retainAsPublished;
    }

    @Override
    public @NotNull MqttTopicFilterImpl getTopicFilter() {
        return topicFilter;
    }

    @Override
    public @NotNull MqttQos getMaxQos() {
        return maxQos;
    }

    @Override
    public boolean isNoLocal() {
        return noLocal;
    }

    @Override
    public @NotNull Mqtt5RetainHandling getRetainHandling() {
        return retainHandling;
    }

    @Override
    public boolean isRetainAsPublished() {
        return retainAsPublished;
    }

    @Override
    public MqttSubscriptionBuilder.@NotNull Default extend() {
        return new MqttSubscriptionBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        return "topicFilter=" + topicFilter + ", maxQos=" + maxQos + ", noLocal=" + noLocal + ", retainHandling=" +
                retainHandling + ", retainAsPublished=" + retainAsPublished;
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
        if (!(o instanceof MqttSubscription)) {
            return false;
        }
        final MqttSubscription that = (MqttSubscription) o;

        return topicFilter.equals(that.topicFilter) && (maxQos == that.maxQos) && (noLocal == that.noLocal) &&
                (retainHandling == that.retainHandling) && (retainAsPublished == that.retainAsPublished);
    }

    @Override
    public int hashCode() {
        int result = topicFilter.hashCode();
        result = 31 * result + maxQos.hashCode();
        result = 31 * result + Boolean.hashCode(noLocal);
        result = 31 * result + retainHandling.hashCode();
        result = 31 * result + Boolean.hashCode(retainAsPublished);
        return result;
    }

    public byte encodeSubscriptionOptions() {
        byte subscriptionOptions = 0;
        subscriptionOptions |= retainHandling.getCode() << 4;
        if (retainAsPublished) {
            subscriptionOptions |= 0b0000_1000;
        }
        if (noLocal) {
            subscriptionOptions |= 0b0000_0100;
        }
        subscriptionOptions |= maxQos.getCode();
        return subscriptionOptions;
    }

    public static @Nullable MqttQos decodeMaxQos(final byte subscriptionOptions) {
        return MqttQos.fromCode(subscriptionOptions & 0b0000_0011);
    }

    public static boolean decodeNoLocal(final byte subscriptionOptions) {
        return (subscriptionOptions & 0b0000_0100) != 0;
    }

    public static @Nullable Mqtt5RetainHandling decodeRetainHandling(final byte subscriptionOptions) {
        return Mqtt5RetainHandling.fromCode((subscriptionOptions & 0b0011_0000) >> 4);
    }

    public static boolean decodeRetainAsPublished(final byte subscriptionOptions) {
        return (subscriptionOptions & 0b0000_1000) != 0;
    }
}

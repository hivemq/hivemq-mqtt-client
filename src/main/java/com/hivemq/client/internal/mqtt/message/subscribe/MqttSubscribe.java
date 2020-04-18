/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.subscribe;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client.internal.util.StringUtil;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubscribe extends MqttMessageWithUserProperties implements Mqtt5Subscribe {

    private final @NotNull ImmutableList<MqttSubscription> subscriptions;
    private final boolean manualAcknowledgement;

    public MqttSubscribe(
            final @NotNull ImmutableList<MqttSubscription> subscriptions,
            final @NotNull MqttUserPropertiesImpl userProperties, final boolean manualAcknowledgement) {

        super(userProperties);
        this.subscriptions = subscriptions;
        this.manualAcknowledgement = manualAcknowledgement;
    }

    @Override
    public @NotNull ImmutableList<MqttSubscription> getSubscriptions() {
        return subscriptions;
    }

    @Override
    public boolean isManualAcknowledgement() {
        return manualAcknowledgement;
    }

    public @NotNull MqttStatefulSubscribe createStateful(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttStatefulSubscribe(this, packetIdentifier, subscriptionIdentifier);
    }

    @Override
    public @NotNull MqttSubscribeBuilder.Default extend() {
        return new MqttSubscribeBuilder.Default(this);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "subscriptions=" + subscriptions + StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttSubscribe{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttSubscribe)) {
            return false;
        }
        final MqttSubscribe that = (MqttSubscribe) o;

        return partialEquals(that) && subscriptions.equals(that.subscriptions);
    }

    @Override
    public int hashCode() {
        return 31 * partialHashCode() + subscriptions.hashCode();
    }
}

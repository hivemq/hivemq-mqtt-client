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

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class Mqtt3SubscribeView implements Mqtt3Subscribe {

    private static @NotNull MqttSubscribe delegate(final @NotNull ImmutableList<MqttSubscription> subscriptions) {
        return new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    static @NotNull Mqtt3SubscribeView of(final @NotNull ImmutableList<MqttSubscription> subscriptions) {
        return new Mqtt3SubscribeView(delegate(subscriptions));
    }

    public static @NotNull Mqtt3SubscribeView of(final @NotNull MqttSubscribe delegate) {
        return new Mqtt3SubscribeView(delegate);
    }

    private final @NotNull MqttSubscribe delegate;

    private Mqtt3SubscribeView(final @NotNull MqttSubscribe delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull ImmutableList<Mqtt3SubscriptionView> getSubscriptions() {
        final ImmutableList<MqttSubscription> subscriptions = delegate.getSubscriptions();
        final ImmutableList.Builder<Mqtt3SubscriptionView> builder = ImmutableList.builder(subscriptions.size());
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.size(); i++) {
            builder.add(Mqtt3SubscriptionView.of(subscriptions.get(i)));
        }
        return builder.build();
    }

    public @NotNull MqttSubscribe getDelegate() {
        return delegate;
    }

    @Override
    public Mqtt3SubscribeViewBuilder.@NotNull Default extend() {
        return new Mqtt3SubscribeViewBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        return "subscriptions=" + getSubscriptions();
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
        if (!(o instanceof Mqtt3SubscribeView)) {
            return false;
        }
        final Mqtt3SubscribeView that = (Mqtt3SubscribeView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}

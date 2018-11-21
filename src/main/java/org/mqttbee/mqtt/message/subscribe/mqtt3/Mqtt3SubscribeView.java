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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

/**
 * @author Silvio Giebl
 */
@Immutable
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
    public @NotNull ImmutableList<@NotNull Mqtt3SubscriptionView> getSubscriptions() {
        final ImmutableList<MqttSubscription> subscriptions = delegate.getSubscriptions();
        final ImmutableList.Builder<Mqtt3SubscriptionView> builder =
                ImmutableList.builderWithExpectedSize(subscriptions.size());
        for (int i = 0; i < subscriptions.size(); i++) {
            builder.add(Mqtt3SubscriptionView.of(subscriptions.get(i)));
        }
        return builder.build();
    }

    public @NotNull MqttSubscribe getDelegate() {
        return delegate;
    }
}

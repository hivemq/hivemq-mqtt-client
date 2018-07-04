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
import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

/** @author Silvio Giebl */
@Immutable
public class Mqtt3SubscribeView implements Mqtt3Subscribe {

    @NotNull
    private static MqttSubscribe delegate(
            @NotNull final ImmutableList<MqttSubscription> subscriptions) {
        return new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    public static Mqtt3SubscribeView of(
            @NotNull final ImmutableList<MqttSubscription> subscriptions) {
        return new Mqtt3SubscribeView(delegate(subscriptions));
    }

    @NotNull
    public static Mqtt3SubscribeView of(@NotNull final MqttSubscribe delegate) {
        return new Mqtt3SubscribeView(delegate);
    }

    private final MqttSubscribe delegate;

    private Mqtt3SubscribeView(@NotNull final MqttSubscribe delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public ImmutableList<? extends Mqtt3Subscription> getSubscriptions() {
        final ImmutableList<MqttSubscription> subscriptions = delegate.getSubscriptions();
        final ImmutableList.Builder<Mqtt3SubscriptionView> builder =
                ImmutableList.builderWithExpectedSize(subscriptions.size());
        for (int i = 0; i < subscriptions.size(); i++) {
            builder.add(Mqtt3SubscriptionView.of(subscriptions.get(i)));
        }
        return builder.build();
    }

    @NotNull
    public MqttSubscribe getDelegate() {
        return delegate;
    }
}

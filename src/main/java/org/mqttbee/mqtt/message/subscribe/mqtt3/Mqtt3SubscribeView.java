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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3SubscribeView implements Mqtt3Subscribe {

    public static MqttSubscribe wrapped(
            @NotNull final ImmutableList<MqttSubscription> subscriptions) {

        return new MqttSubscribe(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    private static ImmutableList<Mqtt3SubscriptionView> wrapSubscriptions(
            @NotNull final ImmutableList<MqttSubscription> subscriptions) {

        final ImmutableList.Builder<Mqtt3SubscriptionView> builder =
                ImmutableList.builderWithExpectedSize(subscriptions.size());
        for (int i = 0; i < subscriptions.size(); i++) {
            builder.add(new Mqtt3SubscriptionView(subscriptions.get(i)));
        }
        return builder.build();
    }

    public static Mqtt3SubscribeView create(
            @NotNull final ImmutableList<MqttSubscription> subscriptions) {

        return new Mqtt3SubscribeView((wrapped(subscriptions)));
    }

    private final MqttSubscribe wrapped;

    private Mqtt3SubscribeView(@NotNull final MqttSubscribe wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public ImmutableList<? extends Mqtt3Subscription> getSubscriptions() {
        return wrapSubscriptions(wrapped.getSubscriptions());
    }

    @NotNull
    public MqttSubscribe getWrapped() {
        return wrapped;
    }

}

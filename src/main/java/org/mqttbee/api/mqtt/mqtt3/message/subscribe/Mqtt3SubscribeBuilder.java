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

package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscriptionView;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscribeBuilder {

    private final ImmutableList.Builder<MqttSubscription> subscriptionBuilder = ImmutableList.builder();

    Mqtt3SubscribeBuilder() {
    }

    @NotNull
    public Mqtt3SubscribeBuilder addSubscription(@NotNull final Mqtt3Subscription subscription) {
        final Mqtt3SubscriptionView subscriptionView =
                MustNotBeImplementedUtil.checkNotImplemented(subscription, Mqtt3SubscriptionView.class);
        subscriptionBuilder.add(subscriptionView.getDelegate());
        return this;
    }

    @NotNull
    public Mqtt3Subscribe build() {
        final ImmutableList<MqttSubscription> subscriptions = subscriptionBuilder.build();
        Preconditions.checkState(!subscriptions.isEmpty());
        return Mqtt3SubscribeView.of(subscriptions);
    }

}

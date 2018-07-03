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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SubscriptionView implements Mqtt3Subscription {

    @NotNull
    private static MqttSubscription delegate(
            @NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttQos qos) {
        return new MqttSubscription(topicFilter, qos, false, Mqtt5RetainHandling.SEND, false);
    }

    @NotNull
    public static Mqtt3SubscriptionView of(@NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttQos qos) {
        return new Mqtt3SubscriptionView(delegate(topicFilter, qos));
    }

    @NotNull
    static Mqtt3SubscriptionView of(@NotNull final MqttSubscription delegate) {
        return new Mqtt3SubscriptionView(delegate);
    }

    private final MqttSubscription delegate;

    private Mqtt3SubscriptionView(@NotNull final MqttSubscription delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public MqttTopicFilter getTopicFilter() {
        return delegate.getTopicFilter();
    }

    @NotNull
    @Override
    public MqttQos getQos() {
        return delegate.getQos();
    }

    @NotNull
    public MqttSubscription getDelegate() {
        return delegate;
    }

}

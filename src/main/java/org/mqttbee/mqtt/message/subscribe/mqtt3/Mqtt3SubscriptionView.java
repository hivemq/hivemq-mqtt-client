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
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
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
    public static MqttSubscription wrapped(
            @NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttQoS qos) {

        return new MqttSubscription(topicFilter, qos, false, Mqtt5RetainHandling.SEND, false);
    }

    @NotNull
    public static Mqtt3SubscriptionView create(
            @NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttQoS qos) {

        return new Mqtt3SubscriptionView(wrapped(topicFilter, qos));
    }

    private final MqttSubscription wrapped;

    Mqtt3SubscriptionView(@NotNull final MqttSubscription wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public MqttTopicFilter getTopicFilter() {
        return wrapped.getTopicFilter();
    }

    @NotNull
    @Override
    public MqttQoS getQoS() {
        return wrapped.getQoS();
    }

    @NotNull
    public MqttSubscription getWrapped() {
        return wrapped;
    }

}

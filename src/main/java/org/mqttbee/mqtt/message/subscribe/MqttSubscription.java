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

package org.mqttbee.mqtt.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubscription implements Mqtt5Subscription {

    private final MqttTopicFilterImpl topicFilter;
    private final MqttQoS qos;
    private final boolean isNoLocal;
    private final Mqtt5RetainHandling retainHandling;
    private final boolean isRetainAsPublished;

    public MqttSubscription(
            @NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttQoS qos, final boolean isNoLocal,
            @NotNull final Mqtt5RetainHandling retainHandling, final boolean isRetainAsPublished) {

        this.topicFilter = topicFilter;
        this.qos = qos;
        this.isNoLocal = isNoLocal;
        this.retainHandling = retainHandling;
        this.isRetainAsPublished = isRetainAsPublished;
    }

    @NotNull
    @Override
    public MqttTopicFilterImpl getTopicFilter() {
        return topicFilter;
    }

    @NotNull
    @Override
    public MqttQoS getQoS() {
        return qos;
    }

    @Override
    public boolean isNoLocal() {
        return isNoLocal;
    }

    @NotNull
    @Override
    public Mqtt5RetainHandling getRetainHandling() {
        return retainHandling;
    }

    @Override
    public boolean isRetainAsPublished() {
        return isRetainAsPublished;
    }

}

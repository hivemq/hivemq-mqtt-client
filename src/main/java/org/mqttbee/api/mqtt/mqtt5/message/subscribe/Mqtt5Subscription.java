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

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;

/**
 * Subscription in the MQTT 5 SUBSCRIBE packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Subscription {

    /**
     * The default for whether the client must not receive messages published by itself.
     */
    boolean DEFAULT_NO_LOCAL = false;
    /**
     * The default handling of retained message.
     */
    @NotNull
    Mqtt5RetainHandling DEFAULT_RETAIN_HANDLING = Mqtt5RetainHandling.SEND;
    /**
     * The default for whether the retain flag for incoming publishes must be set to its original value.
     */
    boolean DEFAULT_RETAIN_AS_PUBLISHED = false;

    @NotNull
    static Mqtt5SubscriptionBuilder<Void> builder() {
        return new Mqtt5SubscriptionBuilder<>(null);
    }

    /**
     * @return the Topic Filter of this subscription.
     */
    @NotNull
    MqttTopicFilter getTopicFilter();

    /**
     * @return the QoS of this subscription.
     */
    @NotNull
    MqttQos getQos();

    /**
     * @return whether the client must not receive messages published by itself. The default is {@link
     * #DEFAULT_NO_LOCAL}.
     */
    boolean isNoLocal();

    /**
     * @return the handling of retained message for this subscription. The default is {@link
     * #DEFAULT_RETAIN_HANDLING}.
     */
    @NotNull
    Mqtt5RetainHandling getRetainHandling();

    /**
     * @return whether the retain flag for incoming publishes must be set to its original value.
     */
    boolean isRetainAsPublished();

}

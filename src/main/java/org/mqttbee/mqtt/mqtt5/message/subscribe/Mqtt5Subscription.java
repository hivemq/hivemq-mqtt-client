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

package org.mqttbee.mqtt.mqtt5.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.internal.mqtt.message.subscribe.MqttSubscriptionBuilder;
import org.mqttbee.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.datatypes.MqttTopicFilter;

/**
 * Subscription in a {@link Mqtt5Subscribe MQTT 5 Subscribe message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5Subscription {

    /**
     * Default {@link MqttQos QoS} level of a Subscription. It is chosen as {@link MqttQos#EXACTLY_ONCE} as this leeds
     * to subscribed Publish messages being delivered with its initial {@link MqttQos QoS} level.
     */
    @NotNull MqttQos DEFAULT_QOS = MqttQos.EXACTLY_ONCE;

    /**
     * The default for whether the client must not receive messages published by itself.
     */
    boolean DEFAULT_NO_LOCAL = false;
    /**
     * The default handling of retained message.
     */
    @NotNull Mqtt5RetainHandling DEFAULT_RETAIN_HANDLING = Mqtt5RetainHandling.SEND;
    /**
     * The default for whether the retain flag for incoming Publish messages must be set to its original value.
     */
    boolean DEFAULT_RETAIN_AS_PUBLISHED = false;

    /**
     * Creatse a builder for a Subscription.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5SubscriptionBuilder builder() {
        return new MqttSubscriptionBuilder.Default();
    }

    /**
     * @return the Topic Filter of this Subscription.
     */
    @NotNull MqttTopicFilter getTopicFilter();

    /**
     * @return the QoS of this Subscription.
     */
    @NotNull MqttQos getQos();

    /**
     * @return whether the client must not receive messages published by itself. The default is {@link
     *         #DEFAULT_NO_LOCAL}.
     */
    boolean isNoLocal();

    /**
     * @return the handling of retained message for this Subscription. The default is {@link #DEFAULT_RETAIN_HANDLING}.
     */
    @NotNull Mqtt5RetainHandling getRetainHandling();

    /**
     * @return whether the retain flag for incoming Publish messages must be set to its original value.
     */
    boolean isRetainAsPublished();
}

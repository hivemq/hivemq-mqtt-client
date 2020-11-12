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

package com.hivemq.client.mqtt.mqtt3.message.subscribe;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscriptionViewBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.jetbrains.annotations.NotNull;

/**
 * Subscription in an {@link Mqtt3Subscribe MQTT 3 Subscribe message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3Subscription {

    /**
     * Default {@link MqttQos QoS} level of a Subscription. It is chosen as {@link MqttQos#EXACTLY_ONCE} as this leeds
     * to subscribed Publish messages being delivered with its initial {@link MqttQos QoS} level.
     */
    @NotNull MqttQos DEFAULT_QOS = MqttQos.EXACTLY_ONCE;

    /**
     * Creates a builder for a Subscription.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt3SubscriptionBuilder builder() {
        return new Mqtt3SubscriptionViewBuilder.Default();
    }

    /**
     * @return the Topic Filter of this Subscription.
     */
    @NotNull MqttTopicFilter getTopicFilter();

    /**
     * @return the maximum QoS of this Subscription.
     */
    @NotNull MqttQos getMaxQos();

    /**
     * Creates a builder for extending this Subscription.
     *
     * @return the created builder.
     * @since 1.1
     */
    @NotNull Mqtt3SubscriptionBuilder extend();
}

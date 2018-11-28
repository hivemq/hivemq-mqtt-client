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

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt3SubscriptionBuilderBase<C extends Mqtt3SubscriptionBuilderBase.Complete<C>> {

    @NotNull C topicFilter(@NotNull String topicFilter);

    @NotNull C topicFilter(@NotNull MqttTopicFilter topicFilter);

    @NotNull MqttTopicFilterBuilder.Nested<? extends C> topicFilter();

    @DoNotImplement
    interface Complete<C extends Mqtt3SubscriptionBuilderBase.Complete<C>> extends Mqtt3SubscriptionBuilderBase<C> {

        @NotNull C qos(@NotNull MqttQos qos);
    }
}

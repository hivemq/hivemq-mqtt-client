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

package org.mqttbee.mqtt.mqtt3.message.unsubscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt3UnsubscribeBuilderBase<C extends Mqtt3UnsubscribeBuilderBase<C>> {

    @NotNull C addTopicFilter(@NotNull String topicFilter);

    @NotNull C addTopicFilter(@NotNull MqttTopicFilter topicFilter);

    @NotNull MqttTopicFilterBuilder.Nested<? extends C> addTopicFilter();

    @NotNull C reverse(@NotNull Mqtt3Subscribe subscribe);

    @DoNotImplement
    interface Start<C extends Mqtt3UnsubscribeBuilderBase<C>> extends Mqtt3UnsubscribeBuilderBase<C> {

        @NotNull C topicFilter(@NotNull String topicFilter);

        @NotNull C topicFilter(@NotNull MqttTopicFilter topicFilter);

        @NotNull MqttTopicFilterBuilder.Nested<? extends C> topicFilter();
    }
}

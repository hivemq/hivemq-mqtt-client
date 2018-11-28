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

package org.mqttbee.api.mqtt.mqtt3.message.publish;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicBuilder;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt3PublishBuilderBase<C extends Mqtt3PublishBuilderBase.Complete<C>> {

    @NotNull C topic(@NotNull String topic);

    @NotNull C topic(@NotNull MqttTopic topic);

    @NotNull MqttTopicBuilder.Nested<? extends C> topic();

    @DoNotImplement
    interface Complete<C extends Mqtt3PublishBuilderBase.Complete<C>> extends Mqtt3PublishBuilderBase<C> {

        @NotNull C payload(@Nullable byte[] payload);

        @NotNull C payload(@Nullable ByteBuffer payload);

        @NotNull C qos(@NotNull MqttQos qos);

        @NotNull C retain(boolean retain);
    }
}

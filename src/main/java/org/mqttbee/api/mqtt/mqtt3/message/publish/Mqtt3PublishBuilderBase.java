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
// @formatter:off
@DoNotImplement
public interface Mqtt3PublishBuilderBase<
            B extends Mqtt3PublishBuilderBase<B, C>,
            C extends B> {
// @formatter:on

    @NotNull C topic(final @NotNull String topic);

    @NotNull C topic(final @NotNull MqttTopic topic);

    @NotNull MqttTopicBuilder<? extends C> topic();

    @NotNull B payload(final @Nullable byte[] payload);

    @NotNull B payload(final @Nullable ByteBuffer payload);

    @NotNull B qos(final @NotNull MqttQos qos);

    @NotNull B retain(final boolean retain);

    // @formatter:off
    @DoNotImplement
    interface Complete<
                B extends Mqtt3PublishBuilderBase<B, C>,
                C extends B>
            extends Mqtt3PublishBuilderBase<B, C> {
    // @formatter:on

        @Override
        @NotNull C payload(final @Nullable byte[] payload);

        @Override
        @NotNull C payload(final @Nullable ByteBuffer payload);

        @Override
        @NotNull C qos(final @NotNull MqttQos qos);

        @Override
        @NotNull C retain(final boolean retain);
    }
}

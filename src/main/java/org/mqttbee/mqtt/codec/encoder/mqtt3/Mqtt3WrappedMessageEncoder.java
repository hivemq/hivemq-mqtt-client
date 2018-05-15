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

package org.mqttbee.mqtt.codec.encoder.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3WrappedMessageEncoder<M extends MqttWrappedMessage<M, W, ?>, W extends MqttMessageWrapper<W, M, ?>>
        extends Mqtt3MessageEncoder<W> implements MqttWrappedMessageEncoder<M, W> {

    @NotNull
    @Override
    public final MqttWrappedMessageEncoder<M, W> apply(@NotNull final M message) {
        return this;
    }

    @NotNull
    @Override
    public final MqttMessageEncoder wrap(@NotNull final W wrapper) {
        apply(wrapper);
        return this;
    }

}

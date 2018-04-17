/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Provider and applier for a {@link MqttMessageEncoder} for a {@link MqttMessageWrapper}.
 *
 * @param <W> the type of the MQTT message wrapper.
 * @param <M> the type of the wrapped MQTT message.
 * @param <P> the type of the encoder provider of the MQTT message wrapper.
 * @author Silvio Giebl
 */
public class MqttMessageWrapperEncoderProvider< //
        W extends MqttMessageWrapper<W, M, P>, //
        M extends MqttWrappedMessage<M, W, P>, //
        P extends MqttMessageEncoderProvider<W>>
        implements MqttMessageEncoderProvider<W>, MqttMessageEncoderApplier<W> {

    @Override
    public MqttMessageEncoderApplier<W> get() {
        return this;
    }

    @NotNull
    @Override
    public MqttMessageEncoder apply(@NotNull final W message) {
        return message.getWrapped().getEncoder().wrap(message);
    }

}

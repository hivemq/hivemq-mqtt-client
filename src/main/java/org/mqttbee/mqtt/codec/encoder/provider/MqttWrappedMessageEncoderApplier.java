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
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Applies a {@link MqttWrappedMessage} to a {@link MqttWrappedMessageEncoder}.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @author Silvio Giebl
 */
public interface MqttWrappedMessageEncoderApplier<M extends MqttWrappedMessage<M, W, ?>, W extends MqttMessageWrapper<W, M, ?>> {

    /**
     * Returns a encoder for a wrapped MQTT message that is applied to the given wrapped MQTT message.
     *
     * @param message the wrapped MQTT message.
     * @return the encoder for the wrapped MQTT message.
     */
    @NotNull
    MqttWrappedMessageEncoder<M, W> apply(@NotNull M message);

}

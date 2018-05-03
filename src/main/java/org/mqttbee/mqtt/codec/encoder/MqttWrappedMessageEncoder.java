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

package org.mqttbee.mqtt.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderApplier;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Encoder for a wrapped MQTT message.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @author Silvio Giebl
 */
public interface MqttWrappedMessageEncoder<
        M extends MqttWrappedMessage<M, W, ?>, W extends MqttMessageWrapper<W, M, ?>>
    extends MqttWrappedMessageEncoderApplier<M, W> {

  /**
   * Returns the encoder for the given wrapper around the MQTT message.
   *
   * @param wrapper the MQTT message wrapper.
   * @return the encoder for the MQTT message wrapper
   */
  @NotNull
  MqttMessageEncoder wrap(@NotNull final W wrapper);
}

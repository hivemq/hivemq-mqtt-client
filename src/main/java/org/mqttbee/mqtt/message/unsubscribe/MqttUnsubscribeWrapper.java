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

package org.mqttbee.mqtt.message.unsubscribe;

import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageWrapper.MqttMessageWrapperWithId;

/** @author Silvio Giebl */
@Immutable
public class MqttUnsubscribeWrapper
    extends MqttMessageWrapperWithId<
        MqttUnsubscribeWrapper, MqttUnsubscribe,
        MqttMessageEncoderProvider<MqttUnsubscribeWrapper>> {

  MqttUnsubscribeWrapper(@NotNull final MqttUnsubscribe unsubscribe, final int packetIdentifier) {
    super(unsubscribe, packetIdentifier);
  }

  @NotNull
  @Override
  protected MqttUnsubscribeWrapper getCodable() {
    return this;
  }
}

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

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import static org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp.DEFAULT_REASON_CODE;

import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;

/** @author Silvio Giebl */
public class Mqtt5PubCompEncoder
    extends Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<
        MqttPubComp, Mqtt5PubCompReasonCode, MqttMessageEncoderProvider<MqttPubComp>> {

  public static final MqttMessageEncoderProvider<MqttPubComp> PROVIDER =
      new ThreadLocalMqttMessageEncoderProvider<>(Mqtt5PubCompEncoder::new);

  private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;

  @Override
  protected int getFixedHeader() {
    return FIXED_HEADER;
  }

  @Override
  protected Mqtt5PubCompReasonCode getDefaultReasonCode() {
    return DEFAULT_REASON_CODE;
  }
}

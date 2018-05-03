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

package org.mqttbee.mqtt.message.publish.puback;

import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.publish.MqttQoSMessage;

/** @author Silvio Giebl */
@Immutable
public class MqttPubAck
    extends MqttMessageWithIdAndReasonCode<
        MqttPubAck, Mqtt5PubAckReasonCode, MqttMessageEncoderProvider<MqttPubAck>>
    implements Mqtt5PubAck, MqttQoSMessage {

  @NotNull
  public static final Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

  public MqttPubAck(
      final int packetIdentifier,
      @NotNull final Mqtt5PubAckReasonCode reasonCode,
      @Nullable final MqttUTF8StringImpl reasonString,
      @NotNull final MqttUserPropertiesImpl userProperties,
      @NotNull final MqttMessageEncoderProvider<MqttPubAck> encoderProvider) {

    super(packetIdentifier, reasonCode, reasonString, userProperties, encoderProvider);
  }

  @NotNull
  @Override
  protected MqttPubAck getCodable() {
    return this;
  }
}

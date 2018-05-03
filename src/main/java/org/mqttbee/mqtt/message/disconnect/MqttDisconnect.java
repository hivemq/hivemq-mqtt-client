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

package org.mqttbee.mqtt.message.disconnect;

import java.util.Optional;
import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonCode;

/** @author Silvio Giebl */
@Immutable
public class MqttDisconnect
    extends MqttMessageWithReasonCode<
        MqttDisconnect, Mqtt5DisconnectReasonCode, MqttMessageEncoderProvider<MqttDisconnect>>
    implements Mqtt5Disconnect {

  @NotNull
  public static final Mqtt5DisconnectReasonCode DEFAULT_REASON_CODE =
      Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

  public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;

  private final long sessionExpiryInterval;
  private final MqttUTF8StringImpl serverReference;

  public MqttDisconnect(
      @NotNull final Mqtt5DisconnectReasonCode reasonCode,
      final long sessionExpiryInterval,
      @Nullable final MqttUTF8StringImpl serverReference,
      @Nullable final MqttUTF8StringImpl reasonString,
      @NotNull final MqttUserPropertiesImpl userProperties,
      @NotNull final MqttMessageEncoderProvider<MqttDisconnect> encoderProvider) {

    super(reasonCode, reasonString, userProperties, encoderProvider);
    this.sessionExpiryInterval = sessionExpiryInterval;
    this.serverReference = serverReference;
  }

  @NotNull
  @Override
  public Optional<Long> getSessionExpiryInterval() {
    return (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_FROM_CONNECT)
        ? Optional.empty()
        : Optional.of(sessionExpiryInterval);
  }

  public long getRawSessionExpiryInterval() {
    return sessionExpiryInterval;
  }

  @NotNull
  @Override
  public Optional<MqttUTF8String> getServerReference() {
    return Optional.ofNullable(serverReference);
  }

  @Nullable
  public MqttUTF8StringImpl getRawServerReference() {
    return serverReference;
  }

  @NotNull
  @Override
  protected MqttDisconnect getCodable() {
    return this;
  }
}

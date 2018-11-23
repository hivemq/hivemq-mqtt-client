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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttDisconnect extends MqttMessageWithUserProperties.WithReason.WithCode<Mqtt5DisconnectReasonCode>
        implements Mqtt5Disconnect {

    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;
    public static final @NotNull MqttDisconnect DEFAULT =
            new MqttDisconnect(DEFAULT_REASON_CODE, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                    MqttUserPropertiesImpl.NO_USER_PROPERTIES);

    private final long sessionExpiryInterval;
    private final @Nullable MqttUtf8StringImpl serverReference;

    public MqttDisconnect(
            final @NotNull Mqtt5DisconnectReasonCode reasonCode, final long sessionExpiryInterval,
            final @Nullable MqttUtf8StringImpl serverReference, final @Nullable MqttUtf8StringImpl reasonString,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(reasonCode, reasonString, userProperties);
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverReference = serverReference;
    }

    @Override
    public @NotNull OptionalLong getSessionExpiryInterval() {
        return (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_FROM_CONNECT) ? OptionalLong.empty() :
                OptionalLong.of(sessionExpiryInterval);
    }

    public long getRawSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public @NotNull Optional<MqttUtf8String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

    public @Nullable MqttUtf8StringImpl getRawServerReference() {
        return serverReference;
    }

    @Override
    public @NotNull MqttDisconnectBuilder.Default extend() {
        return new MqttDisconnectBuilder.Default(this);
    }
}

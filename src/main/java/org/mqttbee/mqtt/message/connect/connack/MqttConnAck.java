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

package org.mqttbee.mqtt.message.connect.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuth;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonCode;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnAck extends MqttMessageWithReasonCode<Mqtt5ConnAckReasonCode> implements Mqtt5ConnAck {

    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;
    public static final int KEEP_ALIVE_FROM_CONNECT = -1;
    @Nullable
    public static final MqttClientIdentifierImpl CLIENT_IDENTIFIER_FROM_CONNECT = null;

    private final boolean isSessionPresent;
    private final long sessionExpiryInterval;
    private final int serverKeepAlive;
    private final MqttClientIdentifierImpl assignedClientIdentifier;
    private final Mqtt5EnhancedAuth enhancedAuth;
    private final MqttConnAckRestrictions restrictions;
    private final MqttUTF8StringImpl responseInformation;
    private final MqttUTF8StringImpl serverReference;

    public MqttConnAck(
            @NotNull final Mqtt5ConnAckReasonCode reasonCode, final boolean isSessionPresent,
            final long sessionExpiryInterval, final int serverKeepAlive,
            @Nullable final MqttClientIdentifierImpl assignedClientIdentifier,
            @Nullable final Mqtt5EnhancedAuth enhancedAuth, @NotNull final MqttConnAckRestrictions restrictions,
            @Nullable final MqttUTF8StringImpl responseInformation, @Nullable final MqttUTF8StringImpl serverReference,
            @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties) {

        super(reasonCode, reasonString, userProperties);
        this.isSessionPresent = isSessionPresent;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverKeepAlive = serverKeepAlive;
        this.assignedClientIdentifier = assignedClientIdentifier;
        this.enhancedAuth = enhancedAuth;
        this.restrictions = restrictions;
        this.responseInformation = responseInformation;
        this.serverReference = serverReference;
    }

    @Override
    public boolean isSessionPresent() {
        return isSessionPresent;
    }

    @NotNull
    @Override
    public Optional<Long> getSessionExpiryInterval() {
        return (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_FROM_CONNECT) ? Optional.empty() :
                Optional.of(sessionExpiryInterval);
    }

    public long getRawSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @NotNull
    @Override
    public Optional<Integer> getServerKeepAlive() {
        return (serverKeepAlive == KEEP_ALIVE_FROM_CONNECT) ? Optional.empty() : Optional.of(serverKeepAlive);
    }

    public int getRawServerKeepAlive() {
        return serverKeepAlive;
    }

    @NotNull
    @Override
    public Optional<MqttClientIdentifier> getAssignedClientIdentifier() {
        return Optional.ofNullable(assignedClientIdentifier);
    }

    @Nullable
    public MqttClientIdentifierImpl getRawAssignedClientIdentifier() {
        return assignedClientIdentifier;
    }

    @NotNull
    @Override
    public Optional<Mqtt5EnhancedAuth> getEnhancedAuth() {
        return Optional.ofNullable(enhancedAuth);
    }

    @Nullable
    public Mqtt5EnhancedAuth getRawEnhancedAuth() {
        return enhancedAuth;
    }

    @NotNull
    @Override
    public MqttConnAckRestrictions getRestrictions() {
        return restrictions;
    }

    @NotNull
    @Override
    public Optional<MqttUTF8String> getResponseInformation() {
        return Optional.ofNullable(responseInformation);
    }

    @NotNull
    @Override
    public Optional<MqttUTF8String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

}

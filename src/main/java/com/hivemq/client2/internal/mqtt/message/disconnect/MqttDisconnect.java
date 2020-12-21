/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.internal.mqtt.message.disconnect;

import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client2.internal.util.StringUtil;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttDisconnect extends MqttMessageWithUserProperties.WithReason.WithCode<Mqtt5DisconnectReasonCode>
        implements Mqtt5Disconnect {

    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;
    public static final @NotNull MqttDisconnect DEFAULT =
            new MqttDisconnect(DEFAULT_REASON_CODE, SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                    MqttUserPropertiesImpl.NO_USER_PROPERTIES);

    private final @Range(from = -1, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval;
    private final @Nullable MqttUtf8StringImpl serverReference;

    public MqttDisconnect(
            final @NotNull Mqtt5DisconnectReasonCode reasonCode,
            final @Range(from = -1, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval,
            final @Nullable MqttUtf8StringImpl serverReference,
            final @Nullable MqttUtf8StringImpl reasonString,
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

    public @Range(from = -1, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long getRawSessionExpiryInterval() {
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
    public MqttDisconnectBuilder.@NotNull Default extend() {
        return new MqttDisconnectBuilder.Default(this);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "reasonCode=" + getReasonCode() + ((sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_FROM_CONNECT) ? "" :
                ", sessionExpiryInterval=" + sessionExpiryInterval) +
                ((serverReference == null) ? "" : ", serverReference=" + serverReference) +
                StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttDisconnect{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttDisconnect)) {
            return false;
        }
        final MqttDisconnect that = (MqttDisconnect) o;

        return partialEquals(that) && (sessionExpiryInterval == that.sessionExpiryInterval) &&
                Objects.equals(serverReference, that.serverReference);
    }

    @Override
    public int hashCode() {
        int result = partialHashCode();
        result = 31 * result + Long.hashCode(sessionExpiryInterval);
        result = 31 * result + Objects.hashCode(serverReference);
        return result;
    }
}

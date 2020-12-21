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

package com.hivemq.client2.internal.mqtt.message.connect;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client2.internal.mqtt.message.auth.MqttEnhancedAuth;
import com.hivemq.client2.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client2.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client2.internal.util.StringUtil;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttConnect extends MqttMessageWithUserProperties implements Mqtt5Connect {

    public static final @NotNull MqttConnect DEFAULT =
            new MqttConnect(DEFAULT_KEEP_ALIVE, DEFAULT_CLEAN_START, DEFAULT_SESSION_EXPIRY_INTERVAL,
                    MqttConnectRestrictions.DEFAULT, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);

    private final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int keepAlive;
    private final boolean cleanStart;
    private final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval;
    private final @NotNull MqttConnectRestrictions restrictions;
    private final @Nullable MqttSimpleAuth simpleAuth;
    private final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private final @Nullable MqttWillPublish willPublish;

    public MqttConnect(
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int keepAlive,
            final boolean cleanStart,
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval,
            final @NotNull MqttConnectRestrictions restrictions,
            final @Nullable MqttSimpleAuth simpleAuth,
            final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism,
            final @Nullable MqttWillPublish willPublish,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(userProperties);
        this.keepAlive = keepAlive;
        this.cleanStart = cleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.restrictions = restrictions;
        this.simpleAuth = simpleAuth;
        this.enhancedAuthMechanism = enhancedAuthMechanism;
        this.willPublish = willPublish;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getKeepAlive() {
        return keepAlive;
    }

    @Override
    public boolean isCleanStart() {
        return cleanStart;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public @NotNull MqttConnectRestrictions getRestrictions() {
        return restrictions;
    }

    @Override
    public @NotNull Optional<Mqtt5SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(simpleAuth);
    }

    public @Nullable MqttSimpleAuth getRawSimpleAuth() {
        return simpleAuth;
    }

    @Override
    public @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism() {
        return Optional.ofNullable(enhancedAuthMechanism);
    }

    public @Nullable Mqtt5EnhancedAuthMechanism getRawEnhancedAuthMechanism() {
        return enhancedAuthMechanism;
    }

    @Override
    public @NotNull Optional<Mqtt5WillPublish> getWillPublish() {
        return Optional.ofNullable(willPublish);
    }

    public @Nullable MqttWillPublish getRawWillPublish() {
        return willPublish;
    }

    @Override
    public MqttConnectBuilder.@NotNull Default extend() {
        return new MqttConnectBuilder.Default(this);
    }

    public @NotNull MqttConnect setDefaults(final @NotNull MqttClientConfig clientConfig) {
        final MqttClientConfig.ConnectDefaults connectDefaults = clientConfig.getConnectDefaults();
        final MqttSimpleAuth defaultSimpleAuth = connectDefaults.getSimpleAuth();
        final Mqtt5EnhancedAuthMechanism defaultEnhancedAuthMechanism = connectDefaults.getEnhancedAuthMechanism();
        final MqttWillPublish defaultWillPublish = connectDefaults.getWillPublish();

        if (((defaultSimpleAuth == null) || (simpleAuth != null)) &&
                ((defaultEnhancedAuthMechanism == null) || (enhancedAuthMechanism != null)) &&
                ((defaultWillPublish == null) || (willPublish != null))) {
            return this;
        }
        return new MqttConnect(keepAlive, cleanStart, sessionExpiryInterval, restrictions,
                (simpleAuth == null) ? defaultSimpleAuth : simpleAuth,
                (enhancedAuthMechanism == null) ? defaultEnhancedAuthMechanism : enhancedAuthMechanism,
                (willPublish == null) ? defaultWillPublish : willPublish, getUserProperties());
    }

    public @NotNull MqttStatefulConnect createStateful(
            final @NotNull MqttClientIdentifierImpl clientIdentifier, final @Nullable MqttEnhancedAuth enhancedAuth) {

        return new MqttStatefulConnect(this, clientIdentifier, enhancedAuth);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "keepAlive=" + keepAlive + ", cleanStart=" + cleanStart + ", sessionExpiryInterval=" +
                sessionExpiryInterval +
                ((restrictions == MqttConnectRestrictions.DEFAULT) ? "" : ", restrictions=" + restrictions) +
                ((simpleAuth == null) ? "" : ", simpleAuth=" + simpleAuth) +
                ((enhancedAuthMechanism == null) ? "" : ", enhancedAuthMechanism=" + enhancedAuthMechanism) +
                ((willPublish == null) ? "" : ", willPublish=" + willPublish) +
                StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttConnect{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttConnect)) {
            return false;
        }
        final MqttConnect that = (MqttConnect) o;

        return partialEquals(that) && (keepAlive == that.keepAlive) && (cleanStart == that.cleanStart) &&
                (sessionExpiryInterval == that.sessionExpiryInterval) && restrictions.equals(that.restrictions) &&
                Objects.equals(simpleAuth, that.simpleAuth) &&
                Objects.equals(enhancedAuthMechanism, that.enhancedAuthMechanism) &&
                Objects.equals(willPublish, that.willPublish);
    }

    @Override
    public int hashCode() {
        int result = partialHashCode();
        result = 31 * result + keepAlive;
        result = 31 * result + Boolean.hashCode(cleanStart);
        result = 31 * result + Long.hashCode(sessionExpiryInterval);
        result = 31 * result + restrictions.hashCode();
        result = 31 * result + Objects.hashCode(simpleAuth);
        result = 31 * result + Objects.hashCode(enhancedAuthMechanism);
        result = 31 * result + Objects.hashCode(willPublish);
        return result;
    }
}

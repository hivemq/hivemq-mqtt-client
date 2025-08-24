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

package com.hivemq.mqtt.client2.internal.message.connect.mqtt3;

import com.hivemq.mqtt.client2.internal.datatypes.MqttUserPropertiesImpl;
import com.hivemq.mqtt.client2.internal.message.auth.MqttSimpleAuth;
import com.hivemq.mqtt.client2.internal.message.auth.mqtt3.Mqtt3SimpleAuthView;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnectRestrictions;
import com.hivemq.mqtt.client2.internal.message.publish.MqttWillPublish;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.mqtt.client2.internal.util.UnsignedDataTypes;
import com.hivemq.mqtt.client2.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3ConnectRestrictions;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnectView implements Mqtt3Connect {

    public static final @NotNull Mqtt3ConnectView DEFAULT =
            of(DEFAULT_KEEP_ALIVE, DEFAULT_CLEAN_SESSION, MqttConnectRestrictions.DEFAULT, null, null);

    private static @NotNull MqttConnect delegate(
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int keepAlive,
            final boolean cleanSession,
            final @NotNull MqttConnectRestrictions restrictions,
            final @Nullable MqttSimpleAuth simpleAuth,
            final @Nullable MqttWillPublish willPublish) {

        return new MqttConnect(keepAlive, cleanSession, cleanSession ? 0 : MqttConnect.NO_SESSION_EXPIRY, restrictions,
                simpleAuth, null, willPublish, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    static @NotNull Mqtt3ConnectView of(
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int keepAlive,
            final boolean cleanSession,
            final @NotNull MqttConnectRestrictions restrictions,
            final @Nullable MqttSimpleAuth simpleAuth,
            final @Nullable MqttWillPublish willPublish) {
        return new Mqtt3ConnectView(delegate(keepAlive, cleanSession, restrictions, simpleAuth, willPublish));
    }

    public static @NotNull Mqtt3ConnectView of(final @NotNull MqttConnect delegate) {
        return new Mqtt3ConnectView(delegate);
    }

    private final @NotNull MqttConnect delegate;

    private Mqtt3ConnectView(final @NotNull MqttConnect delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getKeepAlive() {
        return delegate.getKeepAlive();
    }

    @Override
    public boolean isCleanSession() {
        return delegate.isCleanStart();
    }

    @Override
    public @NotNull Mqtt3ConnectRestrictions getRestrictions() {
        return delegate.getRestrictions();
    }

    @Override
    public @NotNull Optional<Mqtt3SimpleAuth> getSimpleAuth() {
        return Optional.ofNullable(getRawSimpleAuth());
    }

    private @Nullable Mqtt3SimpleAuth getRawSimpleAuth() {
        final MqttSimpleAuth simpleAuth = delegate.getRawSimpleAuth();
        return (simpleAuth == null) ? null : Mqtt3SimpleAuthView.of(simpleAuth);
    }

    @Override
    public @NotNull Optional<Mqtt3Publish> getWillPublish() {
        return Optional.ofNullable(getRawWillPublish());
    }

    private @Nullable Mqtt3Publish getRawWillPublish() {
        final MqttWillPublish willPublish = delegate.getRawWillPublish();
        return (willPublish == null) ? null : Mqtt3PublishView.of(willPublish);
    }

    public @NotNull MqttConnect getDelegate() {
        return delegate;
    }

    @Override
    public Mqtt3ConnectViewBuilder.@NotNull Default extend() {
        return new Mqtt3ConnectViewBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        final Mqtt3SimpleAuth simpleAuth = getRawSimpleAuth();
        final Mqtt3Publish willPublish = getRawWillPublish();
        final Mqtt3ConnectRestrictions restrictions = getRestrictions();
        return "keepAlive=" + getKeepAlive() + ", cleanSession=" + isCleanSession() + ", restrictions=" + restrictions +
                ((simpleAuth == null) ? "" : ", simpleAuth=" + simpleAuth) +
                ((willPublish == null) ? "" : ", willPublish=" + willPublish);
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
        if (!(o instanceof Mqtt3ConnectView)) {
            return false;
        }
        final Mqtt3ConnectView that = (Mqtt3ConnectView) o;
        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}

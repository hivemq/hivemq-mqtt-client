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

package com.hivemq.client.internal.mqtt.message.connect;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuthBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttConnectBuilder<B extends MqttConnectBuilder<B>> {

    private @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int keepAlive =
            MqttConnect.DEFAULT_KEEP_ALIVE;
    private boolean cleanStart = MqttConnect.DEFAULT_CLEAN_START;
    private @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval =
            MqttConnect.DEFAULT_SESSION_EXPIRY_INTERVAL;
    private @NotNull MqttConnectRestrictions restrictions = MqttConnectRestrictions.DEFAULT;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private @Nullable MqttWillPublish willPublish;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    MqttConnectBuilder() {}

    MqttConnectBuilder(final @NotNull MqttConnect connect) {
        keepAlive = connect.getKeepAlive();
        cleanStart = connect.isCleanStart();
        sessionExpiryInterval = connect.getSessionExpiryInterval();
        restrictions = connect.getRestrictions();
        simpleAuth = connect.getRawSimpleAuth();
        enhancedAuthMechanism = connect.getRawEnhancedAuthMechanism();
        willPublish = connect.getRawWillPublish();
        userProperties = connect.getUserProperties();
    }

    abstract @NotNull B self();

    public @NotNull B keepAlive(final int keepAlive) {
        this.keepAlive = Checks.unsignedShort(keepAlive, "Keep alive");
        return self();
    }

    public @NotNull B noKeepAlive() {
        this.keepAlive = MqttConnect.NO_KEEP_ALIVE;
        return self();
    }

    public @NotNull B cleanStart(final boolean cleanStart) {
        this.cleanStart = cleanStart;
        return self();
    }

    public @NotNull B sessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = Checks.unsignedInt(sessionExpiryInterval, "Session expiry interval");
        return self();
    }

    public @NotNull B noSessionExpiry() {
        this.sessionExpiryInterval = MqttConnect.NO_SESSION_EXPIRY;
        return self();
    }

    public @NotNull B restrictions(final @Nullable Mqtt5ConnectRestrictions restrictions) {
        this.restrictions = Checks.notImplemented(restrictions, MqttConnectRestrictions.class, "Connect restrictions");
        return self();
    }

    public MqttConnectRestrictionsBuilder.@NotNull Nested<B> restrictionsWith() {
        return new MqttConnectRestrictionsBuilder.Nested<>(restrictions, this::restrictions);
    }

    public @NotNull B simpleAuth(final @Nullable Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = Checks.notImplementedOrNull(simpleAuth, MqttSimpleAuth.class, "Simple auth");
        return self();
    }

    public MqttSimpleAuthBuilder.@NotNull Nested<B> simpleAuthWith() {
        return new MqttSimpleAuthBuilder.Nested<>(this::simpleAuth);
    }

    public @NotNull B enhancedAuth(final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism) {
        this.enhancedAuthMechanism = enhancedAuthMechanism;
        return self();
    }

    public @NotNull B willPublish(final @Nullable Mqtt5Publish willPublish) {
        this.willPublish = (willPublish == null) ? null :
                Checks.notImplemented(willPublish, MqttPublish.class, "Will publish").asWill();
        return self();
    }

    public MqttPublishBuilder.@NotNull WillNested<B> willPublishWith() {
        return new MqttPublishBuilder.WillNested<>(this::willPublish);
    }

    public @NotNull B userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return self();
    }

    public MqttUserPropertiesImplBuilder.@NotNull Nested<B> userPropertiesWith() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    public @NotNull MqttConnect build() {
        return new MqttConnect(keepAlive, cleanStart, sessionExpiryInterval, restrictions, simpleAuth,
                enhancedAuthMechanism, willPublish, userProperties);
    }

    public static class Default extends MqttConnectBuilder<Default> implements Mqtt5ConnectBuilder {

        public Default() {}

        Default(final @NotNull MqttConnect connect) {
            super(connect);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttConnectBuilder<Nested<P>> implements Mqtt5ConnectBuilder.Nested<P> {

        private final @NotNull Function<? super MqttConnect, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttConnect, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        public Nested(
                final @NotNull MqttConnect connect, final @NotNull Function<? super MqttConnect, P> parentConsumer) {

            super(connect);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyConnect() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends MqttConnectBuilder<Send<P>> implements Mqtt5ConnectBuilder.Send<P> {

        private final @NotNull Function<? super MqttConnect, P> parentConsumer;

        public Send(final @NotNull Function<? super MqttConnect, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}

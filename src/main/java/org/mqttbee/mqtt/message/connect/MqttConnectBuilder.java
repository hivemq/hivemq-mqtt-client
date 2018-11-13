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

package org.mqttbee.mqtt.message.connect;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuthBuilder;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishBuilder;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.util.MqttChecks;
import org.mqttbee.util.Checks;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttConnectBuilder<B extends MqttConnectBuilder<B>> {

    private int keepAlive = MqttConnect.DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = MqttConnect.DEFAULT_CLEAN_START;
    private long sessionExpiryInterval = MqttConnect.DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = MqttConnect.DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = MqttConnect.DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private @NotNull MqttConnectRestrictions restrictions = MqttConnectRestrictions.DEFAULT;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private @Nullable MqttWillPublish willPublish;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    MqttConnectBuilder() {}

    MqttConnectBuilder(final @Nullable Mqtt5Connect connect) {
        final MqttConnect mqttConnect = MqttChecks.connect(connect);
        keepAlive = mqttConnect.getKeepAlive();
        isCleanStart = mqttConnect.isCleanStart();
        sessionExpiryInterval = mqttConnect.getSessionExpiryInterval();
        isResponseInformationRequested = mqttConnect.isResponseInformationRequested();
        isProblemInformationRequested = mqttConnect.isProblemInformationRequested();
        restrictions = mqttConnect.getRestrictions();
        simpleAuth = mqttConnect.getRawSimpleAuth();
        enhancedAuthProvider = mqttConnect.getRawEnhancedAuthProvider();
        willPublish = mqttConnect.getRawWillPublish();
        userProperties = mqttConnect.getUserProperties();
    }

    abstract @NotNull B self();

    public @NotNull B keepAlive(final int keepAlive, final @Nullable TimeUnit timeUnit) {
        Checks.notNull(timeUnit, "Time unit");
        final long keepAliveSeconds = timeUnit.toSeconds(keepAlive);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAliveSeconds),
                "The value of keep alive converted in seconds must not exceed the value range of unsigned short. Found: %s which is bigger than %s (max unsigned short).",
                keepAliveSeconds, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
        this.keepAlive = (int) keepAliveSeconds;
        return self();
    }

    public @NotNull B cleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return self();
    }

    public @NotNull B sessionExpiryInterval(final long sessionExpiryInterval, final @Nullable TimeUnit timeUnit) {
        Checks.notNull(timeUnit, "Time unit");
        final long sessionExpiryIntervalSeconds = timeUnit.toSeconds(sessionExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                sessionExpiryIntervalSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);
        this.sessionExpiryInterval = sessionExpiryIntervalSeconds;
        return self();
    }

    public @NotNull B responseInformationRequested(final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return self();
    }

    public @NotNull B problemInformationRequested(final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return self();
    }

    public @NotNull B restrictions(final @Nullable Mqtt5ConnectRestrictions restrictions) {
        this.restrictions = Checks.notImplemented(restrictions, MqttConnectRestrictions.class, "Connect restrictions");
        return self();
    }

    public @NotNull MqttConnectRestrictionsBuilder.Nested<B> restrictions() {
        return new MqttConnectRestrictionsBuilder.Nested<>(this::restrictions);
    }

    public @NotNull B simpleAuth(final @Nullable Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = Checks.notImplementedOrNull(simpleAuth, MqttSimpleAuth.class, "Simple auth");
        return self();
    }

    public @NotNull MqttSimpleAuthBuilder.Nested<B> simpleAuth() {
        return new MqttSimpleAuthBuilder.Nested<>(this::simpleAuth);
    }

    public @NotNull B enhancedAuth(final @Nullable Mqtt5EnhancedAuthProvider enhancedAuthProvider) {
        this.enhancedAuthProvider = enhancedAuthProvider;
        return self();
    }

    public @NotNull B willPublish(final @Nullable Mqtt5Publish willPublish) {
        this.willPublish = (willPublish == null) ? null :
                MqttChecks.willPublish(Checks.notImplemented(willPublish, MqttPublish.class, "Will publish"));
        return self();
    }

    public @NotNull B willPublish(final @Nullable Mqtt5WillPublish willPublish) {
        this.willPublish = Checks.notImplementedOrNull(willPublish, MqttWillPublish.class, "Will publish");
        return self();
    }

    public @NotNull MqttPublishBuilder.WillNested<B> willPublish() {
        return new MqttPublishBuilder.WillNested<>(this::willPublish);
    }

    public @NotNull B userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return self();
    }

    public @NotNull MqttUserPropertiesImplBuilder.Nested<B> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(this::userProperties);
    }

    public @NotNull MqttConnect build() {
        return new MqttConnect(keepAlive, isCleanStart, sessionExpiryInterval, isResponseInformationRequested,
                isProblemInformationRequested, restrictions, simpleAuth, enhancedAuthProvider, willPublish,
                userProperties);
    }

    public static class Default extends MqttConnectBuilder<Default> implements Mqtt5ConnectBuilder {

        public Default() {}

        public Default(final @Nullable Mqtt5Connect connect) {
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

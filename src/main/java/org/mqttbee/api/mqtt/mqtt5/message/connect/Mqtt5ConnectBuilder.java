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

package org.mqttbee.api.mqtt.mqtt5.message.connect;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.*;

public class Mqtt5ConnectBuilder<P> extends FluentBuilder<Mqtt5Connect, P> {

    private int keepAliveSeconds = DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = DEFAULT_CLEAN_START;
    private long sessionExpiryIntervalSeconds = DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private @NotNull MqttConnectRestrictions restrictions = MqttConnectRestrictions.DEFAULT;
    private @Nullable MqttSimpleAuth simpleAuth;
    private @Nullable Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private @Nullable MqttWillPublish willPublish;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5ConnectBuilder(final @Nullable Function<? super Mqtt5Connect, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt5ConnectBuilder(final @NotNull Mqtt5Connect connect) {
        super(null);
        final MqttConnect connectImpl = MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnect.class);
        keepAliveSeconds = connectImpl.getKeepAlive();
        isCleanStart = connectImpl.isCleanStart();
        sessionExpiryIntervalSeconds = connectImpl.getSessionExpiryInterval();
        isResponseInformationRequested = connectImpl.isResponseInformationRequested();
        isProblemInformationRequested = connectImpl.isProblemInformationRequested();
        restrictions = connectImpl.getRestrictions();
        simpleAuth = connectImpl.getRawSimpleAuth();
        enhancedAuthProvider = connectImpl.getRawEnhancedAuthProvider();
        willPublish = connectImpl.getRawWillPublish();
        userProperties = connectImpl.getUserProperties();
    }

    public @NotNull Mqtt5ConnectBuilder<P> keepAlive(final int keepAlive, final @NotNull TimeUnit timeUnit) {
        final long keepAliveSeconds = timeUnit.toSeconds(keepAlive);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAliveSeconds),
                "The value of keep alive converted in seconds must not exceed the value range of unsigned short. Found: %s which is bigger than %s (max unsigned short).",
                keepAliveSeconds, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
        this.keepAliveSeconds = (int) keepAliveSeconds;
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> cleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> sessionExpiryInterval(
            final long sessionExpiryInterval, final @NotNull TimeUnit timeUnit) {

        final long sessionExpiryIntervalSeconds = timeUnit.toSeconds(sessionExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                keepAliveSeconds, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);

        this.sessionExpiryIntervalSeconds = sessionExpiryIntervalSeconds;
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> responseInformationRequested(final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> problemInformationRequested(final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> restrictions(final @NotNull Mqtt5ConnectRestrictions restrictions) {
        this.restrictions = MustNotBeImplementedUtil.checkNotImplemented(restrictions, MqttConnectRestrictions.class);
        return this;
    }

    public @NotNull Mqtt5ConnectRestrictionsBuilder<Mqtt5ConnectBuilder<P>> restrictions() {
        return new Mqtt5ConnectRestrictionsBuilder<>(this::restrictions);
    }

    public @NotNull Mqtt5ConnectBuilder<P> simpleAuth(final @Nullable Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = MustNotBeImplementedUtil.checkNullOrNotImplemented(simpleAuth, MqttSimpleAuth.class);
        return this;
    }

    public @NotNull Mqtt5SimpleAuthBuilder<Mqtt5ConnectBuilder<P>> simpleAuth() {
        return new Mqtt5SimpleAuthBuilder<>(this::simpleAuth);
    }

    public @NotNull Mqtt5ConnectBuilder<P> enhancedAuth(
            final @Nullable Mqtt5EnhancedAuthProvider enhancedAuthProvider) {

        this.enhancedAuthProvider = enhancedAuthProvider;
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> willPublish(final @Nullable Mqtt5Publish willPublish) {
        this.willPublish = MqttBuilderUtil.willPublish(
                MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, MqttPublish.class));
        return this;
    }

    public @NotNull Mqtt5ConnectBuilder<P> willPublish(final @Nullable Mqtt5WillPublish willPublish) {
        this.willPublish = MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, MqttWillPublish.class);
        return this;
    }

    public @NotNull Mqtt5WillPublishBuilder<Mqtt5ConnectBuilder<P>> willPublish() {
        return new Mqtt5WillPublishBuilder<>(this::willPublish);
    }

    public @NotNull Mqtt5ConnectBuilder<P> userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    public @NotNull Mqtt5UserPropertiesBuilder<Mqtt5ConnectBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @Override
    public @NotNull Mqtt5Connect build() {
        return new MqttConnect(keepAliveSeconds, isCleanStart, sessionExpiryIntervalSeconds,
                isResponseInformationRequested, isProblemInformationRequested, restrictions, simpleAuth,
                enhancedAuthProvider, willPublish, userProperties);
    }

    public @NotNull P applyConnect() {
        return apply();
    }
}
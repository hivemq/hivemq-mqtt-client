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

import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.DEFAULT_CLEAN_START;
import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.DEFAULT_KEEP_ALIVE;
import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.DEFAULT_PROBLEM_INFORMATION_REQUESTED;
import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.DEFAULT_RESPONSE_INFORMATION_REQUESTED;
import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.DEFAULT_SESSION_EXPIRY_INTERVAL;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

public class Mqtt5ConnectBuilder<P> extends FluentBuilder<Mqtt5Connect, P> {

    private int keepAliveSeconds = DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = DEFAULT_CLEAN_START;
    private long sessionExpiryIntervalSeconds = DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private MqttConnectRestrictions restrictions = MqttConnectRestrictions.DEFAULT;
    private MqttSimpleAuth simpleAuth;
    private Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private MqttWillPublish willPublish;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5ConnectBuilder(@Nullable final Function<? super Mqtt5Connect, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt5ConnectBuilder(@NotNull final Mqtt5Connect connect) {
        super(null);
        final MqttConnect connectImpl =
                MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnect.class);
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

    @NotNull
    public Mqtt5ConnectBuilder<P> keepAlive(final int keepAlive, @NotNull final TimeUnit timeUnit) {
        final long keepAliveSeconds = timeUnit.toSeconds(keepAlive);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAliveSeconds));
        this.keepAliveSeconds = (int) keepAliveSeconds;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> cleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> sessionExpiryInterval(
            final long sessionExpiryInterval, @NotNull final TimeUnit timeUnit) {

        final long sessionExpiryIntervalSeconds = timeUnit.toSeconds(sessionExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryIntervalSeconds));
        this.sessionExpiryIntervalSeconds = sessionExpiryIntervalSeconds;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> responseInformationRequested(
            final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> problemInformationRequested(
            final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> restrictions(
            @NotNull final Mqtt5ConnectRestrictions restrictions) {
        this.restrictions =
                MustNotBeImplementedUtil.checkNotImplemented(
                        restrictions, MqttConnectRestrictions.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectRestrictionsBuilder<? extends Mqtt5ConnectBuilder<P>> restrictions() {
        return new Mqtt5ConnectRestrictionsBuilder<>(this::restrictions);
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> simpleAuth(@Nullable final Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(
                        simpleAuth, MqttSimpleAuth.class);
        return this;
    }

    @NotNull
    public Mqtt5SimpleAuthBuilder<? extends Mqtt5ConnectBuilder<P>> simpleAuth() {
        return new Mqtt5SimpleAuthBuilder<>(this::simpleAuth);
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> enhancedAuth(
            @Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {
        this.enhancedAuthProvider = enhancedAuthProvider;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> willPublish(@Nullable final Mqtt5WillPublish willPublish) {
        this.willPublish =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(
                        willPublish, MqttWillPublish.class);
        return this;
    }

    @NotNull
    public Mqtt5WillPublishBuilder<? extends Mqtt5ConnectBuilder<P>> willPublish() {
        return Mqtt5WillPublishBuilder.create(this::willPublish);
    }

    @NotNull
    public Mqtt5ConnectBuilder<P> userProperties(
            @NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<? extends Mqtt5ConnectBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    @Override
    public Mqtt5Connect build() {
        return new MqttConnect(
                keepAliveSeconds,
                isCleanStart,
                sessionExpiryIntervalSeconds,
                isResponseInformationRequested,
                isProblemInformationRequested,
                restrictions,
                simpleAuth,
                enhancedAuthProvider,
                willPublish,
                userProperties);
    }
}

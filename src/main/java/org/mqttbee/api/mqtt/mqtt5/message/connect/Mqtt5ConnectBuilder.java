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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect.*;

public class Mqtt5ConnectBuilder {

    private int keepAlive = DEFAULT_KEEP_ALIVE;
    private boolean isCleanStart = DEFAULT_CLEAN_START;
    private long sessionExpiryInterval = DEFAULT_SESSION_EXPIRY_INTERVAL;
    private boolean isResponseInformationRequested = DEFAULT_RESPONSE_INFORMATION_REQUESTED;
    private boolean isProblemInformationRequested = DEFAULT_PROBLEM_INFORMATION_REQUESTED;
    private MqttConnectRestrictions restrictions = MqttConnectRestrictions.DEFAULT;
    private MqttSimpleAuth simpleAuth;
    private Mqtt5EnhancedAuthProvider enhancedAuthProvider;
    private MqttWillPublish willPublish;
    private MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    Mqtt5ConnectBuilder() {
    }

    Mqtt5ConnectBuilder(@NotNull final Mqtt5Connect connect) {
        final MqttConnect connectImpl = MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnect.class);
        keepAlive = connectImpl.getKeepAlive();
        isCleanStart = connectImpl.isCleanStart();
        sessionExpiryInterval = connectImpl.getSessionExpiryInterval();
        isResponseInformationRequested = connectImpl.isResponseInformationRequested();
        isProblemInformationRequested = connectImpl.isProblemInformationRequested();
        restrictions = connectImpl.getRestrictions();
        simpleAuth = connectImpl.getRawSimpleAuth();
        enhancedAuthProvider = connectImpl.getRawEnhancedAuthProvider();
        willPublish = connectImpl.getRawWillPublish();
        userProperties = connectImpl.getUserProperties();
    }

    @NotNull
    public Mqtt5ConnectBuilder withKeepAlive(final int keepAlive) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(keepAlive));
        this.keepAlive = keepAlive;
        return this;
    }

    public Mqtt5ConnectBuilder withCleanStart(final boolean isCleanStart) {
        this.isCleanStart = isCleanStart;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withSessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withResponseInformationRequested(final boolean isResponseInformationRequested) {
        this.isResponseInformationRequested = isResponseInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withProblemInformationRequested(final boolean isProblemInformationRequested) {
        this.isProblemInformationRequested = isProblemInformationRequested;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withRestrictions(@NotNull final Mqtt5ConnectRestrictions restrictions) {
        this.restrictions = MustNotBeImplementedUtil.checkNotImplemented(restrictions, MqttConnectRestrictions.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withSimpleAuth(@Nullable final Mqtt5SimpleAuth simpleAuth) {
        this.simpleAuth = MustNotBeImplementedUtil.checkNullOrNotImplemented(simpleAuth, MqttSimpleAuth.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withEnhancedAuth(@Nullable final Mqtt5EnhancedAuthProvider enhancedAuthProvider) {
        this.enhancedAuthProvider = enhancedAuthProvider;
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withWillPublish(@Nullable final Mqtt5WillPublish willPublish) {
        this.willPublish = MustNotBeImplementedUtil.checkNullOrNotImplemented(willPublish, MqttWillPublish.class);
        return this;
    }

    @NotNull
    public Mqtt5ConnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @NotNull
    public Mqtt5Connect build() {
        return new MqttConnect(keepAlive, isCleanStart, sessionExpiryInterval, isResponseInformationRequested,
                isProblemInformationRequested, restrictions, simpleAuth, enhancedAuthProvider, willPublish,
                userProperties);
    }

}
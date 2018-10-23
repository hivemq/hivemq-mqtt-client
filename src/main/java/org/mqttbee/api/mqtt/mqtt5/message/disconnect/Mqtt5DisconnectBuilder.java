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

package org.mqttbee.api.mqtt.mqtt5.message.disconnect;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE;
import static org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectBuilder<P> extends FluentBuilder<Mqtt5Disconnect, P> {

    private boolean withWillMessage = false;
    private long sessionExpiryIntervalSeconds = MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
    private @Nullable MqttUTF8StringImpl serverReference;
    private @Nullable MqttUTF8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public Mqtt5DisconnectBuilder(final @Nullable Function<? super Mqtt5Disconnect, P> parentConsumer) {
        super(parentConsumer);
    }

    public @NotNull Mqtt5DisconnectBuilder<P> willMessage(final boolean withWillMessage) {
        this.withWillMessage = withWillMessage;
        return this;
    }

    public @NotNull Mqtt5DisconnectBuilder<P> sessionExpiryInterval(
            final long sessionExpiryInterval, final @NotNull TimeUnit timeUnit) {

        final long sessionExpiryIntervalSeconds = timeUnit.toSeconds(sessionExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                sessionExpiryInterval, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);

        this.sessionExpiryIntervalSeconds = sessionExpiryIntervalSeconds;
        return this;
    }

    public @NotNull Mqtt5DisconnectBuilder<P> serverReference(final @Nullable String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    public @NotNull Mqtt5DisconnectBuilder<P> serverReference(final @Nullable MqttUTF8String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return this;
    }

    public @NotNull Mqtt5DisconnectBuilder<P> reasonString(final @Nullable String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    public @NotNull Mqtt5DisconnectBuilder<P> reasonString(final @Nullable MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    public @NotNull Mqtt5DisconnectBuilder<P> userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    public @NotNull Mqtt5UserPropertiesBuilder<Mqtt5DisconnectBuilder<P>> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @Override
    public @NotNull Mqtt5Disconnect build() {
        final Mqtt5DisconnectReasonCode reasonCode =
                withWillMessage ? DISCONNECT_WITH_WILL_MESSAGE : NORMAL_DISCONNECTION;
        return new MqttDisconnect(
                reasonCode, sessionExpiryIntervalSeconds, serverReference, reasonString, userProperties);
    }

    public @NotNull P applyDisconnect() {
        return apply();
    }
}

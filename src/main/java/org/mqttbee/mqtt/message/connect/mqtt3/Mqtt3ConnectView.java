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

package org.mqttbee.mqtt.message.connect.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3ConnectView implements Mqtt3Connect {

    @NotNull
    public static MqttConnect wrapped(
            final int keepAlive, final boolean isCleanSession, @Nullable final MqttSimpleAuth simpleAuth,
            @Nullable final MqttWillPublish willPublish) {

        return new MqttConnect(keepAlive, isCleanSession, isCleanSession ? 0 : MqttConnect.NO_SESSION_EXPIRY,
                MqttConnect.DEFAULT_RESPONSE_INFORMATION_REQUESTED, MqttConnect.DEFAULT_PROBLEM_INFORMATION_REQUESTED,
                MqttConnectRestrictions.DEFAULT, simpleAuth, null, willPublish,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    public static Mqtt3ConnectView create(
            final int keepAlive, final boolean isCleanSession, @Nullable final MqttSimpleAuth simpleAuth,
            @Nullable final MqttWillPublish willPublish) {

        return new Mqtt3ConnectView(wrapped(keepAlive, isCleanSession, simpleAuth, willPublish));
    }

    private final MqttConnect wrapped;

    private Mqtt3ConnectView(@NotNull final MqttConnect wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int getKeepAlive() {
        return wrapped.getKeepAlive();
    }

    @Override
    public boolean isCleanSession() {
        return wrapped.isCleanStart();
    }

    @NotNull
    @Override
    public Optional<Mqtt3SimpleAuth> getSimpleAuth() {
        final MqttSimpleAuth simpleAuth = wrapped.getRawSimpleAuth();
        return (simpleAuth == null) ? Optional.empty() : Optional.of(new Mqtt3SimpleAuthView(simpleAuth));
    }

    @NotNull
    @Override
    public Optional<Mqtt3Publish> getWillPublish() {
        final MqttWillPublish willPublish = wrapped.getRawWillPublish();
        return (willPublish == null) ? Optional.empty() : Optional.of(new Mqtt3PublishView(willPublish));
    }

    @NotNull
    public MqttConnect getWrapped() {
        return wrapped;
    }

}

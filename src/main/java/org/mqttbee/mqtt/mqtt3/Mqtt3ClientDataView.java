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

package org.mqttbee.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;
import org.mqttbee.api.mqtt.MqttClientSslConfig;
import org.mqttbee.api.mqtt.MqttClientState;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientConnectionData;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientData;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ServerConnectionData;
import org.mqttbee.mqtt.MqttClientData;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientDataView implements Mqtt3ClientData {

    private final @NotNull MqttClientData delegate;

    Mqtt3ClientDataView(@NotNull final MqttClientData delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Optional<MqttClientIdentifier> getClientIdentifier() {
        return delegate.getClientIdentifier();
    }

    @Override
    public @NotNull String getServerHost() {
        return delegate.getServerHost();
    }

    @Override
    public int getServerPort() {
        return delegate.getServerPort();
    }

    @Override
    public boolean usesSsl() {
        return delegate.usesSsl();
    }

    @Override
    public @NotNull Optional<MqttClientSslConfig> getSslConfig() {
        return delegate.getSslConfig();
    }

    @Override
    public boolean usesWebSocket() {
        return delegate.usesWebSocket();
    }

    @Override
    public @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig() {
        return delegate.getWebSocketConfig();
    }

    @Override
    public @NotNull MqttClientExecutorConfig getExecutorConfig() {
        return delegate.getExecutorConfig();
    }

    @Override
    public @NotNull MqttClientState getState() {
        return delegate.getState();
    }

    @Override
    public @NotNull Optional<Mqtt3ClientConnectionData> getClientConnectionData() {
        return Optional.ofNullable(delegate.getRawClientConnectionData());
    }

    @Override
    public @NotNull Optional<Mqtt3ServerConnectionData> getServerConnectionData() {
        return Optional.ofNullable(delegate.getRawServerConnectionData());
    }
}

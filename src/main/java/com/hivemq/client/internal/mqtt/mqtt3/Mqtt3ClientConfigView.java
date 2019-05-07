/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.mqtt3;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.*;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConnectionConfig;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientConfigView implements Mqtt3ClientConfig {

    private final @NotNull MqttClientConfig delegate;

    Mqtt3ClientConfigView(final @NotNull MqttClientConfig delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull MqttVersion getMqttVersion() {
        return delegate.getMqttVersion();
    }

    @Override
    public @NotNull Optional<MqttClientIdentifier> getClientIdentifier() {
        return delegate.getClientIdentifier();
    }

    @Override
    public @NotNull InetSocketAddress getServerAddress() {
        return delegate.getServerAddress();
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
    public @NotNull MqttClientExecutorConfig getExecutorConfig() {
        return delegate.getExecutorConfig();
    }

    @Override
    public @NotNull Optional<MqttClientSslConfig> getSslConfig() {
        return delegate.getSslConfig();
    }

    @Override
    public @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig() {
        return delegate.getWebSocketConfig();
    }

    @Override
    public @NotNull MqttClientState getState() {
        return delegate.getState();
    }

    @Override
    public @NotNull Optional<Mqtt3ClientConnectionConfig> getConnectionConfig() {
        return Optional.ofNullable(delegate.getRawConnectionConfig());
    }
}

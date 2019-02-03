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

package org.mqttbee.internal.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.*;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.mqtt.mqtt3.Mqtt3ClientConfig;
import org.mqttbee.mqtt.mqtt3.Mqtt3ClientConnectionConfig;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientConfigView implements Mqtt3ClientConfig {

    private final @NotNull MqttClientConfig delegate;

    Mqtt3ClientConfigView(@NotNull final MqttClientConfig delegate) {
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

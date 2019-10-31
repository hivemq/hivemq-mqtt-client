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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientTransportConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttClientTransportConfigImpl implements MqttClientTransportConfig {

    public static final @NotNull MqttClientTransportConfigImpl DEFAULT = new MqttClientTransportConfigImpl(
            InetSocketAddress.createUnresolved(MqttClient.DEFAULT_SERVER_HOST, MqttClient.DEFAULT_SERVER_PORT), null,
            null, null);

    private final @NotNull InetSocketAddress serverAddress;
    private final @Nullable InetSocketAddress localAddress;
    private final @Nullable MqttClientSslConfigImpl sslConfig;
    private final @Nullable MqttWebSocketConfigImpl webSocketConfig;

    MqttClientTransportConfigImpl(
            final @NotNull InetSocketAddress serverAddress, final @Nullable InetSocketAddress localAddress,
            final @Nullable MqttClientSslConfigImpl sslConfig,
            final @Nullable MqttWebSocketConfigImpl webSocketConfig) {

        this.serverAddress = serverAddress;
        this.localAddress = localAddress;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
    }

    @Override
    public @NotNull InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    @Override
    public @NotNull Optional<InetSocketAddress> getLocalAddress() {
        return Optional.ofNullable(localAddress);
    }

    public @Nullable InetSocketAddress getRawLocalAddress() {
        return localAddress;
    }

    @Override
    public @NotNull Optional<MqttClientSslConfig> getSslConfig() {
        return Optional.ofNullable(sslConfig);
    }

    public @Nullable MqttClientSslConfigImpl getRawSslConfig() {
        return sslConfig;
    }

    @Override
    public @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig() {
        return Optional.ofNullable(webSocketConfig);
    }

    public @Nullable MqttWebSocketConfigImpl getRawWebSocketConfig() {
        return webSocketConfig;
    }

    @Override
    public @NotNull MqttClientTransportConfigImplBuilder.Default extend() {
        return new MqttClientTransportConfigImplBuilder.Default(this);
    }
}

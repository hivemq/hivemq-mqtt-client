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

import com.hivemq.client.mqtt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttClientTransportConfigImpl implements MqttClientTransportConfig {

    public static final @NotNull MqttClientTransportConfigImpl DEFAULT = new MqttClientTransportConfigImpl(
            InetSocketAddress.createUnresolved(MqttClient.DEFAULT_SERVER_HOST, MqttClient.DEFAULT_SERVER_PORT), null,
            null, null, null);

    private final @NotNull InetSocketAddress serverAddress;
    private final @Nullable InetSocketAddress localAddress;
    private final @Nullable MqttClientSslConfigImpl sslConfig;
    private final @Nullable MqttWebSocketConfigImpl webSocketConfig;
    private final @Nullable MqttProxyConfigImpl proxyConfig;

    MqttClientTransportConfigImpl(
            final @NotNull InetSocketAddress serverAddress, final @Nullable InetSocketAddress localAddress,
            final @Nullable MqttClientSslConfigImpl sslConfig, final @Nullable MqttWebSocketConfigImpl webSocketConfig,
            final @Nullable MqttProxyConfigImpl proxyConfig) {

        this.serverAddress = serverAddress;
        this.localAddress = localAddress;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
        this.proxyConfig = proxyConfig;
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
    public @NotNull Optional<MqttProxyConfig> getProxyConfig() {
        return Optional.ofNullable(proxyConfig);
    }

    public @Nullable MqttProxyConfigImpl getRawProxyConfig() {
        return proxyConfig;
    }

    @Override
    public @NotNull MqttClientTransportConfigImplBuilder.Default extend() {
        return new MqttClientTransportConfigImplBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttClientTransportConfigImpl)) {
            return false;
        }
        final MqttClientTransportConfigImpl that = (MqttClientTransportConfigImpl) o;

        return serverAddress.equals(that.serverAddress) && Objects.equals(localAddress, that.localAddress) &&
                Objects.equals(sslConfig, that.sslConfig) && Objects.equals(webSocketConfig, that.webSocketConfig) &&
                Objects.equals(proxyConfig, that.proxyConfig);
    }

    @Override
    public int hashCode() {
        int result = serverAddress.hashCode();
        result = 31 * result + Objects.hashCode(localAddress);
        result = 31 * result + Objects.hashCode(sslConfig);
        result = 31 * result + Objects.hashCode(webSocketConfig);
        result = 31 * result + Objects.hashCode(proxyConfig);
        return result;
    }
}

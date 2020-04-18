/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.util.InetSocketAddressUtil;
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
            InetSocketAddressUtil.create(MqttClient.DEFAULT_SERVER_HOST, MqttClient.DEFAULT_SERVER_PORT), null, null,
            null, null, DEFAULT_SOCKET_CONNECT_TIMEOUT_MS, DEFAULT_MQTT_CONNECT_TIMEOUT_MS);

    private final @NotNull InetSocketAddress serverAddress;
    private final @Nullable InetSocketAddress localAddress;
    private final @Nullable MqttClientSslConfigImpl sslConfig;
    private final @Nullable MqttWebSocketConfigImpl webSocketConfig;
    private final @Nullable MqttProxyConfigImpl proxyConfig;
    private final int socketConnectTimeoutMs;
    private final int mqttConnectTimeoutMs;

    MqttClientTransportConfigImpl(
            final @NotNull InetSocketAddress serverAddress, final @Nullable InetSocketAddress localAddress,
            final @Nullable MqttClientSslConfigImpl sslConfig, final @Nullable MqttWebSocketConfigImpl webSocketConfig,
            final @Nullable MqttProxyConfigImpl proxyConfig, final int socketConnectTimeoutMs,
            final int mqttConnectTimeoutMs) {

        this.serverAddress = serverAddress;
        this.localAddress = localAddress;
        this.sslConfig = sslConfig;
        this.webSocketConfig = webSocketConfig;
        this.proxyConfig = proxyConfig;
        this.socketConnectTimeoutMs = socketConnectTimeoutMs;
        this.mqttConnectTimeoutMs = mqttConnectTimeoutMs;
    }

    @Override
    public @NotNull InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public @NotNull InetSocketAddress getRemoteAddress() {
        return (proxyConfig == null) ? serverAddress : proxyConfig.getAddress();
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
    public int getSocketConnectTimeoutMs() {
        return socketConnectTimeoutMs;
    }

    @Override
    public int getMqttConnectTimeoutMs() {
        return mqttConnectTimeoutMs;
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
                Objects.equals(proxyConfig, that.proxyConfig) &&
                (socketConnectTimeoutMs == that.socketConnectTimeoutMs) &&
                (mqttConnectTimeoutMs == that.mqttConnectTimeoutMs);
    }

    @Override
    public int hashCode() {
        int result = serverAddress.hashCode();
        result = 31 * result + Objects.hashCode(localAddress);
        result = 31 * result + Objects.hashCode(sslConfig);
        result = 31 * result + Objects.hashCode(webSocketConfig);
        result = 31 * result + Objects.hashCode(proxyConfig);
        result = 31 * result + Integer.hashCode(socketConnectTimeoutMs);
        result = 31 * result + Integer.hashCode(mqttConnectTimeoutMs);
        return result;
    }
}

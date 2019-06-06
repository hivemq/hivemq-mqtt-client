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

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientTransportConfigBuilder;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Function;

import static com.hivemq.client.mqtt.MqttClient.*;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientTransportConfigImplBuilder<B extends MqttClientTransportConfigImplBuilder<B>> {

    private @Nullable InetSocketAddress serverAddress;
    private @NotNull Object serverHost = DEFAULT_SERVER_HOST; // String or InetAddress
    private int serverPort = -1;
    private @Nullable MqttClientSslConfigImpl sslConfig;
    private @Nullable MqttWebSocketConfigImpl webSocketConfig;

    MqttClientTransportConfigImplBuilder() {}

    MqttClientTransportConfigImplBuilder(final @NotNull MqttClientTransportConfigImpl transportConfig) {
        set(transportConfig);
    }

    MqttClientTransportConfigImplBuilder(final @NotNull MqttClientTransportConfigImplBuilder<?> builder) {
        serverAddress = builder.serverAddress;
        serverHost = builder.serverHost;
        serverPort = builder.serverPort;
        sslConfig = builder.sslConfig;
        webSocketConfig = builder.webSocketConfig;
    }

    void set(final @NotNull MqttClientTransportConfigImpl transportConfig) {
        serverAddress = transportConfig.getServerAddress();
        sslConfig = transportConfig.getRawSslConfig();
        webSocketConfig = transportConfig.getRawWebSocketConfig();
    }

    abstract @NotNull B self();

    public @NotNull B serverAddress(final @Nullable InetSocketAddress address) {
        this.serverAddress = Checks.notNull(address, "Server address");
        return self();
    }

    public @NotNull B serverHost(final @Nullable String host) {
        setServerHost(Checks.notEmpty(host, "Server host"));
        return self();
    }

    public @NotNull B serverHost(final @Nullable InetAddress host) {
        setServerHost(Checks.notNull(host, "Server host"));
        return self();
    }

    private void setServerHost(final @NotNull Object serverHost) {
        this.serverHost = serverHost;
        if (serverAddress != null) {
            serverPort = serverAddress.getPort();
            serverAddress = null;
        }
    }

    public @NotNull B serverPort(final int port) {
        this.serverPort = Checks.unsignedShort(port, "Server port");
        if (serverAddress != null) {
            final InetAddress inetAddress = serverAddress.getAddress();
            if (inetAddress != null) {
                serverHost = inetAddress;
            } else {
                serverHost = serverAddress.getHostString();
            }
            serverAddress = null;
        }
        return self();
    }

    public @NotNull B sslWithDefaultConfig() {
        this.sslConfig = MqttClientSslConfigImpl.DEFAULT;
        return self();
    }

    public @NotNull B sslConfig(final @Nullable MqttClientSslConfig sslConfig) {
        this.sslConfig = Checks.notImplementedOrNull(sslConfig, MqttClientSslConfigImpl.class, "SSL config");
        return self();
    }

    public @NotNull MqttClientSslConfigImplBuilder.Nested<B> sslConfig() {
        return new MqttClientSslConfigImplBuilder.Nested<>(sslConfig, this::sslConfig);
    }

    public @NotNull B webSocketWithDefaultConfig() {
        this.webSocketConfig = MqttWebSocketConfigImpl.DEFAULT;
        return self();
    }

    public @NotNull B webSocketConfig(final @Nullable MqttWebSocketConfig webSocketConfig) {
        this.webSocketConfig =
                Checks.notImplementedOrNull(webSocketConfig, MqttWebSocketConfigImpl.class, "WebSocket config");
        return self();
    }

    public @NotNull MqttWebSocketConfigImplBuilder.Nested<B> webSocketConfig() {
        return new MqttWebSocketConfigImplBuilder.Nested<>(webSocketConfig, this::webSocketConfig);
    }

    private @NotNull InetSocketAddress getServerAddress() {
        if (serverAddress != null) {
            return serverAddress;
        }
        if (serverHost instanceof InetAddress) {
            return new InetSocketAddress((InetAddress) serverHost, getServerPort());
        }
        return InetSocketAddress.createUnresolved((String) serverHost, getServerPort());
    }

    private int getServerPort() {
        if (serverPort != -1) {
            return serverPort;
        }
        if (sslConfig == null) {
            if (webSocketConfig == null) {
                return DEFAULT_SERVER_PORT;
            }
            return DEFAULT_SERVER_PORT_WEBSOCKET;
        }
        if (webSocketConfig == null) {
            return DEFAULT_SERVER_PORT_SSL;
        }
        return DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
    }

    @NotNull MqttClientTransportConfigImpl buildTransportConfig() {
        return new MqttClientTransportConfigImpl(getServerAddress(), sslConfig, webSocketConfig);
    }

    public static class Default extends MqttClientTransportConfigImplBuilder<Default>
            implements MqttClientTransportConfigBuilder {

        public Default() {}

        Default(final @NotNull MqttClientTransportConfigImpl transportConfig) {
            super(transportConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }

        @Override
        public @NotNull MqttClientTransportConfigImpl build() {
            return buildTransportConfig();
        }
    }

    public static class Nested<P> extends MqttClientTransportConfigImplBuilder<Nested<P>>
            implements MqttClientTransportConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientTransportConfigImpl, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttClientTransportConfigImpl, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        public Nested(
                final @NotNull MqttClientTransportConfigImpl transportConfig,
                final @NotNull Function<? super MqttClientTransportConfigImpl, P> parentConsumer) {

            super(transportConfig);
            this.parentConsumer = parentConsumer;
        }

        Nested(
                final @NotNull MqttClientTransportConfigImplBuilder<?> builder,
                final @NotNull Function<? super MqttClientTransportConfigImpl, P> parentConsumer) {

            super(builder);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyTransportConfig() {
            return parentConsumer.apply(buildTransportConfig());
        }
    }
}

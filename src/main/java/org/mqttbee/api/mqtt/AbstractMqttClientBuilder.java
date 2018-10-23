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

package org.mqttbee.api.mqtt;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttClientSslConfigImpl;
import org.mqttbee.mqtt.MqttWebSocketConfigImpl;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import static org.mqttbee.api.mqtt.MqttClient.*;

/**
 * @author Silvio Giebl
 */
public abstract class AbstractMqttClientBuilder<S extends AbstractMqttClientBuilder<S>> {

    protected @NotNull MqttClientIdentifierImpl identifier =
            MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    protected @NotNull String serverHost = DEFAULT_SERVER_HOST;
    protected int serverPort = DEFAULT_SERVER_PORT;
    private boolean customServerPort = false;
    protected @Nullable MqttClientSslConfig sslConfig;
    protected @Nullable MqttWebSocketConfig webSocketConfig;
    protected @NotNull MqttClientExecutorConfigImpl executorConfig = MqttClientExecutorConfigImpl.DEFAULT;

    @NotNull S init(final @NotNull AbstractMqttClientBuilder<?> clientBuilder) {
        this.identifier = clientBuilder.identifier;
        this.serverHost = clientBuilder.serverHost;
        this.serverPort = clientBuilder.serverPort;
        this.customServerPort = clientBuilder.customServerPort;
        this.sslConfig = clientBuilder.sslConfig;
        this.webSocketConfig = clientBuilder.webSocketConfig;
        this.executorConfig = clientBuilder.executorConfig;
        return self();
    }

    protected abstract @NotNull S self();

    public @NotNull S identifier(final @NotNull String identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return self();
    }

    public @NotNull S identifier(final @NotNull MqttClientIdentifier identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return self();
    }

    public @NotNull S serverHost(final @NotNull String host) {
        this.serverHost = Preconditions.checkNotNull(host, "Server host must not be null.");
        return self();
    }

    public @NotNull S serverPort(final int port) {
        this.serverPort = port;
        customServerPort = true;
        return self();
    }

    public @NotNull S useSslWithDefaultConfig() {
        return useSsl(MqttClientSslConfigImpl.DEFAULT);
    }

    public @NotNull S useSsl(final @NotNull MqttClientSslConfig sslConfig) {
        if (!customServerPort) {
            if (webSocketConfig == null) {
                serverPort = DEFAULT_SERVER_PORT_SSL;
            } else {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        this.sslConfig = Preconditions.checkNotNull(sslConfig, "SSL config must not be null.");
        return self();
    }

    public @NotNull MqttClientSslConfigBuilder<S> useSsl() {
        return new MqttClientSslConfigBuilder<>(this::useSsl);
    }

    public @NotNull S useWebSocketWithDefaultConfig() {
        return useWebSocket(MqttWebSocketConfigImpl.DEFAULT);
    }

    public @NotNull S useWebSocket(final @NotNull MqttWebSocketConfig webSocketConfig) {
        if (!customServerPort) {
            if (sslConfig == null) {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET;
            } else {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        this.webSocketConfig = Preconditions.checkNotNull(webSocketConfig, "WebSocket config must not be null.");
        return self();
    }

    public @NotNull MqttWebSocketConfigBuilder<S> useWebSocket() {
        return new MqttWebSocketConfigBuilder<>(this::useWebSocket);
    }

    public @NotNull S executorConfig(final @NotNull MqttClientExecutorConfig executorConfig) {
        this.executorConfig =
                MustNotBeImplementedUtil.checkNotImplemented(executorConfig, MqttClientExecutorConfigImpl.class);
        return self();
    }

    public @NotNull MqttClientExecutorConfigBuilder<S> executorConfig() {
        return new MqttClientExecutorConfigBuilder<>(this::executorConfig);
    }
}

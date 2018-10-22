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
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientBuilder;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientBuilder;
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
public class MqttClientBuilder {

    protected @NotNull MqttClientIdentifierImpl identifier =
            MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    protected @NotNull String serverHost = DEFAULT_SERVER_HOST;
    protected int serverPort = DEFAULT_SERVER_PORT;
    private boolean customServerPort = false;
    protected @Nullable MqttClientSslConfig sslConfig;
    protected @Nullable MqttWebSocketConfig webSocketConfig;
    protected @NotNull MqttClientExecutorConfigImpl executorConfig = MqttClientExecutorConfigImpl.DEFAULT;

    protected MqttClientBuilder() {
    }

    public @NotNull MqttClientBuilder identifier(@NotNull final String identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return this;
    }

    public @NotNull MqttClientBuilder identifier(@NotNull final MqttClientIdentifier identifier) {
        this.identifier = MqttBuilderUtil.clientIdentifier(identifier);
        return this;
    }

    public @NotNull MqttClientBuilder serverHost(@NotNull final String host) {
        this.serverHost = Preconditions.checkNotNull(host, "Server host must not be null.");
        return this;
    }

    public @NotNull MqttClientBuilder serverPort(final int port) {
        this.serverPort = port;
        customServerPort = true;
        return this;
    }

    public @NotNull MqttClientBuilder useSslWithDefaultConfig() {
        return useSsl(MqttClientSslConfigImpl.DEFAULT);
    }

    public @NotNull MqttClientBuilder useSsl(@NotNull final MqttClientSslConfig sslConfig) {
        if (!customServerPort) {
            if (webSocketConfig == null) {
                serverPort = DEFAULT_SERVER_PORT_SSL;
            } else {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        this.sslConfig = Preconditions.checkNotNull(sslConfig, "SSL config must not be null.");
        return this;
    }

    public @NotNull MqttClientSslConfigBuilder<? extends MqttClientBuilder> useSsl() {
        return new MqttClientSslConfigBuilder<>(this::useSsl);
    }

    public @NotNull MqttClientBuilder useWebSocketWithDefaultConfig() {
        return useWebSocket(MqttWebSocketConfigImpl.DEFAULT);
    }

    public @NotNull MqttClientBuilder useWebSocket(@NotNull final MqttWebSocketConfig webSocketConfig) {
        if (!customServerPort) {
            if (sslConfig == null) {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET;
            } else {
                serverPort = DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        this.webSocketConfig = Preconditions.checkNotNull(webSocketConfig, "WebSocket config must not be null.");
        return this;
    }

    public @NotNull MqttWebSocketConfigBuilder<? extends MqttClientBuilder> useWebSocket() {
        return new MqttWebSocketConfigBuilder<>(this::useWebSocket);
    }

    public @NotNull MqttClientBuilder executorConfig(@NotNull final MqttClientExecutorConfig executorConfig) {
        this.executorConfig =
                MustNotBeImplementedUtil.checkNotImplemented(executorConfig, MqttClientExecutorConfigImpl.class);
        return this;
    }

    public @NotNull MqttClientExecutorConfigBuilder<? extends MqttClientBuilder> executorConfig() {
        return new MqttClientExecutorConfigBuilder<>(this::executorConfig);
    }

    public @NotNull Mqtt3ClientBuilder useMqttVersion3() {
        return new Mqtt3ClientBuilder(identifier, serverHost, serverPort, sslConfig, webSocketConfig, executorConfig);
    }

    public @NotNull Mqtt5ClientBuilder useMqttVersion5() {
        return new Mqtt5ClientBuilder(identifier, serverHost, serverPort, sslConfig, webSocketConfig, executorConfig);
    }

}

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

package org.mqttbee.mqtt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientBuilder;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;
import org.mqttbee.api.mqtt.MqttClientSslConfig;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.mqtt3.Mqtt3RxClientViewBuilder;
import org.mqttbee.mqtt.util.MqttChecks;
import org.mqttbee.util.Checks;

import static org.mqttbee.api.mqtt.MqttClient.*;

/**
 * @author Silvio Giebl
 */
public abstract class MqttRxClientBuilderBase<B extends MqttRxClientBuilderBase<B>> {

    protected @NotNull MqttClientIdentifierImpl identifier =
            MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER;
    protected @NotNull String serverHost = DEFAULT_SERVER_HOST;
    protected int serverPort = DEFAULT_SERVER_PORT;
    private boolean customServerPort = false;
    protected @Nullable MqttClientSslConfigImpl sslConfig;
    protected @Nullable MqttWebSocketConfigImpl webSocketConfig;
    protected @NotNull MqttClientExecutorConfigImpl executorConfig = MqttClientExecutorConfigImpl.DEFAULT;

    protected MqttRxClientBuilderBase() {}

    protected MqttRxClientBuilderBase(final @NotNull MqttRxClientBuilderBase clientBuilder) {
        this.identifier = clientBuilder.identifier;
        this.serverHost = clientBuilder.serverHost;
        this.serverPort = clientBuilder.serverPort;
        this.customServerPort = clientBuilder.customServerPort;
        this.sslConfig = clientBuilder.sslConfig;
        this.webSocketConfig = clientBuilder.webSocketConfig;
        this.executorConfig = clientBuilder.executorConfig;
    }

    protected abstract @NotNull B self();

    public @NotNull B identifier(final @Nullable String identifier) {
        this.identifier = MqttClientIdentifierImpl.of(identifier);
        return self();
    }

    public @NotNull B identifier(final @Nullable MqttClientIdentifier identifier) {
        this.identifier = MqttChecks.clientIdentifier(identifier);
        return self();
    }

    public @NotNull B serverHost(final @Nullable String host) {
        this.serverHost = Checks.notEmpty(host, "Server host");
        return self();
    }

    public @NotNull B serverPort(final int port) {
        this.serverPort = Checks.unsignedShort(port, "Server port");
        customServerPort = true;
        return self();
    }

    public @NotNull B useSslWithDefaultConfig() {
        return useSsl(MqttClientSslConfigImpl.DEFAULT);
    }

    public @NotNull B useSsl(final @Nullable MqttClientSslConfig sslConfig) {
        if (sslConfig == null) {
            this.sslConfig = null;
            if (!customServerPort) {
                serverPort = (webSocketConfig == null) ? DEFAULT_SERVER_PORT : DEFAULT_SERVER_PORT_WEBSOCKET;
            }
        } else {
            this.sslConfig = Checks.notImplemented(sslConfig, MqttClientSslConfigImpl.class, "SSL config");
            if (!customServerPort) {
                serverPort = (webSocketConfig == null) ? DEFAULT_SERVER_PORT_SSL : DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        return self();
    }

    public @NotNull MqttClientSslConfigImplBuilder.Nested<B> useSsl() {
        return new MqttClientSslConfigImplBuilder.Nested<>(this::useSsl);
    }

    public @NotNull B useWebSocketWithDefaultConfig() {
        return useWebSocket(MqttWebSocketConfigImpl.DEFAULT);
    }

    public @NotNull B useWebSocket(final @Nullable MqttWebSocketConfig webSocketConfig) {
        if (webSocketConfig == null) {
            this.webSocketConfig = null;
            if (!customServerPort) {
                serverPort = (sslConfig == null) ? DEFAULT_SERVER_PORT : DEFAULT_SERVER_PORT_SSL;
            }
        } else {
            this.webSocketConfig =
                    Checks.notImplemented(webSocketConfig, MqttWebSocketConfigImpl.class, "WebSocket config");
            if (!customServerPort) {
                serverPort = (sslConfig == null) ? DEFAULT_SERVER_PORT_WEBSOCKET : DEFAULT_SERVER_PORT_WEBSOCKET_SSL;
            }
        }
        return self();
    }

    public @NotNull MqttWebSocketConfigImplBuilder.Nested<B> useWebSocket() {
        return new MqttWebSocketConfigImplBuilder.Nested<>(this::useWebSocket);
    }

    public @NotNull B executorConfig(final @Nullable MqttClientExecutorConfig executorConfig) {
        this.executorConfig =
                Checks.notImplemented(executorConfig, MqttClientExecutorConfigImpl.class, "Executor config");
        return self();
    }

    public @NotNull MqttClientExecutorConfigImplBuilder.Nested<B> executorConfig() {
        return new MqttClientExecutorConfigImplBuilder.Nested<>(this::executorConfig);
    }

    public static class Choose extends MqttRxClientBuilderBase<Choose> implements MqttClientBuilder {

        @Override
        protected @NotNull Choose self() {
            return this;
        }

        @Override
        public @NotNull Mqtt3RxClientViewBuilder useMqttVersion3() {
            return new Mqtt3RxClientViewBuilder(this);
        }

        @Override
        public @NotNull MqttRxClientBuilder useMqttVersion5() {
            return new MqttRxClientBuilder(this);
        }
    }
}

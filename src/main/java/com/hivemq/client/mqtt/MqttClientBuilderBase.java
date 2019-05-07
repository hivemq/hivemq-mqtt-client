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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Builder base for a {@link MqttClient}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientBuilderBase<B extends MqttClientBuilderBase<B>> {

    /**
     * Sets the {@link MqttClientConfig#getClientIdentifier() Client Identifier}.
     *
     * @param identifier the string representation of the Client Identifier.
     * @return the builder.
     */
    @NotNull B identifier(@NotNull String identifier);

    /**
     * Sets the {@link MqttClientConfig#getClientIdentifier() Client Identifier}.
     *
     * @param identifier the Client Identifier.
     * @return the builder.
     */
    @NotNull B identifier(@NotNull MqttClientIdentifier identifier);

    /**
     * Sets the {@link MqttClientConfig#getServerAddress() server address} to connect to.
     *
     * @param address the server address.
     * @return the builder
     * @since 1.1
     */
    @NotNull B serverAddress(@NotNull InetSocketAddress address);

    /**
     * Sets the {@link MqttClientConfig#getServerHost() server host} to connect to.
     *
     * @param host the server host.
     * @return the builder.
     */
    @NotNull B serverHost(@NotNull String host);

    /**
     * Sets the {@link MqttClientConfig#getServerHost() server host} to connect to.
     *
     * @param host the server host.
     * @return the builder.
     * @since 1.1
     */
    @NotNull B serverHost(@NotNull InetAddress host);

    /**
     * Sets the {@link MqttClientConfig#getServerPort() server port} to connect to.
     *
     * @param port the server port.
     * @return the builder.
     */
    @NotNull B serverPort(int port);

    /**
     * Uses SSL with the default configuration.
     *
     * @return the builder.
     */
    @NotNull B useSslWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientConfig#getSslConfig() SSL configuration}.
     *
     * @param sslConfig the SSL configuration or <code>null</code> to remove any previously set SSL configuration.
     * @return the builder.
     */
    @NotNull B useSsl(@Nullable MqttClientSslConfig sslConfig);

    /**
     * Fluent counterpart of {@link #useSsl(MqttClientSslConfig)}.
     * <p>
     * Calling {@link MqttClientSslConfigBuilder.Nested#applySslConfig()} on the returned builder has the effect of
     * extending the current SSL configuration.
     *
     * @return the fluent builder for the SSL configuration.
     * @see #useSsl(MqttClientSslConfig)
     */
    @NotNull MqttClientSslConfigBuilder.Nested<? extends B> useSsl();

    /**
     * Uses WebSocket with the default configuration.
     *
     * @return the builder.
     */
    @NotNull B useWebSocketWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientConfig#getWebSocketConfig() WebSocket configuration}.
     *
     * @param webSocketConfig the WebSocket configuration or <code>null</code> to remove any previously set WebSocket
     *                        configuration.
     * @return the builder.
     */
    @NotNull B useWebSocket(@Nullable MqttWebSocketConfig webSocketConfig);

    /**
     * Fluent counterpart of {@link #useWebSocket(MqttWebSocketConfig)}.
     * <p>
     * Calling {@link MqttWebSocketConfigBuilder.Nested#applyWebSocketConfig()} on the returned builder has the effect
     * of extending the current WebSocket configuration.
     *
     * @return the fluent builder for the WebSocket configuration.
     * @see #useWebSocket(MqttWebSocketConfig)
     */
    @NotNull MqttWebSocketConfigBuilder.Nested<? extends B> useWebSocket();

    /**
     * Sets the {@link MqttClientConfig#getExecutorConfig() executor configuration}.
     *
     * @param executorConfig the executor configuration.
     * @return the builder.
     */
    @NotNull B executorConfig(@NotNull MqttClientExecutorConfig executorConfig);

    /**
     * Fluent counterpart of {@link #executorConfig(MqttClientExecutorConfig)}.
     * <p>
     * Calling {@link MqttClientExecutorConfigBuilder.Nested#applyExecutorConfig()} on the returned builder has the
     * effect of extending the current executor configuration.
     *
     * @return the fluent builder for the executor configuration.
     * @see #executorConfig(MqttClientExecutorConfig)
     */
    @NotNull MqttClientExecutorConfigBuilder.Nested<? extends B> executorConfig();
}

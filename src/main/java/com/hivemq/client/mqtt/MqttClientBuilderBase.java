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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnectBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Builder base for an {@link MqttClient}.
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
    @CheckReturnValue
    @NotNull B identifier(@NotNull String identifier);

    /**
     * Sets the {@link MqttClientConfig#getClientIdentifier() Client Identifier}.
     *
     * @param identifier the Client Identifier.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B identifier(@NotNull MqttClientIdentifier identifier);

    /**
     * Sets the {@link MqttClientConfig#getServerAddress() server address} to connect to.
     *
     * @param address the server address.
     * @return the builder
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B serverAddress(@NotNull InetSocketAddress address);

    /**
     * Sets the {@link MqttClientConfig#getServerHost() server host} to connect to.
     *
     * @param host the server host.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverHost(@NotNull String host);

    /**
     * Sets the {@link MqttClientConfig#getServerHost() server host} to connect to.
     *
     * @param host the server host.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B serverHost(@NotNull InetAddress host);

    /**
     * Sets the {@link MqttClientConfig#getServerPort() server port} to connect to.
     *
     * @param port the server port.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverPort(int port);

    /**
     * Sets the {@link MqttClientConfig#getTlsConfig() transport layer security configuration} to the default
     * configuration.
     * <p>
     * This means that the systems default trust store, ciphers and protocols are used.
     *
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B tlsWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientConfig#getTlsConfig() transport layer security configuration}.
     *
     * @param tlsConfig the transport layer security configuration or <code>null</code> to remove any previously set
     *                  transport layer security configuration.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B tlsConfig(@Nullable MqttTlsConfig tlsConfig);

    /**
     * Fluent counterpart of {@link #tlsConfig(MqttTlsConfig)}.
     * <p>
     * Calling {@link MqttTlsConfigBuilder.Nested#applyTlsConfig()} on the returned builder has the effect of extending
     * the current transport layer security configuration.
     *
     * @return the fluent builder for the transport layer security configuration.
     * @see #tlsConfig(MqttTlsConfig)
     * @since 1.1
     */
    @CheckReturnValue
    MqttTlsConfigBuilder.@NotNull Nested<? extends B> tlsConfig();

    /**
     * Sets the {@link MqttClientConfig#getWebSocketConfig() WebSocket transport configuration} to the default
     * configuration.
     *
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B webSocketWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientConfig#getWebSocketConfig() WebSocket transport configuration}.
     *
     * @param webSocketConfig the WebSocket transport configuration or <code>null</code> to remove any previously set
     *                        WebSocket transport configuration.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B webSocketConfig(@Nullable MqttWebSocketConfig webSocketConfig);

    /**
     * Fluent counterpart of {@link #webSocketConfig(MqttWebSocketConfig)}.
     * <p>
     * Calling {@link MqttWebSocketConfigBuilder.Nested#applyWebSocketConfig()} on the returned builder has the effect
     * of extending the current WebSocket transport configuration.
     *
     * @return the fluent builder for the WebSocket configuration.
     * @see #webSocketConfig(MqttWebSocketConfig)
     * @since 1.1
     */
    @CheckReturnValue
    MqttWebSocketConfigBuilder.@NotNull Nested<? extends B> webSocketConfig();

    /**
     * Sets the {@link MqttClientConfig#getTransportConfig() transport configuration}.
     *
     * @param transportConfig the transport configuration.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B transportConfig(@NotNull MqttTransportConfig transportConfig);

    /**
     * Fluent counterpart of {@link #transportConfig(MqttTransportConfig)}.
     * <p>
     * Calling {@link MqttTransportConfigBuilder.Nested#applyTransportConfig()} on the returned builder has the effect
     * of extending the current transport configuration.
     *
     * @return the fluent builder for the transport configuration.
     * @see #transportConfig(MqttTransportConfig)
     * @since 1.1
     */
    @CheckReturnValue
    MqttTransportConfigBuilder.@NotNull Nested<? extends B> transportConfig();

    /**
     * Sets the {@link MqttClientConfig#getExecutorConfig() executor configuration}.
     *
     * @param executorConfig the executor configuration.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B executorConfig(@NotNull MqttExecutorConfig executorConfig);

    /**
     * Fluent counterpart of {@link #executorConfig(MqttExecutorConfig)}.
     * <p>
     * Calling {@link MqttExecutorConfigBuilder.Nested#applyExecutorConfig()} on the returned builder has the effect of
     * extending the current executor configuration.
     *
     * @return the fluent builder for the executor configuration.
     * @see #executorConfig(MqttExecutorConfig)
     */
    @CheckReturnValue
    MqttExecutorConfigBuilder.@NotNull Nested<? extends B> executorConfig();

    /**
     * Uses automatic reconnect with the default configuration.
     *
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B automaticReconnectWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientConfig#getAutomaticReconnect() automatic reconnect strategy}.
     *
     * @param autoReconnect the automatic reconnect strategy or <code>null</code> to remove any previously set automatic
     *                      reconnect strategy.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B automaticReconnect(@Nullable MqttClientAutoReconnect autoReconnect);

    /**
     * Fluent counterpart of {@link #automaticReconnect(MqttClientAutoReconnect)}.
     * <p>
     * Calling {@link MqttClientAutoReconnectBuilder.Nested#applyAutomaticReconnect()} on the returned builder has the
     * effect of extending the current automatic reconnect strategy.
     *
     * @return the fluent builder for the automatic reconnect strategy.
     * @see #automaticReconnect(MqttClientAutoReconnect)
     * @since 1.1
     */
    @CheckReturnValue
    MqttClientAutoReconnectBuilder.@NotNull Nested<? extends B> automaticReconnect();

    /**
     * Adds a listener which is notified when the client is connected (a successful ConnAck message is received).
     * <p>
     * The listeners are called in the same order in which they are added.
     *
     * @param connectedListener the listener to add.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B addConnectedListener(@NotNull MqttClientConnectedListener connectedListener);

    /**
     * Adds a listener which is notified when the client is disconnected (with or without a Disconnect message) or the
     * connection fails.
     * <p>
     * The listeners are called in the same order in which they are added.
     *
     * @param disconnectedListener the listener to add.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B addDisconnectedListener(@NotNull MqttClientDisconnectedListener disconnectedListener);
}

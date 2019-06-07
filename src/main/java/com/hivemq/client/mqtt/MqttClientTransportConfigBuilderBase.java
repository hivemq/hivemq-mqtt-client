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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Builder base for a {@link MqttClientTransportConfig}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientTransportConfigBuilderBase<B extends MqttClientTransportConfigBuilderBase<B>> {

    /**
     * Sets the {@link MqttClientTransportConfig#getServerAddress() server address} to connect to.
     *
     * @param address the server address.
     * @return the builder.
     */
    @NotNull B serverAddress(@NotNull InetSocketAddress address);

    /**
     * Sets the server host to connect to.
     *
     * @param host the server host.
     * @return the builder.
     */
    @NotNull B serverHost(@NotNull String host);

    /**
     * Sets the server host to connect to.
     *
     * @param host the server host.
     * @return the builder.
     */
    @NotNull B serverHost(@NotNull InetAddress host);

    /**
     * Sets the server port to connect to.
     *
     * @param port the server port.
     * @return the builder.
     */
    @NotNull B serverPort(int port);

    /**
     * Sets the {@link MqttClientTransportConfig#getSslConfig() secure transport configuration} to the default
     * configuration.
     * <p>
     * This means that the systems default trust store, ciphers and protocols are used.
     *
     * @return the builder.
     */
    @NotNull B sslWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientTransportConfig#getSslConfig() secure transport configuration}.
     *
     * @param sslConfig the secure transport configuration or <code>null</code> to remove any previously set secure
     *                  transport configuration.
     * @return the builder.
     */
    @NotNull B sslConfig(@Nullable MqttClientSslConfig sslConfig);

    /**
     * Fluent counterpart of {@link #sslConfig(MqttClientSslConfig)}.
     * <p>
     * Calling {@link MqttClientSslConfigBuilder.Nested#applySslConfig()} on the returned builder has the effect of
     * extending the current secure transport configuration.
     *
     * @return the fluent builder for the secure transport configuration.
     * @see #sslConfig(MqttClientSslConfig)
     */
    @NotNull MqttClientSslConfigBuilder.Nested<? extends B> sslConfig();

    /**
     * Sets the {@link MqttClientTransportConfig#getWebSocketConfig() WebSocket transport configuration} to the default
     * configuration.
     *
     * @return the builder.
     */
    @NotNull B webSocketWithDefaultConfig();

    /**
     * Sets the optional {@link MqttClientTransportConfig#getWebSocketConfig() WebSocket transport configuration}.
     *
     * @param webSocketConfig the WebSocket transport configuration or <code>null</code> to remove any previously set
     *                        WebSocket transport configuration.
     * @return the builder.
     */
    @NotNull B webSocketConfig(@Nullable MqttWebSocketConfig webSocketConfig);

    /**
     * Fluent counterpart of {@link #webSocketConfig(MqttWebSocketConfig)}.
     * <p>
     * Calling {@link MqttWebSocketConfigBuilder.Nested#applyWebSocketConfig()} on the returned builder has the effect
     * of extending the current WebSocket transport configuration.
     *
     * @return the fluent builder for the WebSocket configuration.
     * @see #webSocketConfig(MqttWebSocketConfig)
     */
    @NotNull MqttWebSocketConfigBuilder.Nested<? extends B> webSocketConfig();
}

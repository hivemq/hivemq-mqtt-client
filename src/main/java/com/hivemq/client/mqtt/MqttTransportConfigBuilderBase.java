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
import com.hivemq.client.internal.util.UnsignedDataTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Builder base for a {@link MqttTransportConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.NonExtendable
public interface MqttTransportConfigBuilderBase<B extends MqttTransportConfigBuilderBase<B>> {

    /**
     * Sets the {@link MqttTransportConfig#getServerAddress() server address} to connect to.
     *
     * @param address the server address.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverAddress(@NotNull InetSocketAddress address);

    /**
     * Sets the server host to connect to.
     *
     * @param host the server host.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverHost(@NotNull String host);

    /**
     * Sets the server host to connect to.
     *
     * @param host the server host.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverHost(@NotNull InetAddress host);

    /**
     * Sets the server port to connect to.
     *
     * @param port the server port.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverPort(@Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int port);

    /**
     * Sets the optional {@link MqttTransportConfig#getLocalAddress() local bind address}.
     * <p>
     * The address must be resolved.
     *
     * @param address the local bind address.
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B localAddress(@Nullable InetSocketAddress address);

    /**
     * Sets the optional local bind address.
     * <p>
     * The address must be resolvable.
     *
     * @param address the local bind address.
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B localAddress(@Nullable String address);

    /**
     * Sets the optional local bind address.
     *
     * @param address the local bind address
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B localAddress(@Nullable InetAddress address);

    /**
     * Sets the optional local bind port.
     *
     * @param port the local bind port.
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B localPort(@Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int port);

    /**
     * Sets the {@link MqttTransportConfig#getTlsConfig() transport layer security configuration} to the default
     * configuration.
     * <p>
     * This means that the systems default trust store, ciphers and protocols are used.
     *
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B tls();

    /**
     * Sets the optional {@link MqttTransportConfig#getTlsConfig() transport layer security configuration}.
     *
     * @param tlsConfig the transport layer security configuration or <code>null</code> to remove any previously set
     *                  transport layer security configuration.
     * @return the builder.
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
     */
    @CheckReturnValue
    MqttTlsConfigBuilder.@NotNull Nested<? extends B> tlsConfigWith();

    /**
     * Sets the {@link MqttTransportConfig#getWebSocketConfig() WebSocket transport configuration} to the default
     * configuration.
     *
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B webSocket();

    /**
     * Sets the optional {@link MqttTransportConfig#getWebSocketConfig() WebSocket transport configuration}.
     *
     * @param webSocketConfig the WebSocket transport configuration or <code>null</code> to remove any previously set
     *                        WebSocket transport configuration.
     * @return the builder.
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
     */
    @CheckReturnValue
    MqttWebSocketConfigBuilder.@NotNull Nested<? extends B> webSocketConfigWith();

    /**
     * Sets the optional {@link MqttTransportConfig#getProxyConfig() proxy configuration}.
     *
     * @param proxyConfig the proxy configuration or <code>null</code> to remove any previously set proxy
     *                    configuration.
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B proxyConfig(@Nullable MqttProxyConfig proxyConfig);

    /**
     * Fluent counterpart of {@link #proxyConfig(MqttProxyConfig)}.
     * <p>
     * Calling {@link MqttProxyConfigBuilder.Nested#applyProxyConfig()} on the returned builder has the effect of
     * extending the current proxy configuration.
     *
     * @return the fluent builder for the proxy configuration.
     * @see #proxyConfig(MqttProxyConfig)
     * @since 1.2
     */
    @CheckReturnValue
    MqttProxyConfigBuilder.@NotNull Nested<? extends B> proxyConfigWith();

    /**
     * Sets the {@link MqttTransportConfig#getSocketConnectTimeoutMs() timeout for connecting the socket to the
     * server}.
     * <p>
     * The timeout in milliseconds must be in the range: [0, {@link Integer#MAX_VALUE}].
     *
     * @param timeout  the timeout for connecting the socket to the server or <code>0</code> to disable the timeout.
     * @param timeUnit the time unit of the given timeout (this timeout only supports millisecond precision).
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B socketConnectTimeout(long timeout, @NotNull TimeUnit timeUnit);

    /**
     * Sets the {@link MqttTransportConfig#getMqttConnectTimeoutMs() timeout between sending the Connect and receiving
     * the ConnAck message}.
     * <p>
     * The timeout in milliseconds must be in the range: [0, {@link Integer#MAX_VALUE}].
     *
     * @param timeout  the timeout between sending the Connect and receiving the ConnAck message or <code>0</code> to
     *                 disable the timeout.
     * @param timeUnit the time unit of the given timeout (this timeout only supports millisecond precision).
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B mqttConnectTimeout(long timeout, @NotNull TimeUnit timeUnit);
}

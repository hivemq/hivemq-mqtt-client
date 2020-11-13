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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Builder base for a {@link MqttProxyConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.2
 */
@ApiStatus.NonExtendable
public interface MqttProxyConfigBuilderBase<B extends MqttProxyConfigBuilderBase<B>> {

    /**
     * Sets the {@link MqttProxyConfig#getProtocol() proxy protocol}.
     *
     * @param protocol the proxy protocol.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B protocol(@NotNull MqttProxyProtocol protocol);

    /**
     * Sets the {@link MqttProxyConfig#getAddress() proxy address} to connect to.
     *
     * @param address the proxy address.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B address(@NotNull InetSocketAddress address);

    /**
     * Sets the proxy host to connect to.
     *
     * @param host the proxy host.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B host(@NotNull String host);

    /**
     * Sets the proxy host to connect to.
     *
     * @param host the proxy host.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B host(@NotNull InetAddress host);

    /**
     * Sets the proxy port to connect to.
     *
     * @param port the proxy port.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B port(int port);

    /**
     * Sets the {@link MqttProxyConfig#getUsername() proxy username}.
     *
     * @param username the proxy username.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B username(@Nullable String username);

    /**
     * Sets the {@link MqttProxyConfig#getPassword() proxy password}.
     *
     * @param password the proxy username.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B password(@Nullable String password);

    /**
     * Sets the {@link MqttProxyConfig#getHandshakeTimeoutMs() proxy handshake timeout}.
     * <p>
     * The timeout in milliseconds must be in the range: [0, {@link Integer#MAX_VALUE}].
     *
     * @param timeout  the proxy handshake timeout or <code>0</code> to disable the timeout.
     * @param timeUnit the time unit of the given timeout (this timeout only supports millisecond precision).
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B handshakeTimeout(long timeout, @NotNull TimeUnit timeUnit);
}

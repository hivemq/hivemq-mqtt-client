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
 * Builder base for a {@link MqttProxyConfig}.
 *
 * @author Silvio Giebl
 * @since 1.2
 */
@DoNotImplement
public interface MqttProxyConfigBuilderBase<B extends MqttProxyConfigBuilderBase<B>> {

    /**
     * Sets the {@link MqttProxyConfig#getProxyProtocol() proxy protocol}.
     *
     * @param protocol the proxy protocol.
     * @return the builder.
     */
    @NotNull B proxyProtocol(@NotNull MqttProxyProtocol protocol);

    /**
     * Sets the {@link MqttProxyConfig#getProxyAddress() proxy address} to connect to.
     *
     * @param address the proxy address.
     * @return the builder.
     */
    @NotNull B proxyAddress(@NotNull InetSocketAddress address);

    /**
     * Sets the proxy host to connect to.
     *
     * @param host the proxy host.
     * @return the builder.
     */
    @NotNull B proxyHost(@NotNull String host);

    /**
     * Sets the proxy host to connect to.
     *
     * @param host the proxy host.
     * @return the builder.
     */
    @NotNull B proxyHost(@NotNull InetAddress host);

    /**
     * Sets the proxy port to connect to.
     *
     * @param port the proxy port.
     * @return the builder.
     */
    @NotNull B proxyPort(int port);

    /**
     * Sets the {@link MqttProxyConfig#getProxyUsername() proxy username}.
     *
     * @param username the proxy username.
     * @return the builder.
     */
    @NotNull B proxyUsername(@Nullable String username);

    /**
     * Sets the {@link MqttProxyConfig#getProxyPassword() proxy password}.
     *
     * @param password the proxy username.
     * @return the builder.
     */
    @NotNull B proxyPassword(@Nullable String password);
}

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

import com.hivemq.client.internal.mqtt.MqttProxyConfigImplBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Configuration for a proxy to use by {@link MqttClient MQTT clients}.
 *
 * @author Silvio Giebl
 * @since 1.2
 */
@ApiStatus.NonExtendable
public interface MqttProxyConfig {

    /**
     * The default proxy protocol.
     */
    @NotNull MqttProxyProtocol DEFAULT_PROXY_PROTOCOL = MqttProxyProtocol.SOCKS_5;
    /**
     * The default proxy host.
     */
    @NotNull String DEFAULT_PROXY_HOST = "localhost";
    /**
     * The default SOCKS proxy port.
     */
    int DEFAULT_SOCKS_PROXY_PORT = 1080;
    /**
     * The default HTTP proxy port.
     */
    int DEFAULT_HTTP_PROXY_PORT = 80;
    /**
     * The default proxy handshake timeout in milliseconds.
     */
    int DEFAULT_HANDSHAKE_TIMEOUT_MS = 10_000;

    /**
     * Creates a builder for a proxy configuration.
     *
     * @return the created builder for a proxy configuration.
     */
    static @NotNull MqttProxyConfigBuilder builder() {
        return new MqttProxyConfigImplBuilder.Default();
    }

    /**
     * @return the proxy protocol.
     */
    @NotNull MqttProxyProtocol getProtocol();

    /**
     * @return the proxy address to connect to.
     */
    @NotNull InetSocketAddress getAddress();

    /**
     * @return the optional proxy username.
     */
    @NotNull Optional<String> getUsername();

    /**
     * @return the optional proxy password.
     */
    @NotNull Optional<String> getPassword();

    /**
     * @return the proxy handshake timeout in milliseconds.
     */
    int getHandshakeTimeoutMs();

    /**
     * Creates a builder for extending this proxy configuration.
     *
     * @return the created builder.
     */
    @NotNull MqttProxyConfigBuilder extend();
}

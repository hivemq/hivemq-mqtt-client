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

import com.hivemq.client.internal.mqtt.MqttTransportConfigImplBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Configuration for a transport to use by {@link MqttClient MQTT clients}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.NonExtendable
public interface MqttTransportConfig {

    /**
     * The default server host.
     */
    @NotNull String DEFAULT_SERVER_HOST = "localhost";
    /**
     * The default server port.
     */
    int DEFAULT_SERVER_PORT = 1883;
    /**
     * The default server port when using TLS.
     */
    int DEFAULT_SERVER_PORT_TLS = 8883;
    /**
     * The default server port when using WebSocket.
     */
    int DEFAULT_SERVER_PORT_WEBSOCKET = 80;
    /**
     * The default server port when using TLS and WebSocket.
     */
    int DEFAULT_SERVER_PORT_WEBSOCKET_TLS = 443;
    /**
     * The default timeout for connecting the socket to the server in milliseconds.
     *
     * @since 1.2
     */
    int DEFAULT_SOCKET_CONNECT_TIMEOUT_MS = 10_000;
    /**
     * The default timeout between sending the Connect and receiving the ConnAck message in milliseconds.
     *
     * @since 1.2
     */
    int DEFAULT_MQTT_CONNECT_TIMEOUT_MS = 60_000;

    /**
     * Creates a builder for a transport configuration.
     *
     * @return the created builder for a transport configuration.
     */
    static @NotNull MqttTransportConfigBuilder builder() {
        return new MqttTransportConfigImplBuilder.Default();
    }

    /**
     * @return the server address to connect to.
     */
    @NotNull InetSocketAddress getServerAddress();

    /**
     * @return the optional local bind address.
     * @since 1.2
     */
    @NotNull Optional<InetSocketAddress> getLocalAddress();

    /**
     * @return the optional transport layer security configuration.
     */
    @NotNull Optional<MqttTlsConfig> getTlsConfig();

    /**
     * @return the optional WebSocket transport configuration.
     */
    @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig();

    /**
     * @return the optional proxy configuration.
     * @since 1.2
     */
    @NotNull Optional<MqttProxyConfig> getProxyConfig();

    /**
     * @return the timeout for connecting the socket to the server in milliseconds.
     * @since 1.2
     */
    @Range(from = 0, to = Integer.MAX_VALUE) int getSocketConnectTimeoutMs();

    /**
     * @return the timeout between sending the Connect and receiving the ConnAck message in milliseconds.
     * @since 1.2
     */
    @Range(from = 0, to = Integer.MAX_VALUE) int getMqttConnectTimeoutMs();

    /**
     * Creates a builder for extending this transport configuration.
     *
     * @return the created builder.
     */
    @NotNull MqttTransportConfigBuilder extend();
}

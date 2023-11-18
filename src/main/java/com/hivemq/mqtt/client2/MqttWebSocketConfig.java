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

package com.hivemq.mqtt.client2;

import com.hivemq.mqtt.client2.internal.MqttWebSocketConfigImplBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Configuration for a WebSocket transport to use by {@link MqttClient MQTT clients}.
 *
 * @author Christian Hoff
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttWebSocketConfig {

    /**
     * The default WebSocket path.
     */
    @NotNull String DEFAULT_PATH = "";
    /**
     * The default WebSocket query.
     */
    @NotNull String DEFAULT_QUERY = "";
    /**
     * The default WebSocket subprotocol.
     * <p>
     * See the <a href="https://www.iana.org/assignments/websocket/websocket.xml#subprotocol-name">WebSocket Subprotocol
     * Name Registry</a>
     */
    @NotNull String DEFAULT_SUBPROTOCOL = "mqtt";
    /**
     * The default WebSocket handshake timeout in milliseconds.
     *
     * @since 1.2
     */
    int DEFAULT_HANDSHAKE_TIMEOUT_MS = 10_000;

    /**
     * Creates a builder for a WebSocket configuration.
     *
     * @return the created builder for a WebSocket configuration.
     */
    static @NotNull MqttWebSocketConfigBuilder builder() {
        return new MqttWebSocketConfigImplBuilder.Default();
    }

    /**
     * @return the WebSocket path.
     */
    @NotNull String getPath();

    /**
     * @return the WebSocket query.
     */
    @NotNull String getQuery();

    /**
     * @return the WebSocket subprotocol.
     */
    @NotNull String getSubprotocol();

    /**
     * @return the WebSocket handshake timeout in milliseconds.
     * @since 1.2
     */
    @Range(from = 0, to = Integer.MAX_VALUE) int getHandshakeTimeoutMs();

    /**
     * Creates a builder for extending this WebSocket configuration.
     *
     * @return the created builder.
     * @since 1.1
     */
    @NotNull MqttWebSocketConfigBuilder extend();
}

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
 */

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.MqttWebSocketConfigImplBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for a WebSocket transport to use by {@link MqttClient MQTT clients}.
 *
 * @author Christian Hoff
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttWebSocketConfig {

    /**
     * The default WebSocket server path.
     */
    @NotNull String DEFAULT_SERVER_PATH = "";
    /**
     * The default WebSocket query string.
     */
    @NotNull String DEFAULT_QUERY_STRING = "";
    /**
     * The default WebSocket subprotocol.
     * <p>
     * See the <a href="https://www.iana.org/assignments/websocket/websocket.xml#subprotocol-name">WebSocket Subprotocol
     * Name Registry</a>
     */
    @NotNull String DEFAULT_MQTT_SUBPROTOCOL = "mqtt";

    /**
     * Creates a builder for a WebSocket configuration.
     *
     * @return the created builder for a WebSocket configuration.
     */
    static @NotNull MqttWebSocketConfigBuilder builder() {
        return new MqttWebSocketConfigImplBuilder.Default();
    }

    /**
     * @return the WebSocket server path.
     */
    @NotNull String getServerPath();

    /**
     * @return the WebSocket query string.
     */
    @NotNull String getQueryString();
    
    /**
     * @return the WebSocket subprotocol.
     */
    @NotNull String getSubprotocol();

    /**
     * Creates a builder for extending this WebSocket configuration.
     *
     * @return the created builder.
     * @since 1.1
     */
    @NotNull MqttWebSocketConfigBuilder extend();
}

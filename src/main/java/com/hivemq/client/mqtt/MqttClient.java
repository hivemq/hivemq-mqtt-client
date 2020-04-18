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

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.MqttRxClientBuilderBase;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface for MQTT clients.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClient {

    /**
     * The default server host.
     */
    @NotNull String DEFAULT_SERVER_HOST = "localhost";
    /**
     * The default server port.
     */
    int DEFAULT_SERVER_PORT = 1883;
    /**
     * The default server port when using SSL.
     */
    int DEFAULT_SERVER_PORT_SSL = 8883;
    /**
     * The default server port when using WebSocket.
     */
    int DEFAULT_SERVER_PORT_WEBSOCKET = 80;
    /**
     * The default server port when using SSL and WebSocket.
     */
    int DEFAULT_SERVER_PORT_WEBSOCKET_SSL = 443;

    /**
     * Creates a builder for a MQTT client.
     *
     * @return the created builder for a MQTT client.
     */
    static @NotNull MqttClientBuilder builder() {
        return new MqttRxClientBuilderBase.Choose();
    }

    /**
     * @return the configuration of this client.
     */
    @NotNull MqttClientConfig getConfig();

    /**
     * @return the state of this client.
     */
    default @NotNull MqttClientState getState() {
        return getConfig().getState();
    }
}

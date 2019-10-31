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
import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImplBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Configuration for a transport to use by {@link MqttClient MQTT clients}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientTransportConfig {

    /**
     * Creates a builder for a transport configuration.
     *
     * @return the created builder for a transport configuration.
     */
    static @NotNull MqttClientTransportConfigBuilder builder() {
        return new MqttClientTransportConfigImplBuilder.Default();
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
     * @return the optional secure transport configuration.
     */
    @NotNull Optional<MqttClientSslConfig> getSslConfig();

    /**
     * @return the optional WebSocket transport configuration.
     */
    @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig();

    /**
     * Creates a builder for extending this transport configuration.
     *
     * @return the created builder.
     */
    @NotNull MqttClientTransportConfigBuilder extend();
}

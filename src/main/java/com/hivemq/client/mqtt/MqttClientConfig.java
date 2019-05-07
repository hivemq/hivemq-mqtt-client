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
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Configuration of a {@link MqttClient}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientConfig {

    /**
     * @return the MQTT version of the client.
     */
    @NotNull MqttVersion getMqttVersion();

    /**
     * Returns the identifier of the client. It is not present if it was not specified by the client itself but is
     * assigned by the server and the server did not tell the assigned Client Identifier yet.
     *
     * @return the (currently not present) identifier of the client.
     */
    @NotNull Optional<MqttClientIdentifier> getClientIdentifier();

    /**
     * @return the server address the client connects to or is connected to.
     * @since 1.1
     */
    @NotNull InetSocketAddress getServerAddress();

    /**
     * @return the server host the clients connects to or is connected to.
     */
    @NotNull String getServerHost();

    /**
     * @return the server port the client connects to or is connected to.
     */
    int getServerPort();

    /**
     * @return the executor configuration of the client.
     */
    @NotNull MqttClientExecutorConfig getExecutorConfig();

    /**
     * @return the optional secure transport configuration of the client.
     */
    @NotNull Optional<MqttClientSslConfig> getSslConfig();

    /**
     * @return the optional WebSocket configuration of the client.
     */
    @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig();

    /**
     * @return the state of the client.
     */
    @NotNull MqttClientState getState();

    /**
     * Returns the optional connection configuration of the client. It is present if the client is connected and has
     * received the ConnAck message.
     *
     * @return the optional connection configuration of the client.
     */
    @NotNull Optional<? extends MqttClientConnectionConfig> getConnectionConfig();
}

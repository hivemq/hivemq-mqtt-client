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
import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

/**
 * Configuration of an {@link MqttClient}.
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
     * @return the server address the client connects to.
     * @since 1.1
     */
    default @NotNull InetSocketAddress getServerAddress() {
        return getTransportConfig().getServerAddress();
    }

    /**
     * @return the server host the clients connects to.
     */
    default @NotNull String getServerHost() {
        return getServerAddress().getHostString();
    }

    /**
     * @return the server port the client connects to.
     */
    default int getServerPort() {
        return getServerAddress().getPort();
    }

    /**
     * @return the optional transport layer security configuration of the client.
     */
    default @NotNull Optional<MqttClientTlsConfig> getTlsConfig() {
        return getTransportConfig().getTlsConfig();
    }

    /**
     * @return the optional WebSocket configuration of the client.
     */
    default @NotNull Optional<MqttWebSocketConfig> getWebSocketConfig() {
        return getTransportConfig().getWebSocketConfig();
    }

    /**
     * @return the transport configuration of the client.
     * @since 1.1
     */
    @NotNull MqttTransportConfig getTransportConfig();

    /**
     * @return the executor configuration of the client.
     */
    @NotNull MqttClientExecutorConfig getExecutorConfig();

    /**
     * @return the optional automatic reconnect strategy of the client.
     * @since 1.1
     */
    @NotNull Optional<MqttClientAutoReconnect> getAutomaticReconnect();

    /**
     * @return the listeners which are notified (in the order of the list) when this client is connected (a successful
     *         ConnAck message is received).
     * @since 1.1
     */
    @Immutable @NotNull List<@NotNull MqttClientConnectedListener> getConnectedListeners();

    /**
     * @return the listeners which are notified (in the order of the list) when this client is disconnected (with or
     *         without a Disconnect message) or the connection fails.
     * @since 1.1
     */
    @Immutable @NotNull List<@NotNull MqttClientDisconnectedListener> getDisconnectedListeners();

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

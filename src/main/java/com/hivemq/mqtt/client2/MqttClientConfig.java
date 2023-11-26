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

import com.hivemq.mqtt.client2.datatypes.MqttClientIdentifier;
import com.hivemq.mqtt.client2.lifecycle.MqttAutoReconnect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Configuration of an {@link MqttClient}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
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
    @NotNull Optional<MqttClientIdentifier> getIdentifier();

    /**
     * @return the transport configuration of the client.
     * @since 1.1
     */
    @NotNull MqttTransportConfig getTransportConfig();

    /**
     * @return the executor configuration of the client.
     */
    @NotNull MqttExecutorConfig getExecutorConfig();

    /**
     * @return the optional automatic reconnect strategy of the client.
     * @since 1.1
     */
    @NotNull Optional<MqttAutoReconnect> getAutomaticReconnect();

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

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

package com.hivemq.client.mqtt.lifecycle;

import com.hivemq.client.mqtt.MqttClientConfig;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Provides context about the client that is now disconnected, the cause for disconnection and allows reconnecting.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.NonExtendable
public interface MqttDisconnectedContext {

    /**
     * @return the config of the client that is now disconnected.
     */
    @NotNull MqttClientConfig getClientConfig();

    /**
     * @return the source which triggered the disconnection.
     */
    @NotNull MqttDisconnectSource getSource();

    /**
     * Returns the cause for disconnection.
     * <p>
     * This can be:
     * <ul>
     *   <li>{@link com.hivemq.client.mqtt.exceptions.ConnectionFailedException ConnectionFailedException} if a connect
     *     attempt failed
     *   <li>{@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException Mqtt3ConnAckException} or {@link
     *     com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException Mqtt5ConnAckException} (depending on the MQTT
     *     version of the client) if the ConnAck message contained an error code, which means that the connect was
     *     rejected
     *   <li>{@link com.hivemq.client.mqtt.exceptions.ConnectionClosedException ConnectionClosedException} if the
     *     connection was closed without sending a Disconnect message (use {@link #getSource()} to determine if the
     *     server or the client closed the connection)
     *   <li>{@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException Mqtt3DisconnectException} or {@link
     *     com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException Mqtt5DisconnectException} (depending on the
     *     MQTT version of the client) if the connection was closed with a Disconnect message (use {@link #getSource()}
     *     to determine if the server, the user or the client sent the Disconnect message)
     * </ul>
     * <p>
     * Example: You can use the following code to extract the Disconnect message:
     * <pre>
     * TypeSwitch.when(context.getCause()).is(Mqtt5DisconnectException.class, disconnectException -&gt; {
     *     Mqtt5Disconnect disconnect = disconnectException.getMqttMessage();
     * }).is(Mqtt3DisconnectException.class, disconnectException -&gt; {
     *     Mqtt3Disconnect disconnect = disconnectException.getMqttMessage();
     * });
     * </pre>
     *
     * @return the cause for disconnection.
     */
    @NotNull Throwable getCause();

    /**
     * @return the reconnector which can be used for reconnecting.
     */
    @NotNull MqttReconnector getReconnector();
}

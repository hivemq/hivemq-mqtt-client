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

import org.jetbrains.annotations.NotNull;

/**
 * Listener which is notified when a client is disconnected (with or without a Disconnect message) or the connection
 * fails.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@FunctionalInterface
public interface MqttDisconnectedListener {

    /**
     * Listener method which is notified in the following cases:
     * <ul>
     *   <li>A client was disconnected (with or without a Disconnect message, by the server, client or user) or the
     *     connection failed. The client state will still be {@link com.hivemq.client.mqtt.MqttClientState#CONNECTED
     *     CONNECTED} and the {@link com.hivemq.client.mqtt.MqttClientConnectionConfig MqttClientConnectionConfig} will
     *     still be present.
     *   <li>A connect attempt by the user failed. The client state will still be
     *     {@link com.hivemq.client.mqtt.MqttClientState#CONNECTING CONNECTING}.
     *   <li>A reconnect attempt by the client failed. The client state will still be
     *     {@link com.hivemq.client.mqtt.MqttClientState#CONNECTING_RECONNECT CONNECTING_RECONNECT}.
     * </ul>
     * The client state will be updated after all {@link MqttDisconnectedListener}s
     * are called to
     * <ul>
     *   <li>{@link com.hivemq.client.mqtt.MqttClientState#DISCONNECTED DISCONNECTED} or
     *   <li>{@link com.hivemq.client.mqtt.MqttClientState#DISCONNECTED_RECONNECT DISCONNECTED_RECONNECT} if the client
     *     is instructed to reconnect.
     * </ul>
     * <p>
     * This method must not block. If you want to reconnect you have to use the supplied {@link
     * MqttDisconnectedContext#getReconnector()}.
     *
     * @param context provides context about the client that is now disconnected, the cause for disconnection and allows
     *                reconnecting.
     */
    void onDisconnected(@NotNull MqttDisconnectedContext context);
}

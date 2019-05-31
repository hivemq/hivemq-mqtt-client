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

package com.hivemq.client.mqtt.lifecycle;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttClientConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Listener which is notified when the client is disconnected (with or without a Disconnect message) or the connection
 * fails.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@FunctionalInterface
public interface MqttClientDisconnectedListener {

    /**
     * Listener method which is notified when the client is disconnected (with or without a Disconnect message) or the
     * connection fails.
     * <p>
     * This method must not block. If you want to reconnect you have to use the supplied {@link
     * Context#getReconnector()}.
     * <p>
     * Important: the {@link com.hivemq.client.mqtt.MqttClientState client state} will still be
     * <ul>
     * <li>{@link com.hivemq.client.mqtt.MqttClientState#CONNECTED CONNECTED} if the client was disconnected,</li>
     * <li>{@link com.hivemq.client.mqtt.MqttClientState#CONNECTING CONNECTING} if a connect attempt failed or</li>
     * <li>{@link com.hivemq.client.mqtt.MqttClientState#CONNECTING_RECONNECT CONNECTING_RECONNECT} if a reconnect
     * attempt failed</li>
     * </ul>
     * and will be updated after all {@link MqttClientDisconnectedListener MqttClientDisconnectedListeners} are called
     * to
     * <ul>
     * <li>{@link com.hivemq.client.mqtt.MqttClientState#DISCONNECTED DISCONNECTED} or</li>
     * <li>{@link com.hivemq.client.mqtt.MqttClientState#DISCONNECTED_RECONNECT DISCONNECTED_RECONNECT} if the client
     * is instructed to reconnect.</li>
     * </ul>
     *
     * @param context provides context about the client and the cause for disconnection and allows reconnecting.
     */
    void onDisconnected(@NotNull Context context);

    /**
     * Provides context about the client and the cause for disconnection and allows reconnecting.
     */
    @DoNotImplement
    interface Context {

        /**
         * @return the config of the client that is disconnected.
         */
        @NotNull MqttClientConfig getClientConfig();

        /**
         * @return the source which triggered the disconnection.
         */
        @NotNull MqttDisconnectSource getSource();

        /**
         * @return the cause for disconnection.
         */
        @NotNull Throwable getCause();

        /**
         * @return the reconnector which can be used for reconnecting.
         */
        @NotNull MqttClientReconnector getReconnector();
    }
}

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

package com.hivemq.client.internal.mqtt.lifecycle;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.lifecycle.mqtt3.Mqtt3ClientDisconnectedContextView;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.lifecycle.Mqtt5ClientDisconnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttClientDisconnectedContextImpl implements Mqtt5ClientDisconnectedContext {

    public static @NotNull MqttClientDisconnectedContext of(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull MqttClientReconnector reconnector) {

        if (clientConfig.getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            return Mqtt3ClientDisconnectedContextView.of(clientConfig, source, cause, reconnector);
        }
        return new MqttClientDisconnectedContextImpl(clientConfig, source, cause, reconnector);
    }

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttDisconnectSource source;
    private final @NotNull Throwable cause;
    private final @NotNull MqttClientReconnector reconnector;

    private MqttClientDisconnectedContextImpl(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull MqttClientReconnector reconnector) {

        this.clientConfig = clientConfig;
        this.source = source;
        this.cause = cause;
        this.reconnector = reconnector;
    }

    @Override
    public @NotNull MqttClientConfig getClientConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull MqttDisconnectSource getSource() {
        return source;
    }

    @Override
    public @NotNull Throwable getCause() {
        return cause;
    }

    @Override
    public @NotNull MqttClientReconnector getReconnector() {
        return reconnector;
    }
}

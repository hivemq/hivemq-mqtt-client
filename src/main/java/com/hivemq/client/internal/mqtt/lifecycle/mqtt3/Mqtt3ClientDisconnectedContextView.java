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

package com.hivemq.client.internal.mqtt.lifecycle.mqtt3;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.client.internal.mqtt.lifecycle.MqttClientReconnector;
import com.hivemq.client.internal.mqtt.mqtt3.Mqtt3ClientConfigView;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt3.lifecycle.Mqtt3ClientDisconnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientDisconnectedContextView implements Mqtt3ClientDisconnectedContext {

    public static @NotNull MqttClientDisconnectedContext of(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause, final @NotNull MqttClientReconnector reconnector) {

        return new Mqtt3ClientDisconnectedContextView(new Mqtt3ClientConfigView(clientConfig), source,
                Mqtt3ExceptionFactory.map(cause), new Mqtt3ClientReconnectorView(reconnector));
    }

    private final @NotNull Mqtt3ClientConfigView clientConfig;
    private final @NotNull MqttDisconnectSource source;
    private final @NotNull Throwable cause;
    private final @NotNull Mqtt3ClientReconnectorView reconnector;

    private Mqtt3ClientDisconnectedContextView(
            final @NotNull Mqtt3ClientConfigView clientConfig, final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause, final @NotNull Mqtt3ClientReconnectorView reconnector) {

        this.clientConfig = clientConfig;
        this.source = source;
        this.cause = cause;
        this.reconnector = reconnector;
    }

    @Override
    public @NotNull Mqtt3ClientConfigView getClientConfig() {
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
    public @NotNull Mqtt3ClientReconnectorView getReconnector() {
        return reconnector;
    }
}

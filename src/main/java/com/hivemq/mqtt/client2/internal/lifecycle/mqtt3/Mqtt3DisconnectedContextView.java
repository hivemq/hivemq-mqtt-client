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

package com.hivemq.mqtt.client2.internal.lifecycle.mqtt3;

import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttReconnector;
import com.hivemq.mqtt.client2.internal.mqtt3.Mqtt3ClientConfigView;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectSource;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectedContext;
import com.hivemq.mqtt.client2.mqtt3.lifecycle.Mqtt3DisconnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt3DisconnectedContextView implements Mqtt3DisconnectedContext {

    public static @NotNull MqttDisconnectedContext of(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull MqttReconnector reconnector) {

        return new Mqtt3DisconnectedContextView(new Mqtt3ClientConfigView(clientConfig), source,
                Mqtt3ExceptionFactory.map(cause), new Mqtt3ReconnectorView(reconnector));
    }

    private final @NotNull Mqtt3ClientConfigView clientConfig;
    private final @NotNull MqttDisconnectSource source;
    private final @NotNull Throwable cause;
    private final @NotNull Mqtt3ReconnectorView reconnector;

    private Mqtt3DisconnectedContextView(
            final @NotNull Mqtt3ClientConfigView clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull Mqtt3ReconnectorView reconnector) {

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
    public @NotNull Mqtt3ReconnectorView getReconnector() {
        return reconnector;
    }
}
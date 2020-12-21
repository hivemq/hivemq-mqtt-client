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

package com.hivemq.client2.internal.mqtt.lifecycle;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.lifecycle.mqtt3.Mqtt3ConnectedContextView;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client2.mqtt.MqttVersion;
import com.hivemq.client2.mqtt.lifecycle.MqttConnectedContext;
import com.hivemq.client2.mqtt.mqtt5.lifecycle.Mqtt5ConnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttConnectedContextImpl implements Mqtt5ConnectedContext {

    public static @NotNull MqttConnectedContext of(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAck connAck) {

        if (clientConfig.getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            return Mqtt3ConnectedContextView.of(clientConfig, connect, connAck);
        }
        return new MqttConnectedContextImpl(clientConfig, connect, connAck);
    }

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;
    private final @NotNull MqttConnAck connAck;

    private MqttConnectedContextImpl(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAck connAck) {

        this.clientConfig = clientConfig;
        this.connect = connect;
        this.connAck = connAck;
    }

    @Override
    public @NotNull MqttClientConfig getClientConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull MqttConnect getConnect() {
        return connect;
    }

    @Override
    public @NotNull MqttConnAck getConnAck() {
        return connAck;
    }
}

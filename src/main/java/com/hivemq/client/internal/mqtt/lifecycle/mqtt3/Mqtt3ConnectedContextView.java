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

package com.hivemq.client.internal.mqtt.lifecycle.mqtt3;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.mqtt3.Mqtt3ClientConfigView;
import com.hivemq.client.mqtt.lifecycle.MqttConnectedContext;
import com.hivemq.client.mqtt.mqtt3.lifecycle.Mqtt3ConnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnectedContextView implements Mqtt3ConnectedContext {

    public static @NotNull MqttConnectedContext of(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAck connAck) {

        return new Mqtt3ConnectedContextView(
                new Mqtt3ClientConfigView(clientConfig), Mqtt3ConnectView.of(connect), Mqtt3ConnAckView.of(connAck));
    }

    private final @NotNull Mqtt3ClientConfigView clientConfig;
    private final @NotNull Mqtt3ConnectView connect;
    private final @NotNull Mqtt3ConnAckView connAck;

    private Mqtt3ConnectedContextView(
            final @NotNull Mqtt3ClientConfigView clientConfig,
            final @NotNull Mqtt3ConnectView connect,
            final @NotNull Mqtt3ConnAckView connAck) {

        this.clientConfig = clientConfig;
        this.connect = connect;
        this.connAck = connAck;
    }

    @Override
    public @NotNull Mqtt3ClientConfigView getClientConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull Mqtt3ConnectView getConnect() {
        return connect;
    }

    @Override
    public @NotNull Mqtt3ConnAckView getConnAck() {
        return connAck;
    }
}

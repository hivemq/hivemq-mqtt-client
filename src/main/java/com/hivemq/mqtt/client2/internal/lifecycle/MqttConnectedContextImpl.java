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

package com.hivemq.mqtt.client2.internal.lifecycle;

import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnAck;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.mqtt5.lifecycle.Mqtt5ConnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttConnectedContextImpl implements Mqtt5ConnectedContext {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;
    private final @NotNull MqttConnAck connAck;

    public MqttConnectedContextImpl(
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

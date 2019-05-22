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
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientReconnector {

    @NotNull MqttClientReconnector reconnect(boolean reconnect);

    boolean isReconnect();

    int getAttempts();

    @NotNull MqttClientReconnector delay(long delay, @NotNull TimeUnit timeUnit);

    long getDelay(@NotNull TimeUnit timeUnit);

    @NotNull MqttClientReconnector serverAddress(@NotNull InetSocketAddress address);

    @NotNull MqttClientReconnector serverHost(@NotNull String host);

    @NotNull MqttClientReconnector serverHost(@NotNull InetAddress host);

    @NotNull MqttClientReconnector serverPort(int port);

    @NotNull InetSocketAddress getServerAddress();
}

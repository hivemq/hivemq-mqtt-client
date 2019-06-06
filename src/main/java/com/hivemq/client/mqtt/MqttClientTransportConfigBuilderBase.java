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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientTransportConfigBuilderBase<B extends MqttClientTransportConfigBuilderBase<B>> {

    @NotNull B serverAddress(@NotNull InetSocketAddress address);

    @NotNull B serverHost(@NotNull String host);

    @NotNull B serverHost(@NotNull InetAddress host);

    @NotNull B serverPort(int port);

    @NotNull B sslWithDefaultConfig();

    @NotNull B sslConfig(@Nullable MqttClientSslConfig sslConfig);

    @NotNull MqttClientSslConfigBuilder.Nested<? extends B> sslConfig();

    @NotNull B webSocketWithDefaultConfig();

    @NotNull B webSocketConfig(@Nullable MqttWebSocketConfig webSocketConfig);

    @NotNull MqttWebSocketConfigBuilder.Nested<? extends B> webSocketConfig();
}

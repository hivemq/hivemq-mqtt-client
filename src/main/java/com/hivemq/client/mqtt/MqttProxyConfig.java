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
import com.hivemq.client.internal.mqtt.MqttProxyConfigImplBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @author Silvio Giebl
 * @since 1.2
 */
@DoNotImplement
public interface MqttProxyConfig {

    @NotNull MqttProxyType DEFAULT_PROXY_TYPE = MqttProxyType.SOCKS_5;
    @NotNull String DEFAULT_PROXY_HOST = "localhost";
    int DEFAULT_SOCKS_PROXY_PORT = 1080;
    int DEFAULT_HTTP_PROXY_PORT = 80;

    static @NotNull MqttProxyConfigBuilder builder() {
        return new MqttProxyConfigImplBuilder.Default();
    }

    @NotNull MqttProxyType getProxyType();

    @NotNull InetSocketAddress getProxyAddress();

    @NotNull Optional<String> getProxyUsername();

    @NotNull Optional<String> getProxyPassword();

    @NotNull MqttProxyConfigBuilder extend();
}

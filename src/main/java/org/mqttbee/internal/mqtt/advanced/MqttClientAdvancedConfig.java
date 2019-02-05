/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.internal.mqtt.advanced;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import org.mqttbee.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfig;

/**
 * @author Silvio Giebl
 */
public class MqttClientAdvancedConfig implements Mqtt5ClientAdvancedConfig {

    public static final @NotNull MqttClientAdvancedConfig DEFAULT = new MqttClientAdvancedConfig(false, null);

    private final boolean allowServerReAuth;
    private final @Nullable MqttClientInterceptors interceptors;

    MqttClientAdvancedConfig(final boolean allowServerReAuth, final @Nullable MqttClientInterceptors interceptors) {
        this.allowServerReAuth = allowServerReAuth;
        this.interceptors = interceptors;
    }

    @Override
    public boolean isAllowServerReAuth() {
        return allowServerReAuth;
    }

    @Override
    public @Nullable MqttClientInterceptors getInterceptors() {
        return interceptors;
    }
}

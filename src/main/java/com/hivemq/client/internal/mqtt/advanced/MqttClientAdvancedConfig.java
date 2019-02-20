/*
 * Copyright 2018 The HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.advanced;

import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttClientAdvancedConfig implements Mqtt5ClientAdvancedConfig {

    public static final @NotNull MqttClientAdvancedConfig DEFAULT = new MqttClientAdvancedConfig(false, false, null);

    private final boolean allowServerReAuth;
    private final boolean validatePayloadFormat;
    private final @Nullable MqttClientInterceptors interceptors;

    MqttClientAdvancedConfig(
            final boolean allowServerReAuth, final boolean validatePayloadFormat,
            final @Nullable MqttClientInterceptors interceptors) {

        this.allowServerReAuth = allowServerReAuth;
        this.validatePayloadFormat = validatePayloadFormat;
        this.interceptors = interceptors;
    }

    @Override
    public boolean isAllowServerReAuth() {
        return allowServerReAuth;
    }

    @Override
    public boolean isValidatePayloadFormat() {
        return validatePayloadFormat;
    }

    @Override
    public @Nullable MqttClientInterceptors getInterceptors() {
        return interceptors;
    }
}

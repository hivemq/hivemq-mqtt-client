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

package com.hivemq.client.internal.mqtt.advanced;

import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5AdvancedConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
public class MqttAdvancedConfig implements Mqtt5AdvancedConfig {

    public static final @NotNull MqttAdvancedConfig DEFAULT = new MqttAdvancedConfig(false, false, null);

    private final boolean allowServerReAuth;
    private final boolean validatePayloadFormat;
    private final @Nullable MqttClientInterceptors interceptors;

    MqttAdvancedConfig(
            final boolean allowServerReAuth,
            final boolean validatePayloadFormat,
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

    @Override
    public MqttAdvancedConfigBuilder.@NotNull Default extend() {
        return new MqttAdvancedConfigBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttAdvancedConfig)) {
            return false;
        }
        final MqttAdvancedConfig that = (MqttAdvancedConfig) o;

        return (allowServerReAuth == that.allowServerReAuth) && (validatePayloadFormat == that.validatePayloadFormat) &&
                Objects.equals(interceptors, that.interceptors);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(allowServerReAuth);
        result = 31 * result + Boolean.hashCode(validatePayloadFormat);
        result = 31 * result + Objects.hashCode(interceptors);
        return result;
    }
}

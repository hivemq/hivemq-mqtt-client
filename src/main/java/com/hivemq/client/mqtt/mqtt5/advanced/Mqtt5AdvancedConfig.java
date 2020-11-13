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

package com.hivemq.client.mqtt.mqtt5.advanced;

import com.hivemq.client.internal.mqtt.advanced.MqttAdvancedConfigBuilder;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Advanced configuration of an {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5AdvancedConfig {

    /**
     * Creates a builder for an advanced configuration.
     *
     * @return the created builder for an advanced configuration.
     */
    static @NotNull Mqtt5AdvancedConfigBuilder builder() {
        return new MqttAdvancedConfigBuilder.Default();
    }

    /**
     * @return whether server re-authentication is allowed.
     */
    boolean isAllowServerReAuth();

    /**
     * Returns whether the payload format is validated if {@link com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish#getPayloadFormatIndicator
     * Mqtt5Publish#getPayloadFormatIndicator()} is present.
     *
     * @return whether the payload format is validated.
     */
    boolean isValidatePayloadFormat();

    /**
     * @return the optional interceptors of messages.
     */
    @Nullable Mqtt5ClientInterceptors getInterceptors();

    /**
     * Creates a builder for extending this advanced configuration.
     *
     * @return the created builder.
     * @since 1.1
     */
    @NotNull Mqtt5AdvancedConfigBuilder extend();
}

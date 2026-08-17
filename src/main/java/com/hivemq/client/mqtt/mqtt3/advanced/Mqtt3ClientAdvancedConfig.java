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

package com.hivemq.client.mqtt.mqtt3.advanced;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.advanced.mqtt3.Mqtt3ClientAdvancedConfigViewBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Advanced configuration of an {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client Mqtt3Client}.
 *
 * @author Abdullah Imal
 * @since 1.4
 */
@DoNotImplement
public interface Mqtt3ClientAdvancedConfig {

    /**
     * Creates a builder for an advanced configuration.
     *
     * @return the created builder for an advanced configuration.
     */
    static @NotNull Mqtt3ClientAdvancedConfigBuilder builder() {
        return new Mqtt3ClientAdvancedConfigViewBuilder.Default();
    }

    /**
     * @return the maximum number of publish sources that are processed concurrently.
     */
    int maxConcurrentPublishes();

    /**
     * Creates a builder for extending this advanced configuration.
     *
     * @return the created builder.
     */
    @NotNull Mqtt3ClientAdvancedConfigBuilder extend();
}

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

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder base for a {@link Mqtt3ClientAdvancedConfig}.
 *
 * @param <B> the type of the builder.
 * @author Abdullah Imal
 * @since 1.4
 */
@DoNotImplement
public interface Mqtt3ClientAdvancedConfigBuilderBase<B extends Mqtt3ClientAdvancedConfigBuilderBase<B>> {

    /**
     * Sets the {@link Mqtt3ClientAdvancedConfig#maxConcurrentPublishes() maximum number of publish sources that are
     * processed concurrently}.
     *
     * @param maxConcurrentPublishes the maximum number of publish sources that are processed concurrently. Must be at
     *                               least 1.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B maxConcurrentPublishes(int maxConcurrentPublishes);
}

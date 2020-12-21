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

package com.hivemq.client2.mqtt.mqtt5.advanced;

import com.hivemq.client2.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5AdvancedConfig}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5AdvancedConfigBuilder extends Mqtt5AdvancedConfigBuilderBase<Mqtt5AdvancedConfigBuilder> {

    /**
     * Builds the {@link Mqtt5AdvancedConfig}.
     *
     * @return the built {@link Mqtt5AdvancedConfig}.
     */
    @CheckReturnValue
    @NotNull Mqtt5AdvancedConfig build();

    /**
     * Builder for a {@link Mqtt5AdvancedConfig} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5AdvancedConfig} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt5AdvancedConfigBuilderBase<Nested<P>> {

        /**
         * Builds the {@link Mqtt5AdvancedConfig} and applies it to the parent.
         *
         * @return the result when the built {@link Mqtt5AdvancedConfig} is applied to the parent.
         */
        @NotNull P applyAdvancedConfig();
    }
}

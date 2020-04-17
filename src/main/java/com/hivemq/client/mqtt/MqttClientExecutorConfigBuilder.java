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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link MqttClientExecutorConfig}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientExecutorConfigBuilder
        extends MqttClientExecutorConfigBuilderBase<MqttClientExecutorConfigBuilder> {

    /**
     * Builds the {@link MqttClientExecutorConfig}.
     *
     * @return the built {@link MqttClientExecutorConfig}.
     */
    @CheckReturnValue
    @NotNull MqttClientExecutorConfig build();

    /**
     * Builder for a {@link MqttClientExecutorConfig} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link MqttClientExecutorConfig} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends MqttClientExecutorConfigBuilderBase<Nested<P>> {

        /**
         * Builds the {@link MqttClientExecutorConfig} and applies it to the parent.
         *
         * @return the result when the built {@link MqttClientExecutorConfig} is applied to the parent.
         */
        @NotNull P applyExecutorConfig();
    }
}

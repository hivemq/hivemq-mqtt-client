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

package com.hivemq.client.mqtt.lifecycle;

import com.hivemq.client.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Builder base for a {@link MqttAutoReconnect}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.NonExtendable
public interface MqttAutoReconnectBuilderBase<B extends MqttAutoReconnectBuilderBase<B>> {

    /**
     * Sets the initial delay the client will wait before it tries to reconnect.
     * <p>
     * It must be positive.
     *
     * @param initialDelay the initial delay.
     * @param timeUnit     the time unit of the given initial delay.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B initialDelay(final long initialDelay, @NotNull TimeUnit timeUnit);

    /**
     * Sets the maximum delay the client will wait before it tries to reconnect.
     * <p>
     * It must be positive or zero.
     *
     * @param maxDelay the maximum delay.
     * @param timeUnit the time unit of the given maximum delay.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B maxDelay(final long maxDelay, @NotNull TimeUnit timeUnit);
}

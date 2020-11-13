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

import com.hivemq.client.internal.mqtt.lifecycle.MqttAutoReconnectImplBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Automatic reconnect strategy using an exponential backoff with configurable initial and maximum delays.
 * <p>
 * The initial delay will be doubled for every unsuccessful connect attempt. The actual delay will be capped at the
 * maximum delay. Additionally a random delay of +-25% will be added.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@ApiStatus.NonExtendable
public interface MqttAutoReconnect extends MqttDisconnectedListener {

    /**
     * The default initial delay in seconds the client will wait before it tries to reconnect.
     */
    long DEFAULT_START_DELAY_S = 1;

    /**
     * The default maximum delay in seconds the client will wait before it tries to reconnect.
     */
    long DEFAULT_MAX_DELAY_S = 120;

    /**
     * Creates a builder for an automatic reconnect strategy.
     *
     * @return the created builder for an automatic reconnect strategy.
     */
    static @NotNull MqttAutoReconnectBuilder builder() {
        return new MqttAutoReconnectImplBuilder.Default();
    }

    /**
     * Returns the initial delay the client will wait before it tries to reconnect.
     * <p>
     * This delay will be doubled for every unsuccessful connect attempt.
     *
     * @param timeUnit the time unit of the returned initial delay.
     * @return the start delay in the given time unit.
     */
    long getInitialDelay(@NotNull TimeUnit timeUnit);

    /**
     * Returns the maximum delay the client will wait before it tries to reconnect.
     *
     * @param timeUnit the time unit of the returned maximum delay.
     * @return the maximum delay in the given time unit.
     */
    long getMaxDelay(@NotNull TimeUnit timeUnit);

    /**
     * Creates a builder for extending this automatic reconnect strategy.
     *
     * @return the created builder.
     */
    @NotNull MqttAutoReconnectBuilder extend();
}

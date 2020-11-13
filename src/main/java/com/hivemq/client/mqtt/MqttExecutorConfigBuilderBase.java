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
import io.reactivex.Scheduler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

/**
 * Builder base for a {@link MqttExecutorConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttExecutorConfigBuilderBase<B extends MqttExecutorConfigBuilderBase<B>> {

    /**
     * Sets the optional user defined {@link MqttExecutorConfig#getNettyExecutor() executor for Netty} (network
     * communication framework).
     *
     * @param nettyExecutor the user defined executor for Netty or <code>null</code> to use the default executor.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B nettyExecutor(@Nullable Executor nettyExecutor);

    /**
     * Sets the optional user defined {@link MqttExecutorConfig#getNettyThreads() amount of threads Netty} (network
     * communication framework).
     *
     * @param nettyThreads the user defined amount of threads Netty.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B nettyThreads(int nettyThreads);

    /**
     * Sets the {@link MqttExecutorConfig#getApplicationScheduler() scheduler used for executing application specific
     * code}.
     *
     * @param applicationScheduler the scheduler used for executing application specific code.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B applicationScheduler(@NotNull Scheduler applicationScheduler);
}

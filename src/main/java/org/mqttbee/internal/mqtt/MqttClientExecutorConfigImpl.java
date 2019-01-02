/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.internal.mqtt;

import io.reactivex.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttClientExecutorConfigImpl implements MqttClientExecutorConfig {

    public static final int DEFAULT_NETTY_THREADS = 0;
    public static final @NotNull MqttClientExecutorConfigImpl DEFAULT =
            new MqttClientExecutorConfigImpl(null, DEFAULT_NETTY_THREADS, DEFAULT_APPLICATION_SCHEDULER);

    private final @Nullable Executor nettyExecutor;
    private final int nettyThreads;
    private final @NotNull Scheduler applicationScheduler;

    MqttClientExecutorConfigImpl(
            final @Nullable Executor nettyExecutor, final int nettyThreads,
            final @NotNull Scheduler applicationScheduler) {

        this.nettyExecutor = nettyExecutor;
        this.nettyThreads = nettyThreads;
        this.applicationScheduler = applicationScheduler;
    }

    @Override
    public @NotNull Optional<Executor> getNettyExecutor() {
        return Optional.ofNullable(nettyExecutor);
    }

    public @Nullable Executor getRawNettyExecutor() {
        return nettyExecutor;
    }

    @Override
    public @NotNull OptionalInt getNettyThreads() {
        return (nettyThreads == DEFAULT_NETTY_THREADS) ? OptionalInt.empty() : OptionalInt.of(nettyThreads);
    }

    public int getRawNettyThreads() {
        return nettyThreads;
    }

    public @NotNull Scheduler getApplicationScheduler() {
        return applicationScheduler;
    }
}

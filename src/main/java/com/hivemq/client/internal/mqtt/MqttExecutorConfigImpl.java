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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.mqtt.MqttExecutorConfig;
import io.reactivex.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttExecutorConfigImpl implements MqttExecutorConfig {

    public static final int DEFAULT_NETTY_THREADS = 0;
    public static final @NotNull MqttExecutorConfigImpl DEFAULT =
            new MqttExecutorConfigImpl(null, DEFAULT_NETTY_THREADS, DEFAULT_APPLICATION_SCHEDULER);

    private final @Nullable Executor nettyExecutor;
    private final int nettyThreads;
    private final @NotNull Scheduler applicationScheduler;

    MqttExecutorConfigImpl(
            final @Nullable Executor nettyExecutor,
            final int nettyThreads,
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

    @Override
    public @NotNull Scheduler getApplicationScheduler() {
        return applicationScheduler;
    }

    @Override
    public MqttExecutorConfigImplBuilder.@NotNull Default extend() {
        return new MqttExecutorConfigImplBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttExecutorConfigImpl)) {
            return false;
        }
        final MqttExecutorConfigImpl that = (MqttExecutorConfigImpl) o;

        return Objects.equals(nettyExecutor, that.nettyExecutor) && (nettyThreads == that.nettyThreads) &&
                applicationScheduler.equals(that.applicationScheduler);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(nettyExecutor);
        result = 31 * result + nettyThreads;
        result = 31 * result + applicationScheduler.hashCode();
        return result;
    }
}

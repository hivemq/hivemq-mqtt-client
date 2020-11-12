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

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttExecutorConfigBuilder;
import io.reactivex.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttExecutorConfigImplBuilder<B extends MqttExecutorConfigImplBuilder<B>> {

    private @Nullable Executor nettyExecutor;
    private int nettyThreads = MqttExecutorConfigImpl.DEFAULT_NETTY_THREADS;
    private @NotNull Scheduler applicationScheduler = MqttExecutorConfigImpl.DEFAULT_APPLICATION_SCHEDULER;

    MqttExecutorConfigImplBuilder() {}

    MqttExecutorConfigImplBuilder(final @NotNull MqttExecutorConfigImpl executorConfig) {
        nettyExecutor = executorConfig.getRawNettyExecutor();
        nettyThreads = executorConfig.getRawNettyThreads();
        applicationScheduler = executorConfig.getApplicationScheduler();
    }

    abstract @NotNull B self();

    public @NotNull B nettyExecutor(final @Nullable Executor nettyExecutor) {
        this.nettyExecutor = nettyExecutor;
        return self();
    }

    public @NotNull B nettyThreads(final int nettyThreads) {
        if (nettyThreads <= 0) {
            throw new IllegalArgumentException(
                    "Number of Netty threads must be greater than 0. Found: " + nettyThreads);
        }
        this.nettyThreads = nettyThreads;
        return self();
    }

    public @NotNull B applicationScheduler(final @Nullable Scheduler applicationScheduler) {
        this.applicationScheduler = Checks.notNull(applicationScheduler, "Application scheduler");
        return self();
    }

    public @NotNull MqttExecutorConfigImpl build() {
        return new MqttExecutorConfigImpl(nettyExecutor, nettyThreads, applicationScheduler);
    }

    public static class Default extends MqttExecutorConfigImplBuilder<Default> implements MqttExecutorConfigBuilder {

        public Default() {}

        Default(final @NotNull MqttExecutorConfigImpl executorConfig) {
            super(executorConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttExecutorConfigImplBuilder<Nested<P>>
            implements MqttExecutorConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttExecutorConfigImpl, P> parentConsumer;

        Nested(
                final @NotNull MqttExecutorConfigImpl executorConfig,
                final @NotNull Function<? super MqttExecutorConfigImpl, P> parentConsumer) {

            super(executorConfig);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyExecutorConfig() {
            return parentConsumer.apply(build());
        }
    }
}

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

package org.mqttbee.mqtt;

import io.netty.channel.MultithreadEventLoopGroup;
import io.reactivex.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientExecutorConfigBuilder;
import org.mqttbee.mqtt.ioc.MqttBeeComponent;
import org.mqttbee.util.Checks;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientExecutorConfigImplBuilder<B extends MqttClientExecutorConfigImplBuilder<B>> {

    private @Nullable Executor nettyExecutor;
    private int nettyThreads = MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS;
    private @NotNull Scheduler applicationScheduler = MqttClientExecutorConfigImpl.DEFAULT_APPLICATION_SCHEDULER;

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

    public @NotNull MqttClientExecutorConfigImpl build() {

        final MultithreadEventLoopGroup eventLoopGroup =
                MqttBeeComponent.INSTANCE.nettyEventLoopProvider().getEventLoopGroup(nettyExecutor, nettyThreads);
        return new MqttClientExecutorConfigImpl(eventLoopGroup, applicationScheduler);
    }

    public static class Default extends MqttClientExecutorConfigImplBuilder<Default>
            implements MqttClientExecutorConfigBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientExecutorConfigImplBuilder<Nested<P>>
            implements MqttClientExecutorConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientExecutorConfigImpl, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttClientExecutorConfigImpl, P> parentConsumer) {
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

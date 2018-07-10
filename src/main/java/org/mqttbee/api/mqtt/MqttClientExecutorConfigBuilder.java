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

package org.mqttbee.api.mqtt;

import com.google.common.base.Preconditions;
import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.util.FluentBuilder;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttClientExecutorConfigBuilder<P> extends FluentBuilder<MqttClientExecutorConfig, P> {

    private Executor nettyExecutor;
    private int nettyThreads = MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS;
    private Scheduler applicationScheduler = MqttClientExecutorConfigImpl.DEFAULT_RX_JAVA_SCHEDULER;

    public MqttClientExecutorConfigBuilder(
            @Nullable final Function<? super MqttClientExecutorConfig, P> parentConsumer) {

        super(parentConsumer);
    }

    @NotNull
    public MqttClientExecutorConfigBuilder<P> nettyExecutor(@NotNull final Executor nettyExecutor) {
        Preconditions.checkNotNull(nettyExecutor, "Netty executor must not be null.");
        this.nettyExecutor = nettyExecutor;
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder<P> nettyThreads(final int nettyThreads) {
        Preconditions.checkArgument(nettyThreads > 0, "Number of Netty threads must be bigger than 0. Found: %s.",
                nettyThreads);
        this.nettyThreads = nettyThreads;
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder<P> applicationScheduler(@NotNull final Scheduler applicationScheduler) {
        Preconditions.checkNotNull(applicationScheduler, "Application scheduler must not be null.");
        this.applicationScheduler = applicationScheduler;
        return this;
    }

    @NotNull
    @Override
    public MqttClientExecutorConfig build() {
        return new MqttClientExecutorConfigImpl(nettyExecutor, nettyThreads, applicationScheduler);
    }

}

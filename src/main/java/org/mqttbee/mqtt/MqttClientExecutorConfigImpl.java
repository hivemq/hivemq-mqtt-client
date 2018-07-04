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

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttClientExecutorConfigImpl implements MqttClientExecutorConfig {

    public static final int DEFAULT_NETTY_THREADS = 0;
    public static final Scheduler DEFAULT_RX_JAVA_SCHEDULER = Schedulers.computation();
    public static final MqttClientExecutorConfigImpl DEFAULT =
            new MqttClientExecutorConfigImpl(null, DEFAULT_NETTY_THREADS, DEFAULT_RX_JAVA_SCHEDULER);

    private final Executor nettyExecutor;
    private final int nettyThreads;
    private final Scheduler applicationScheduler;

    public MqttClientExecutorConfigImpl(
            @Nullable final Executor nettyExecutor, final int nettyThreads,
            @NotNull final Scheduler applicationScheduler) {

        this.nettyExecutor = nettyExecutor;
        this.nettyThreads = nettyThreads;
        this.applicationScheduler = applicationScheduler;
    }

    @NotNull
    @Override
    public Optional<Executor> getUserDefinedNettyExecutor() {
        return Optional.ofNullable(nettyExecutor);
    }

    @Nullable
    public Executor getRawNettyExecutor() {
        return nettyExecutor;
    }

    @NotNull
    @Override
    public Optional<Integer> getUserDefinedNettyThreads() {
        return (nettyThreads == DEFAULT_NETTY_THREADS) ? Optional.empty() : Optional.of(nettyThreads);
    }

    public int getRawNettyThreads() {
        return nettyThreads;
    }

    @NotNull
    public Scheduler getApplicationScheduler() {
        return applicationScheduler;
    }

}

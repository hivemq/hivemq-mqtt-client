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
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;

import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
public class MqttClientExecutorConfigBuilder {

    private Executor nettyExecutor;
    private int nettyThreads = MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS;
    private Scheduler rxJavaScheduler = MqttClientExecutorConfigImpl.DEFAULT_RX_JAVA_SCHEDULER;

    @NotNull
    public MqttClientExecutorConfigBuilder nettyExecutor(@NotNull final Executor nettyExecutor) {
        Preconditions.checkNotNull(nettyExecutor);
        this.nettyExecutor = nettyExecutor;
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder nettyThreads(final int nettyThreads) {
        Preconditions.checkArgument(nettyThreads > 0);
        this.nettyThreads = nettyThreads;
        return this;
    }

    @NotNull
    public MqttClientExecutorConfigBuilder rxJavaScheduler(@NotNull final Scheduler rxJavaScheduler) {
        Preconditions.checkNotNull(rxJavaScheduler);
        this.rxJavaScheduler = rxJavaScheduler;
        return this;
    }

    public MqttClientExecutorConfig build() {
        return new MqttClientExecutorConfigImpl(nettyExecutor, nettyThreads, rxJavaScheduler);
    }

}

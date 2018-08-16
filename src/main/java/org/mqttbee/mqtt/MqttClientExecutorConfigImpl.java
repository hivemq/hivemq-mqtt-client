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
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;
import org.mqttbee.mqtt.ioc.MqttBeeComponent;

/**
 * @author Silvio Giebl
 */
public class MqttClientExecutorConfigImpl implements MqttClientExecutorConfig {

    public static final int DEFAULT_NETTY_THREADS = 0;

    @NotNull
    public static MqttClientExecutorConfigImpl orDefault(@Nullable final MqttClientExecutorConfigImpl executorConfig) {
        return (executorConfig == null) ? MqttClientExecutorConfigImpl.getDefault() : executorConfig;
    }

    @NotNull
    public static MqttClientExecutorConfigImpl getDefault() {
        return new MqttClientExecutorConfigImpl(
                MqttBeeComponent.INSTANCE.nettyEventLoopProvider().getEventLoopGroup(null, DEFAULT_NETTY_THREADS),
                DEFAULT_APPLICATION_SCHEDULER);
    }

    private final MultithreadEventLoopGroup nettyEventLoopGroup;
    private final Scheduler applicationScheduler;

    public MqttClientExecutorConfigImpl(
            @NotNull final MultithreadEventLoopGroup nettyEventLoopGroup,
            @NotNull final Scheduler applicationScheduler) {

        this.nettyEventLoopGroup = nettyEventLoopGroup;
        this.applicationScheduler = applicationScheduler;
    }

    @NotNull
    @Override
    public MultithreadEventLoopGroup getNettyEventLoopGroup() {
        return nettyEventLoopGroup;
    }

    @NotNull
    public Scheduler getApplicationScheduler() {
        return applicationScheduler;
    }

}

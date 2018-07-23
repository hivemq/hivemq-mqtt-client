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

package org.mqttbee.mqtt.netty;

import io.netty.channel.Channel;
import io.netty.channel.MultithreadEventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
@Singleton
@ThreadSafe
public abstract class NettyBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyBootstrap.class);

    private MultithreadEventLoopGroup defaultEventLoopGroup;
    private AtomicInteger defaultEventLoopGroupReferenceCount;
    private Map<Executor, MultithreadEventLoopGroup> eventLoopGroups;
    private Map<Executor, AtomicInteger> eventLoopGroupReferenceCounts;

    NettyBootstrap() {
    }

    @NotNull
    public synchronized MultithreadEventLoopGroup getEventLoopGroup(
            @NotNull final MqttClientExecutorConfigImpl executorConfig) {

        if (executorConfig.getRawNettyExecutor() == null) {
            return getDefaultEventLoopGroup(executorConfig.getRawNettyThreads());
        }
        return getExecutorEventLoopGroup(executorConfig.getRawNettyExecutor(), executorConfig.getRawNettyThreads());
    }

    @NotNull
    private MultithreadEventLoopGroup getDefaultEventLoopGroup(final int numberOfNettyThreads) {
        if (defaultEventLoopGroup == null) {
            defaultEventLoopGroup = newDefaultEventLoopGroup(numberOfNettyThreads);
            defaultEventLoopGroupReferenceCount = new AtomicInteger(1);
        } else {
            final int defaultThreadCount = defaultEventLoopGroup.executorCount();
            if ((numberOfNettyThreads != MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS) &&
                    (defaultThreadCount != numberOfNettyThreads)) {
                LOGGER.warn(
                        "Tried to use the default executor with a different amount of Netty threads. Using {} threads instead of {}",
                        defaultThreadCount, numberOfNettyThreads);
            }
            defaultEventLoopGroupReferenceCount.incrementAndGet();
        }
        return defaultEventLoopGroup;
    }

    @NotNull
    private MultithreadEventLoopGroup getExecutorEventLoopGroup(
            @Nullable final Executor executor, final int numberOfNettyThreads) {

        if (eventLoopGroups == null) {
            eventLoopGroups = new HashMap<>(4);
            eventLoopGroupReferenceCounts = new HashMap<>(4);
        }
        MultithreadEventLoopGroup eventLoopGroup = eventLoopGroups.get(executor);
        if (eventLoopGroup == null) {
            eventLoopGroup = newExecutorEventLoopGroup(executor, numberOfNettyThreads);
            eventLoopGroups.put(executor, eventLoopGroup);
            eventLoopGroupReferenceCounts.put(executor, new AtomicInteger(1));
        } else {
            final int threadCount = eventLoopGroup.executorCount();
            if ((numberOfNettyThreads != MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS) &&
                    (threadCount != numberOfNettyThreads)) {
                LOGGER.warn(
                        "Tried to use a different amount of Netty threads for the same executor. Using {} threads instead of {}",
                        threadCount, numberOfNettyThreads);
            }
            eventLoopGroupReferenceCounts.get(executor).incrementAndGet();
        }
        return eventLoopGroup;
    }

    @NotNull
    abstract MultithreadEventLoopGroup newDefaultEventLoopGroup(final int numberOfNettyThreads);

    @NotNull
    abstract MultithreadEventLoopGroup newExecutorEventLoopGroup(
            @Nullable final Executor executor, final int numberOfNettyThreads);

    @NotNull
    public abstract Class<? extends Channel> getChannelClass();

    public synchronized void free(@NotNull final MqttClientExecutorConfigImpl executorConfig) {
        if (executorConfig.getRawNettyExecutor() == null) {
            freeDefaultEventLoopGroup();
        } else {
            freeExecutorEventLoopGroup(executorConfig.getRawNettyExecutor());
        }
    }

    private void freeDefaultEventLoopGroup() {
        if (defaultEventLoopGroupReferenceCount.decrementAndGet() == 0) {
            defaultEventLoopGroup.shutdownGracefully();
            defaultEventLoopGroup = null;
            defaultEventLoopGroupReferenceCount = null;
        }
    }

    private void freeExecutorEventLoopGroup(@Nullable final Executor executor) {
        final MultithreadEventLoopGroup eventLoopGroup = eventLoopGroups.get(executor);
        if (eventLoopGroupReferenceCounts.get(executor).decrementAndGet() == 0) {
            eventLoopGroup.shutdownGracefully();
            eventLoopGroups.remove(executor);
            eventLoopGroupReferenceCounts.remove(executor);
            if (eventLoopGroups.size() == 0) {
                eventLoopGroups = null;
                eventLoopGroupReferenceCounts = null;
            }
        }
    }

}

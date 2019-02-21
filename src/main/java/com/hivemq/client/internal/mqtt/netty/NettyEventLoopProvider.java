/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.netty;

import com.hivemq.client.internal.annotations.ThreadSafe;
import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientExecutorConfigImpl;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.MultithreadEventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * @author Silvio Giebl
 */
@ThreadSafe
public class NettyEventLoopProvider {

    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(NettyEventLoopProvider.class);

    private final @NotNull Map<@Nullable Executor, @NotNull Entry> entries = new HashMap<>();
    private final @NotNull BiFunction<Integer, Executor, MultithreadEventLoopGroup> eventLoopGroupFactory;
    private final @NotNull ChannelFactory<?> channelFactory;

    NettyEventLoopProvider(
            final @NotNull BiFunction<Integer, Executor, MultithreadEventLoopGroup> eventLoopGroupFactory,
            final @NotNull ChannelFactory<?> channelFactory) {

        this.eventLoopGroupFactory = eventLoopGroupFactory;
        this.channelFactory = channelFactory;
    }

    public synchronized @NotNull EventLoop acquireEventLoop(final @Nullable Executor executor, final int threadCount) {
        Entry entry = entries.get(executor);
        if (entry == null) {
            entry = new Entry(eventLoopGroupFactory.apply(threadCount, executor));
            entries.put(executor, entry);
        } else {
            final int previousThreadCount = entry.eventLoopGroup.executorCount();
            if ((threadCount != MqttClientExecutorConfigImpl.DEFAULT_NETTY_THREADS) &&
                    (previousThreadCount != threadCount)) {
                LOGGER.warn(
                        "Tried to use a different amount of Netty threads for the same executor. Using {} threads instead of {}",
                        previousThreadCount, threadCount);
            }
            entry.referenceCount++;
        }
        return entry.eventLoopGroup.next();
    }

    public synchronized void releaseEventLoop(final @Nullable Executor executor) {
        final Entry entry = entries.get(executor);
        if (--entry.referenceCount == 0) {
            entry.eventLoopGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
            entries.remove(executor);
        }
    }

    public @NotNull ChannelFactory<?> getChannelFactory() {
        return channelFactory;
    }

    private static class Entry {

        final @NotNull MultithreadEventLoopGroup eventLoopGroup;
        int referenceCount = 1;

        private Entry(final @NotNull MultithreadEventLoopGroup eventLoopGroup) {
            this.eventLoopGroup = eventLoopGroup;
        }
    }
}

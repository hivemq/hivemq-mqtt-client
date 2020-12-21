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

package com.hivemq.client2.internal.netty;

import com.hivemq.client2.internal.annotations.ThreadSafe;
import com.hivemq.client2.internal.logging.InternalLogger;
import com.hivemq.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.client2.internal.mqtt.MqttExecutorConfigImpl;
import com.hivemq.client2.internal.util.ClassUtil;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
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

    public static final @NotNull NettyEventLoopProvider INSTANCE;

    static {
        if (ClassUtil.isAvailable("io.netty.channel.epoll.Epoll")) {
            INSTANCE = EpollHolder.eventLoopProvider();
        } else {
            INSTANCE = nioEventLoopProvider();
        }
    }

    private static NettyEventLoopProvider nioEventLoopProvider() {
        return new NettyEventLoopProvider(NioEventLoopGroup::new, NioSocketChannel::new);
    }

    private static class EpollHolder {

        private static NettyEventLoopProvider eventLoopProvider() {
            if (Epoll.isAvailable()) {
                return new NettyEventLoopProvider(EpollEventLoopGroup::new, EpollSocketChannel::new);
            } else {
                return nioEventLoopProvider();
            }
        }
    }

    private final @NotNull Map<@Nullable Executor, @NotNull Entry> entries = new HashMap<>();
    private final @NotNull BiFunction<Integer, Executor, MultithreadEventLoopGroup> eventLoopGroupFactory;
    private final @NotNull ChannelFactory<?> channelFactory;

    private NettyEventLoopProvider(
            final @NotNull BiFunction<Integer, Executor, MultithreadEventLoopGroup> eventLoopGroupFactory,
            final @NotNull ChannelFactory<?> channelFactory) {

        this.eventLoopGroupFactory = eventLoopGroupFactory;
        this.channelFactory = channelFactory;
    }

    public synchronized @NotNull EventLoop acquireEventLoop(final @Nullable Executor executor, final int threadCount) {
        Entry entry = entries.get(executor);
        if (entry == null) {
            final MultithreadEventLoopGroup eventLoopGroup;
            if (executor == null) {
                eventLoopGroup = eventLoopGroupFactory.apply(
                        threadCount, new ThreadPerTaskExecutor(
                                new DefaultThreadFactory("com.hivemq.client2.mqtt", Thread.MAX_PRIORITY)));

            } else if (executor instanceof MultithreadEventLoopGroup) {
                eventLoopGroup = (MultithreadEventLoopGroup) executor;
                if ((threadCount != MqttExecutorConfigImpl.DEFAULT_NETTY_THREADS) &&
                        (eventLoopGroup.executorCount() != threadCount)) {
                    LOGGER.warn("Tried to use a different amount of Netty threads for the provided event loop. " +
                            "Using {} threads instead of {}", eventLoopGroup.executorCount(), threadCount);
                }
            } else {
                eventLoopGroup = eventLoopGroupFactory.apply(threadCount, executor);
            }
            entry = new Entry(eventLoopGroup);
            entries.put(executor, entry);
        } else {
            if ((threadCount != MqttExecutorConfigImpl.DEFAULT_NETTY_THREADS) &&
                    (entry.eventLoopGroup.executorCount() != threadCount)) {
                LOGGER.warn("Tried to use a different amount of Netty threads for the same executor. " +
                        "Using {} threads instead of {}", entry.eventLoopGroup.executorCount(), threadCount);
            }
            entry.referenceCount++;
        }
        return entry.eventLoopGroup.next();
    }

    public synchronized void releaseEventLoop(final @Nullable Executor executor) {
        final Entry entry = entries.get(executor);
        if (--entry.referenceCount == 0) {
            entries.remove(executor);
            if (!(executor instanceof MultithreadEventLoopGroup)) {
                // shutdownGracefully must be the last statement so everything is cleaned up even if it throws
                entry.eventLoopGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
            }
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

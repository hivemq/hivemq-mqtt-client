package org.mqttbee.mqtt5.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
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
    public Bootstrap bootstrap(@Nullable final Executor executor, final int numberOfNettyThreads) {
        return new Bootstrap().group(getEventLoopGroup(executor, numberOfNettyThreads))
                .channel(getChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    @NotNull
    private synchronized MultithreadEventLoopGroup getEventLoopGroup(
            @Nullable final Executor executor, final int numberOfNettyThreads) {

        if (executor == null) {
            return getDefaultEventLoopGroup(numberOfNettyThreads);
        }
        return getExecutorEventLoopGroup(executor, numberOfNettyThreads);
    }

    @NotNull
    private MultithreadEventLoopGroup getDefaultEventLoopGroup(final int numberOfNettyThreads) {
        if (defaultEventLoopGroup == null) {
            defaultEventLoopGroup = newDefaultEventLoopGroup(numberOfNettyThreads);
        } else {
            final int defaultThreadCount = defaultEventLoopGroup.executorCount();
            if (defaultThreadCount != numberOfNettyThreads) {
                LOGGER.warn(
                        "Tried to use the default executor with a different amount of Netty threads. Using {} threads instead of {}",
                        defaultThreadCount, numberOfNettyThreads);
            }
        }
        defaultEventLoopGroupReferenceCount.incrementAndGet();
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
            if (threadCount != numberOfNettyThreads) {
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
    abstract Class<? extends Channel> getChannelClass();

    public synchronized void free(@Nullable final Executor executor) {
        if (executor == null) {
            freeDefaultEventLoopGroup();
        } else {
            freeExecutorEventLoopGroup(executor);
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

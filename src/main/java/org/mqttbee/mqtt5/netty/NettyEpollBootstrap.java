package org.mqttbee.mqtt5.netty;

import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
@Singleton
class NettyEpollBootstrap extends NettyBootstrap {

    @Inject
    NettyEpollBootstrap() {
    }

    @NotNull
    @Override
    MultithreadEventLoopGroup newDefaultEventLoopGroup(final int numberOfNettyThreads) {
        return new EpollEventLoopGroup(numberOfNettyThreads);
    }

    @NotNull
    @Override
    MultithreadEventLoopGroup newExecutorEventLoopGroup(
            @Nullable final Executor executor, final int numberOfNettyThreads) {
        return new EpollEventLoopGroup(numberOfNettyThreads, executor);
    }

    @NotNull
    @Override
    Class<EpollSocketChannel> getChannelClass() {
        return EpollSocketChannel.class;
    }

}

package org.mqttbee.mqtt5.netty;

import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

/**
 * @author Silvio Giebl
 */
@Singleton
class NettyNioBootstrap extends NettyBootstrap {

    @Inject
    NettyNioBootstrap() {
    }

    @NotNull
    @Override
    MultithreadEventLoopGroup newDefaultEventLoopGroup(final int numberOfNettyThreads) {
        return new NioEventLoopGroup(numberOfNettyThreads);
    }

    @NotNull
    @Override
    MultithreadEventLoopGroup newExecutorEventLoopGroup(
            @Nullable final Executor executor, final int numberOfNettyThreads) {
        return new NioEventLoopGroup(numberOfNettyThreads, executor);
    }

    @NotNull
    @Override
    Class<NioSocketChannel> getChannelClass() {
        return NioSocketChannel.class;
    }

}

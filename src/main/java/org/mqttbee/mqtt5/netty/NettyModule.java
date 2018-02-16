package org.mqttbee.mqtt5.netty;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import io.netty.channel.epoll.Epoll;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Module
public class NettyModule {

    @Provides
    @Singleton
    static NettyBootstrap provideNettyBootstrap(
            final Lazy<NettyNioBootstrap> nioBootstrapLazy, final Lazy<NettyEpollBootstrap> epollBootstrapLazy) {

        if (Epoll.isAvailable()) {
            return epollBootstrapLazy.get();
        } else {
            return nioBootstrapLazy.get();
        }
    }

}

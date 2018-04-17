/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

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

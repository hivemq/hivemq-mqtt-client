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

package com.hivemq.client.internal.mqtt.ioc;

import com.hivemq.client.internal.mqtt.handler.MqttChannelInitializer;
import com.hivemq.client.internal.mqtt.handler.auth.MqttAuthHandler;
import com.hivemq.client.internal.mqtt.handler.auth.MqttConnectAuthHandler;
import com.hivemq.client.internal.mqtt.handler.auth.MqttDisconnectOnAuthHandler;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.netty.NettyEventLoopProvider;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
@Module
abstract class ConnectionModule {

    @Provides
    static @NotNull Bootstrap provideBootstrap(
            final @NotNull NettyEventLoopProvider nettyEventLoopProvider,
            final @NotNull MqttChannelInitializer channelInitializer) {

        return new Bootstrap().channelFactory(nettyEventLoopProvider.getChannelFactory())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                .handler(channelInitializer);
    }

    @Provides
    @ConnectionScope
    static @NotNull MqttAuthHandler provideAuthHandler(
            final @NotNull MqttConnect connect, final @NotNull Lazy<MqttConnectAuthHandler> connectAuthHandlerLazy,
            final @NotNull Lazy<MqttDisconnectOnAuthHandler> disconnectOnAuthHandlerLazy) {

        return (connect.getRawEnhancedAuthMechanism() == null) ? disconnectOnAuthHandlerLazy.get() :
                connectAuthHandlerLazy.get();
    }
}

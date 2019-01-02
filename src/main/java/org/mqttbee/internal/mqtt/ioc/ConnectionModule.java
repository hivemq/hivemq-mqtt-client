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

package org.mqttbee.internal.mqtt.ioc;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.handler.MqttChannelInitializer;
import org.mqttbee.internal.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.internal.mqtt.handler.auth.MqttConnectAuthHandler;
import org.mqttbee.internal.mqtt.handler.auth.MqttDisconnectOnAuthHandler;
import org.mqttbee.internal.mqtt.message.connect.MqttConnect;
import org.mqttbee.internal.mqtt.netty.NettyEventLoopProvider;

/**
 * @author Silvio Giebl
 */
@Module
abstract class ConnectionModule {

    @Provides
    static @NotNull Bootstrap provideBootstrap(
            final @NotNull MqttClientConfig clientConfig, final @NotNull NettyEventLoopProvider nettyEventLoopProvider,
            final @NotNull MqttChannelInitializer channelInitializer) {

        return new Bootstrap().group(clientConfig.acquireEventLoop())
                .channelFactory(nettyEventLoopProvider.getChannelFactory())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(channelInitializer);
    }

    @Provides
    @ConnectionScope
    static @NotNull MqttAuthHandler provideAuthHandler(
            final @NotNull MqttConnect connect, final @NotNull Lazy<MqttConnectAuthHandler> connectAuthHandlerLazy,
            final @NotNull Lazy<MqttDisconnectOnAuthHandler> disconnectOnAuthHandlerLazy) {

        return (connect.getRawEnhancedAuthProvider() == null) ? disconnectOnAuthHandlerLazy.get() :
                connectAuthHandlerLazy.get();
    }
}

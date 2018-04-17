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

package org.mqttbee.mqtt5.handler.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * Default interface to make it easier to extend a subclass of {@link io.netty.channel.ChannelInboundHandlerAdapter}
 * and also implement {@link ChannelOutboundHandler}.
 *
 * @author Silvio Giebl
 */
public interface DefaultChannelOutboundHandler extends ChannelOutboundHandler {

    @Override
    default void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) {
        ctx.bind(localAddress, promise);
    }

    @Override
    default void connect(
            final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress,
            final ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    default void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ctx.disconnect(promise);
    }

    @Override
    default void close(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ctx.close(promise);
    }

    @Override
    default void deregister(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ctx.deregister(promise);
    }

    @Override
    default void read(final ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    default void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        ctx.write(msg, promise);
    }

    @Override
    default void flush(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

}

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

package com.hivemq.client.internal.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;

/**
 * Default interface to make it easier to extend a subclass of {@link io.netty.channel.ChannelInboundHandlerAdapter} and
 * also implement {@link ChannelOutboundHandler}.
 *
 * @author Silvio Giebl
 */
public interface DefaultChannelOutboundHandler extends ChannelOutboundHandler {

    @Override
    default void bind(
            final @NotNull ChannelHandlerContext ctx, final @NotNull SocketAddress localAddress,
            final @NotNull ChannelPromise promise) {

        ctx.bind(localAddress, promise);
    }

    @Override
    default void connect(
            final @NotNull ChannelHandlerContext ctx, final @NotNull SocketAddress remoteAddress,
            final @Nullable SocketAddress localAddress, final @NotNull ChannelPromise promise) {

        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    default void disconnect(final @NotNull ChannelHandlerContext ctx, final @NotNull ChannelPromise promise) {
        ctx.disconnect(promise);
    }

    @Override
    default void close(final @NotNull ChannelHandlerContext ctx, final @NotNull ChannelPromise promise) {
        ctx.close(promise);
    }

    @Override
    default void deregister(final @NotNull ChannelHandlerContext ctx, final @NotNull ChannelPromise promise) {
        ctx.deregister(promise);
    }

    @Override
    default void read(final @NotNull ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    default void write(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg,
            final @NotNull ChannelPromise promise) {

        ctx.write(msg, promise);
    }

    @Override
    default void flush(final @NotNull ChannelHandlerContext ctx) {
        ctx.flush();
    }
}

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

package com.hivemq.client2.internal.mqtt.handler.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
class MqttWebsocketHandshakeHandler extends ChannelInboundHandlerAdapter {

    public static final @NotNull String NAME = "ws.handshake";

    private final @NotNull WebSocketClientHandshaker handshaker;
    private final int handshakeTimeoutMs;
    private final @NotNull Consumer<Channel> onSuccess;
    private final @NotNull BiConsumer<Channel, Throwable> onError;

    private boolean handshakeStarted = false;
    private boolean handshakeDone = false;
    private @Nullable ScheduledFuture<?> timeoutFuture;

    MqttWebsocketHandshakeHandler(
            final @NotNull WebSocketClientHandshaker handshaker,
            final int handshakeTimeoutMs,
            final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        this.handshaker = handshaker;
        this.handshakeTimeoutMs = handshakeTimeoutMs;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            startHandshake(ctx);
        }
    }

    @Override
    public void channelActive(final @NotNull ChannelHandlerContext ctx) {
        startHandshake(ctx);
        ctx.fireChannelActive();
    }

    private void startHandshake(final @NotNull ChannelHandlerContext ctx) {
        if (!handshakeStarted) {
            handshakeStarted = true;

            if (handshakeTimeoutMs > 0) {
                timeoutFuture = ctx.channel().eventLoop().schedule(() -> {
                    if (setHandshakeDone(ctx)) {
                        onError.accept(ctx.channel(), new WebSocketHandshakeException(
                                "handshake timed out after " + handshakeTimeoutMs + "ms"));
                    }
                }, handshakeTimeoutMs, TimeUnit.MILLISECONDS);
            }

            handshaker.handshake(ctx.channel(), ctx.voidPromise());
        }
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof FullHttpResponse) {
            finishHandshake(ctx, (FullHttpResponse) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void finishHandshake(final @NotNull ChannelHandlerContext ctx, final @NotNull FullHttpResponse response) {
        if (setHandshakeDone(ctx)) {
            try {
                handshaker.finishHandshake(ctx.channel(), response);
                onSuccess.accept(ctx.channel());
            } catch (final Throwable t) {
                onError.accept(ctx.channel(), t);
            }
        }
        response.release();
    }

    @Override
    public void channelInactive(final @NotNull ChannelHandlerContext ctx) {
        if (setHandshakeDone(ctx)) {
            onError.accept(ctx.channel(), new WebSocketHandshakeException("connection was closed during handshake"));
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (setHandshakeDone(ctx)) {
            onError.accept(ctx.channel(), cause);
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    private boolean setHandshakeDone(final @NotNull ChannelHandlerContext ctx) {
        if (!handshakeDone) {
            handshakeDone = true;
            ctx.pipeline().remove(this);
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
                timeoutFuture = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}

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

package com.hivemq.client.internal.mqtt.handler.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
class MqttWebsocketHandshakeHandler extends ChannelInboundHandlerAdapter {

    public static final @NotNull String NAME = "ws.init";

    private final @NotNull WebSocketClientHandshaker handshaker;
    private final @NotNull Consumer<Channel> onSuccess;
    private final @NotNull BiConsumer<Channel, Throwable> onError;
    private boolean handshakeStarted = false;

    MqttWebsocketHandshakeHandler(
            final @NotNull WebSocketClientHandshaker handshaker, final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        this.handshaker = handshaker;
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
            handshaker.handshake(ctx.channel(), ctx.voidPromise());
            // TODO timeout
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
        try {
            if (handshaker.isHandshakeComplete()) {
                throw new IllegalStateException(
                        "Must not receive http response if websocket handshake is already finished.");
            }
            handshaker.finishHandshake(ctx.channel(), response);
            onSuccess.accept(ctx.channel());
            ctx.pipeline().remove(this);
        } finally {
            response.release();
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        onError.accept(ctx.channel(), cause);
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}

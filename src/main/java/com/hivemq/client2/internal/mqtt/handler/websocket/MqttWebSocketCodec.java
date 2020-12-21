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

import com.hivemq.client2.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.*;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class MqttWebSocketCodec extends ChannelDuplexHandler {

    public static final @NotNull String NAME = "ws.mqtt";

    @Inject
    MqttWebSocketCodec() {}

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof WebSocketFrame) {
            final WebSocketFrame webSocketFrame = (WebSocketFrame) msg;
            if ((msg instanceof BinaryWebSocketFrame) || (msg instanceof ContinuationWebSocketFrame)) {
                ctx.fireChannelRead(webSocketFrame.content());
            } else if (msg instanceof TextWebSocketFrame) {
                webSocketFrame.release();
                MqttDisconnectUtil.close(ctx.channel(), "Must not receive text websocket frames");
            } else if (msg instanceof CloseWebSocketFrame) {
                webSocketFrame.release();
                ctx.close();
            } else if (msg instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(webSocketFrame.content()));
            } else {
                webSocketFrame.release();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull Object msg,
            final @NotNull ChannelPromise promise) {

        if (msg instanceof ByteBuf) {
            ctx.write(new BinaryWebSocketFrame((ByteBuf) msg), promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}

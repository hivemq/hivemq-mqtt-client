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

import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class MqttWebSocketCodec extends ChannelDuplexHandler {

    public static final @NotNull String NAME = "ws.codec";

    @Inject
    MqttWebSocketCodec() {}

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof WebSocketFrame) {
            if ((msg instanceof BinaryWebSocketFrame) || (msg instanceof ContinuationWebSocketFrame)) {
                ctx.fireChannelRead(((WebSocketFrame) msg).content());
            } else if (msg instanceof TextWebSocketFrame) {
                MqttDisconnectUtil.close(ctx.channel(), "Must not receive text websocket frames");
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg,
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

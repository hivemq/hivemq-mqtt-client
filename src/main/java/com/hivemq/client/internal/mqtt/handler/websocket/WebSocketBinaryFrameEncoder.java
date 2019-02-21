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
 */

package com.hivemq.client.internal.mqtt.handler.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class WebSocketBinaryFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    public static final @NotNull String NAME = "ws.encoder";

    @Inject
    WebSocketBinaryFrameEncoder() {}

    @Override
    protected void encode(
            final @NotNull ChannelHandlerContext ctx, final @NotNull ByteBuf msg, final @NotNull List<Object> out) {

        out.add(new BinaryWebSocketFrame(msg.retain()));
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}

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
 */

package org.mqttbee.mqtt.handler.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

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

    public static final String NAME = "ws.encoder";

    @Inject
    WebSocketBinaryFrameEncoder() {
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final List<Object> out) {
        out.add(new BinaryWebSocketFrame(msg.retain()));
    }

}

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

package com.hivemq.client.internal.mqtt.handler.websocket;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MqttWebSocketHttpHeaders extends ChannelDuplexHandler {

    public static final String HTTP_HEADERS = "http.headers";
    private final @NotNull Map<String, String> httpHeaders;

    public MqttWebSocketHttpHeaders(@NotNull final Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpRequest) {
            final HttpRequest request = (HttpRequest) msg;
            this.httpHeaders.forEach((key, value) -> request.headers().set(key, value));
        }
        super.write(ctx, msg, promise);
    }
}

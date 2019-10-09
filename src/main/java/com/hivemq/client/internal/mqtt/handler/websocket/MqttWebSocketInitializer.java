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

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttWebSocketConfigImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.handler.MqttChannelInitializer;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttWebSocketInitializer extends ChannelInboundHandlerAdapter {

    private static final @NotNull String NAME = "ws.init";
    private static final @NotNull String HTTP_CODEC_NAME = "http.codec";
    private static final @NotNull String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private static final @NotNull String WEBSOCKET_URI_SCHEME = "ws";
    private static final @NotNull String WEBSOCKET_TLS_URI_SCHEME = "wss";

    private final @NotNull MqttClientConfig clientConfig;

    private final @NotNull MqttChannelInitializer mqttChannelInitializer;
    private final @NotNull MqttWebSocketCodec mqttWebSocketCodec;

    private @Nullable WebSocketClientHandshaker handshaker;

    @Inject
    MqttWebSocketInitializer(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttChannelInitializer mqttChannelInitializer,
            final @NotNull MqttWebSocketCodec mqttWebSocketCodec) {

        this.clientConfig = clientConfig;
        this.mqttChannelInitializer = mqttChannelInitializer;
        this.mqttWebSocketCodec = mqttWebSocketCodec;
    }

    public void initChannel(final @NotNull Channel channel, final @NotNull MqttWebSocketConfigImpl webSocketConfig)
            throws URISyntaxException {

        final URI uri = new URI((clientConfig.getTransportConfig().getRawSslConfig() == null) ? WEBSOCKET_URI_SCHEME :
                WEBSOCKET_TLS_URI_SCHEME, null, clientConfig.getServerHost(), clientConfig.getServerPort(),
                "/" + webSocketConfig.getServerPath(), webSocketConfig.getQueryString(), null);

        handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13,
                webSocketConfig.getSubprotocol(), true, null, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT, true,
                false);

        channel.pipeline()
                .addLast(HTTP_CODEC_NAME, new HttpClientCodec())
                .addLast(HTTP_AGGREGATOR_NAME, new HttpObjectAggregator(65_535))
                .addLast(NAME, this)
                .addLast(MqttWebSocketCodec.NAME, mqttWebSocketCodec);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        assert handshaker != null;

        ctx.fireChannelActive();
        handshaker.handshake(ctx.channel(), ctx.voidPromise());
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        assert handshaker != null;

        if (msg instanceof FullHttpResponse) {
            final FullHttpResponse response = (FullHttpResponse) msg;
            try {
                if (handshaker.isHandshakeComplete()) {
                    throw new IllegalStateException(
                            "Must not receive http response if websocket handshake is already finished.");
                }
                handshaker.finishHandshake(ctx.channel(), response);
                mqttChannelInitializer.initMqtt(ctx.channel());
                ctx.pipeline().remove(this);
            } finally {
                response.release();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        mqttChannelInitializer.exceptionCaught(ctx, cause);
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}

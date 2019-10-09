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
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttWebSocketInitializer extends ChannelInboundHandlerAdapter {

    private static final @NotNull String NAME = "ws.init";
    private static final @NotNull String PROTOCOL_HANDLER_NAME = "ws.protocol";
    private static final @NotNull String HTTP_CODEC_NAME = "http.codec";
    private static final @NotNull String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private static final @NotNull String WEBSOCKET_URI_SCHEME = "ws";
    private static final @NotNull String WEBSOCKET_TLS_URI_SCHEME = "wss";

    private final @NotNull MqttClientConfig clientConfig;

    private final @NotNull MqttChannelInitializer mqttChannelInitializer;
    private final @NotNull WebSocketBinaryFrameEncoder webSocketBinaryFrameEncoder;
    private final @NotNull WebSocketBinaryFrameDecoder webSocketBinaryFrameDecoder;

    @Inject
    MqttWebSocketInitializer(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttChannelInitializer mqttChannelInitializer,
            final @NotNull WebSocketBinaryFrameEncoder webSocketBinaryFrameEncoder,
            final @NotNull WebSocketBinaryFrameDecoder webSocketBinaryFrameDecoder) {

        this.clientConfig = clientConfig;
        this.mqttChannelInitializer = mqttChannelInitializer;
        this.webSocketBinaryFrameEncoder = webSocketBinaryFrameEncoder;
        this.webSocketBinaryFrameDecoder = webSocketBinaryFrameDecoder;
    }

    public void initChannel(final @NotNull Channel channel, final @NotNull MqttWebSocketConfigImpl webSocketConfig)
            throws URISyntaxException {

        final HttpClientCodec httpCodec = new HttpClientCodec();
        final HttpObjectAggregator httpAggregator =
                new HttpObjectAggregator(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);

        final URI uri = new URI((clientConfig.getTransportConfig().getRawSslConfig() == null) ? WEBSOCKET_URI_SCHEME :
                WEBSOCKET_TLS_URI_SCHEME, null, clientConfig.getServerHost(), clientConfig.getServerPort(),
                "/" + webSocketConfig.getServerPath(), webSocketConfig.getQueryString(), null);

        final WebSocketClientProtocolHandler webSocketClientProtocolHandler =
                new WebSocketClientProtocolHandler(uri, WebSocketVersion.V13, webSocketConfig.getSubprotocol(), true,
                        null, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);

        channel.pipeline()
                .addLast(HTTP_CODEC_NAME, httpCodec)
                .addLast(HTTP_AGGREGATOR_NAME, httpAggregator)
                .addLast(PROTOCOL_HANDLER_NAME, webSocketClientProtocolHandler)
                .addLast(NAME, this)
                .addLast(WebSocketBinaryFrameEncoder.NAME, webSocketBinaryFrameEncoder)
                .addLast(WebSocketBinaryFrameDecoder.NAME, webSocketBinaryFrameDecoder);
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            mqttChannelInitializer.initMqtt(ctx.channel());
            ctx.pipeline().remove(this);
        } else {
            ctx.fireUserEventTriggered(evt);
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

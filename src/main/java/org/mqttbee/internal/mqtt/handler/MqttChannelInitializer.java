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
 *
 */

package org.mqttbee.internal.mqtt.handler;

import dagger.Lazy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.MqttClientSslConfigImpl;
import org.mqttbee.internal.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.internal.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.internal.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.internal.mqtt.handler.connect.MqttConnAckSingle;
import org.mqttbee.internal.mqtt.handler.connect.MqttConnectHandler;
import org.mqttbee.internal.mqtt.handler.disconnect.MqttDisconnectHandler;
import org.mqttbee.internal.mqtt.handler.ssl.SslUtil;
import org.mqttbee.internal.mqtt.handler.websocket.MqttWebSocketClientProtocolHandler;
import org.mqttbee.internal.mqtt.handler.websocket.WebSocketBinaryFrameDecoder;
import org.mqttbee.internal.mqtt.handler.websocket.WebSocketBinaryFrameEncoder;
import org.mqttbee.internal.mqtt.ioc.ConnectionScope;
import org.mqttbee.rx.SingleFlow;

import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.net.URISyntaxException;

/**
 * Initializes:
 * <ul>
 * <li>the SSL handlers (optional)</li>
 * <li>the WebSocket handlers (optional)</li>
 * <li>the basic MQTT handlers: Encoder, AuthHandler, ConnectHandler, DisconnectHandler</li>
 * </ul>
 *
 * @author Silvio Giebl
 * @author David Katz
 */
@ConnectionScope
public class MqttChannelInitializer extends ChannelInitializer<Channel> {

    private static final @NotNull String HTTP_CODEC_NAME = "http.codec";
    private static final @NotNull String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull SingleFlow<Mqtt5ConnAck> connAckFlow;

    private final @NotNull MqttEncoder encoder;
    private final @NotNull MqttConnectHandler connectHandler;
    private final @NotNull MqttDisconnectHandler disconnectHandler;
    private final @NotNull MqttAuthHandler authHandler;

    private final @NotNull Lazy<WebSocketBinaryFrameEncoder> webSocketBinaryFrameEncoder;
    private final @NotNull Lazy<WebSocketBinaryFrameDecoder> webSocketBinaryFrameDecoder;

    @Inject
    MqttChannelInitializer(
            final @NotNull MqttClientConfig clientConfig, final @NotNull SingleFlow<Mqtt5ConnAck> connAckFlow,
            final @NotNull MqttEncoder encoder, final @NotNull MqttConnectHandler connectHandler,
            final @NotNull MqttDisconnectHandler disconnectHandler, final @NotNull MqttAuthHandler authHandler,
            final @NotNull Lazy<WebSocketBinaryFrameEncoder> webSocketBinaryFrameEncoder,
            final @NotNull Lazy<WebSocketBinaryFrameDecoder> webSocketBinaryFrameDecoder) {

        this.clientConfig = clientConfig;
        this.connAckFlow = connAckFlow;
        this.encoder = encoder;
        this.connectHandler = connectHandler;
        this.disconnectHandler = disconnectHandler;
        this.authHandler = authHandler;
        this.webSocketBinaryFrameEncoder = webSocketBinaryFrameEncoder;
        this.webSocketBinaryFrameDecoder = webSocketBinaryFrameDecoder;
    }

    @Override
    protected void initChannel(final @NotNull Channel channel) throws Exception {
        final MqttClientSslConfigImpl sslConfig = clientConfig.getRawSslConfig();
        if (sslConfig != null) {
            initSsl(channel, sslConfig);
        }
        final MqttWebSocketConfig webSocketConfig = clientConfig.getRawWebSocketConfig();
        if (webSocketConfig != null) {
            initMqttOverWebSocket(channel.pipeline(), webSocketConfig);
        } else {
            initMqttHandlers(channel.pipeline());
        }
    }

    public void initMqttHandlers(final @NotNull ChannelPipeline pipeline) {
        pipeline.addLast(MqttEncoder.NAME, encoder)
                .addLast(MqttAuthHandler.NAME, authHandler)
                .addLast(MqttConnectHandler.NAME, connectHandler)
                .addLast(MqttDisconnectHandler.NAME, disconnectHandler);
    }

    private void initMqttOverWebSocket(
            final @NotNull ChannelPipeline pipeline, final @NotNull MqttWebSocketConfig webSocketConfig)
            throws URISyntaxException {

        final MqttWebSocketClientProtocolHandler mqttWebSocketClientProtocolHandler =
                new MqttWebSocketClientProtocolHandler(clientConfig, webSocketConfig, this);

        pipeline.addLast(HTTP_CODEC_NAME, new HttpClientCodec())
                .addLast(HTTP_AGGREGATOR_NAME,
                        new HttpObjectAggregator(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT))
                .addLast(MqttWebSocketClientProtocolHandler.NAME, mqttWebSocketClientProtocolHandler)
                .addLast(WebSocketBinaryFrameEncoder.NAME, webSocketBinaryFrameEncoder.get())
                .addLast(WebSocketBinaryFrameDecoder.NAME, webSocketBinaryFrameDecoder.get());
    }

    private void initSsl(final @NotNull Channel channel, final @NotNull MqttClientSslConfigImpl sslConfig)
            throws SSLException {

        channel.pipeline().addFirst(SslUtil.createSslHandler(channel, sslConfig));
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        ctx.close();
        MqttConnAckSingle.onError(clientConfig, connAckFlow, cause);
    }
}

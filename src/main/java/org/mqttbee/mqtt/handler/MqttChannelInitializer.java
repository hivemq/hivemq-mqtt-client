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

package org.mqttbee.mqtt.handler;

import dagger.Lazy;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.reactivex.SingleEmitter;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientSslConfigImpl;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.mqtt.handler.connect.MqttConnectHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectHandler;
import org.mqttbee.mqtt.handler.ssl.SslUtil;
import org.mqttbee.mqtt.handler.websocket.MqttWebSocketClientProtocolHandler;
import org.mqttbee.mqtt.handler.websocket.WebSocketBinaryFrameDecoder;
import org.mqttbee.mqtt.handler.websocket.WebSocketBinaryFrameEncoder;
import org.mqttbee.mqtt.ioc.ConnectionScope;

import javax.inject.Inject;
import javax.inject.Named;
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

    private static final String HTTP_CODEC_NAME = "http.codec";
    private static final String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private final MqttClientData clientData;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;

    private final MqttEncoder encoder;
    private final MqttConnectHandler connectHandler;
    private final MqttDisconnectHandler disconnectHandler;
    private final ChannelHandler authHandler;

    private final Lazy<WebSocketBinaryFrameEncoder> webSocketBinaryFrameEncoder;
    private final Lazy<WebSocketBinaryFrameDecoder> webSocketBinaryFrameDecoder;

    @Inject
    MqttChannelInitializer(
            final MqttClientData clientData, final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            final MqttEncoder encoder, final MqttConnectHandler connectHandler,
            final MqttDisconnectHandler disconnectHandler, @Named("Auth") final ChannelHandler authHandler,
            final Lazy<WebSocketBinaryFrameEncoder> webSocketBinaryFrameEncoder,
            final Lazy<WebSocketBinaryFrameDecoder> webSocketBinaryFrameDecoder) {

        this.clientData = clientData;
        this.connAckEmitter = connAckEmitter;
        this.encoder = encoder;
        this.connectHandler = connectHandler;
        this.disconnectHandler = disconnectHandler;
        this.authHandler = authHandler;
        this.webSocketBinaryFrameEncoder = webSocketBinaryFrameEncoder;
        this.webSocketBinaryFrameDecoder = webSocketBinaryFrameDecoder;
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        final MqttClientSslConfigImpl sslConfig = clientData.getRawSslConfig();
        if (sslConfig != null) {
            initSsl(channel, sslConfig);
        }
        final MqttWebSocketConfig webSocketConfig = clientData.getRawWebSocketConfig();
        if (webSocketConfig != null) {
            initMqttOverWebSockets(channel.pipeline(), webSocketConfig);
        } else {
            initMqttHandlers(channel.pipeline());
        }
    }

    public void initMqttHandlers(@NotNull final ChannelPipeline pipeline) {
        pipeline.addLast(MqttEncoder.NAME, encoder);
        pipeline.addLast(MqttAuthHandler.NAME, authHandler);
        pipeline.addLast(MqttConnectHandler.NAME, connectHandler);
        pipeline.addLast(MqttDisconnectHandler.NAME, disconnectHandler);
    }

    private void initMqttOverWebSockets(
            @NotNull final ChannelPipeline pipeline, @NotNull final MqttWebSocketConfig webSocketConfig)
            throws URISyntaxException {

        final MqttWebSocketClientProtocolHandler mqttWebSocketClientProtocolHandler =
                new MqttWebSocketClientProtocolHandler(clientData, webSocketConfig, this);

        pipeline.addLast(HTTP_CODEC_NAME, new HttpClientCodec());
        pipeline.addLast(
                HTTP_AGGREGATOR_NAME, new HttpObjectAggregator(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT));
        pipeline.addLast(MqttWebSocketClientProtocolHandler.NAME, mqttWebSocketClientProtocolHandler);
        pipeline.addLast(WebSocketBinaryFrameEncoder.NAME, webSocketBinaryFrameEncoder.get());
        pipeline.addLast(WebSocketBinaryFrameDecoder.NAME, webSocketBinaryFrameDecoder.get());
    }

    private void initSsl(@NotNull final Channel channel, @NotNull final MqttClientSslConfigImpl sslConfig)
            throws SSLException {

        channel.pipeline().addFirst(SslUtil.createSslHandler(channel, sslConfig));
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        connAckEmitter.onError(cause);
        ctx.close();
    }

}

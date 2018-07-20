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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.reactivex.SingleEmitter;
import io.reactivex.exceptions.Exceptions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientSslConfig;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.mqtt.handler.auth.MqttDisconnectOnAuthHandler;
import org.mqttbee.mqtt.handler.connect.MqttConnectHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectHandler;
import org.mqttbee.mqtt.handler.ssl.SslUtil;
import org.mqttbee.mqtt.handler.websocket.MqttWebSocketClientProtocolHandler;
import org.mqttbee.mqtt.handler.websocket.WebSocketBinaryFrameDecoder;
import org.mqttbee.mqtt.handler.websocket.WebSocketBinaryFrameEncoder;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.message.connect.MqttConnect;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;

/**
 * Default channel initializer.
 *
 * @author Silvio Giebl
 * @author David Katz
 */
public class MqttChannelInitializer extends ChannelInitializer<Channel> {

    private static final String HTTP_CODEC_NAME = "http.codec";
    private static final String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private final MqttConnect connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final MqttClientData clientData;

    private ChannelComponent channelComponent;

    public MqttChannelInitializer(
            @NotNull final MqttConnect connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientData clientData) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    protected void initChannel(final Channel channel) {
        channelComponent = ChannelComponent.create(channel, clientData);
        final MqttClientSslConfig sslConfig = clientData.getRawSslConfig();
        if (sslConfig != null) {
            initSsl(channel, sslConfig);
        }
        final MqttWebSocketConfig websocketConfig = clientData.getRawWebsocketConfig();
        if (websocketConfig != null) {
            initMqttOverWebSockets(channel.pipeline(), websocketConfig);
        } else {
            initMqttHandlers(channel.pipeline());
        }
    }

    public void initMqttHandlers(@NotNull final ChannelPipeline pipeline) {
        pipeline.addLast(MqttEncoder.NAME, channelComponent.encoder());

        if (connect.getRawEnhancedAuthProvider() == null) {
            pipeline.addLast(MqttDisconnectOnAuthHandler.NAME, channelComponent.disconnectOnAuthHandler());
        } else {
            pipeline.addLast(MqttAuthHandler.NAME, channelComponent.authHandler());
        }

        pipeline.addLast(MqttConnectHandler.NAME, new MqttConnectHandler(connect, connAckEmitter, clientData));
        pipeline.addLast(MqttDisconnectHandler.NAME, channelComponent.disconnectHandler());

    }

    private void initMqttOverWebSockets(
            @NotNull final ChannelPipeline pipeline, @NotNull final MqttWebSocketConfig websocketConfig) {

        try {
            final MqttWebSocketClientProtocolHandler wsProtocolHandler =
                    new MqttWebSocketClientProtocolHandler(clientData, websocketConfig, this);

            pipeline.addLast(HTTP_CODEC_NAME, new HttpClientCodec());
            pipeline.addLast(
                    HTTP_AGGREGATOR_NAME, new HttpObjectAggregator(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT));
            pipeline.addLast(MqttWebSocketClientProtocolHandler.NAME, wsProtocolHandler);
            pipeline.addLast(WebSocketBinaryFrameEncoder.NAME, channelComponent.webSocketBinaryFrameEncoder());
            pipeline.addLast(WebSocketBinaryFrameDecoder.NAME, channelComponent.webSocketBinaryFrameDecoder());

        } catch (final URISyntaxException e) {
            Exceptions.propagate(e);
        }
    }

    private void initSsl(@NotNull final Channel channel, @NotNull final MqttClientSslConfig sslConfig) {
        try {
            final SslHandler sslHandler = SslUtil.createSslHandler(channel, sslConfig);
            channel.pipeline().addFirst(sslHandler);

        } catch (final SSLException e) {
            channel.pipeline().fireExceptionCaught(e);
        }
    }

}

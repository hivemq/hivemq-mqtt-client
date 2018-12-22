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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttWebSocketConfig;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.handler.MqttChannelInitializer;
import org.mqttbee.mqtt.ioc.ConnectionScope;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttWebSocketClientProtocolHandler extends WebSocketClientProtocolHandler {

    public static final @NotNull String NAME = "ws.mqtt";
    private static final @NotNull String WEBSOCKET_URI_SCHEME = "ws";
    private static final @NotNull String WEBSOCKET_TLS_URI_SCHEME = "wss";

    private static @NotNull URI createURI(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttWebSocketConfig webSocketConfig)
            throws URISyntaxException {

        return new URI(clientConfig.usesSsl() ? WEBSOCKET_TLS_URI_SCHEME : WEBSOCKET_URI_SCHEME, null,
                clientConfig.getServerHost(), clientConfig.getServerPort(), "/" + webSocketConfig.getServerPath(), null,
                null);
    }

    private final @NotNull MqttChannelInitializer channelInitializer;

    public MqttWebSocketClientProtocolHandler(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttWebSocketConfig webSocketConfig,
            final @NotNull MqttChannelInitializer channelInitializer) throws URISyntaxException {

        super(createURI(clientConfig, webSocketConfig), WebSocketVersion.V13, webSocketConfig.getSubprotocol(), true,
                null, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        if (evt == ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            channelInitializer.initMqttHandlers(ctx.pipeline());
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}

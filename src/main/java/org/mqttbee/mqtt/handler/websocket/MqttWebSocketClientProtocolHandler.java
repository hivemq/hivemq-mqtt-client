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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttWebsocketConfig;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.handler.MqttChannelInitializer;
import org.mqttbee.mqtt.ioc.ChannelScope;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttWebSocketClientProtocolHandler extends WebSocketClientProtocolHandler {

    public static final String NAME = "ws.mqtt";
    private static final String WEBSOCKET_URI_SCHEME = "ws";
    private static final String WEBSOCKET_TLS_URI_SCHEME = "wss";
    // https://www.iana.org/assignments/websocket/websocket.xml#subprotocol-name
    private static final String MQTT_SUBPROTOCOL = "mqtt";

    private static URI createURI(
            @NotNull final MqttClientData clientData, @NotNull final MqttWebsocketConfig websocketConfig)
            throws URISyntaxException {

        return new URI(clientData.usesSsl() ? WEBSOCKET_TLS_URI_SCHEME : WEBSOCKET_URI_SCHEME, null,
                clientData.getServerHost(), clientData.getServerPort(), "/" + websocketConfig.getServerPath(), null,
                null);
    }

    private final MqttChannelInitializer channelInitializer;

    public MqttWebSocketClientProtocolHandler(
            @NotNull final MqttClientData clientData, @NotNull final MqttWebsocketConfig websocketConfig,
            @NotNull final MqttChannelInitializer channelInitializer) throws URISyntaxException {

        super(createURI(clientData, websocketConfig), WebSocketVersion.V13, MQTT_SUBPROTOCOL, true, null,
                MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (ClientHandshakeStateEvent.HANDSHAKE_COMPLETE.equals(evt)) {
            channelInitializer.initMqttHandlers(ctx.pipeline());
        }
        ctx.fireUserEventTriggered(evt);
    }

}

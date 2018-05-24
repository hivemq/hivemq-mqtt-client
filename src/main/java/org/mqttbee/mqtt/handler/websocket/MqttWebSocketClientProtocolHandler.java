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
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;

import java.net.URI;

public class MqttWebSocketClientProtocolHandler extends WebSocketClientProtocolHandler {
    // https://www.iana.org/assignments/websocket/websocket.xml#subprotocol-name
    private static final String MQTT_SUBPROTOCOL = "mqtt";
    private Runnable action;

    public MqttWebSocketClientProtocolHandler(URI serverUri) {
        super(serverUri, WebSocketVersion.V13, MQTT_SUBPROTOCOL, true, null, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (ClientHandshakeStateEvent.HANDSHAKE_COMPLETE.equals(evt)) {
            // action allows other handlers to be added to the pipeline once the handshake is complete.
            action.run();
        }
        ctx.fireUserEventTriggered(evt);
    }

    public void onWebSocketHandshakeComplete(Runnable action) {
        this.action = action;
    }
}

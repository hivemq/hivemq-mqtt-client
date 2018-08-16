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

package org.mqttbee.mqtt.handler.auth;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Sends a DISCONNECT message if an AUTH message or a CONNACK message with enhanced auth data is received. This handler
 * is added if enhanced auth is not used at connection.
 *
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class MqttDisconnectOnAuthHandler extends ChannelInboundHandlerAdapter {

    @Inject
    MqttDisconnectOnAuthHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttAuth) {
            readAuth(ctx, (MqttAuth) msg);
        } else if (msg instanceof MqttConnAck) {
            readConnAck(ctx, (MqttConnAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readAuth(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth, "Server must not send AUTH"));
    }

    private void readConnAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttConnAck connAck) {
        if (connAck.getRawEnhancedAuth() != null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Server must not include auth in CONNACK"));
        } else {
            ctx.fireChannelRead(connAck);
        }
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}

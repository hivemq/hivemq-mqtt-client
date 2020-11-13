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

package com.hivemq.client.internal.mqtt.handler.auth;

import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.message.auth.MqttAuth;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;

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
public class MqttDisconnectOnAuthHandler extends ChannelInboundHandlerAdapter implements MqttAuthHandler {

    @Inject
    MqttDisconnectOnAuthHandler() {}

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttAuth) {
            readAuth(ctx, (MqttAuth) msg);
        } else if (msg instanceof MqttConnAck) {
            readConnAck(ctx, (MqttConnAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5AuthException(auth, "Server must not send AUTH"));
    }

    private void readConnAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        if (connAck.getRawEnhancedAuth() != null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5ConnAckException(connAck, "Server must not include auth in CONNACK"));
        } else {
            ctx.fireChannelRead(connAck);
        }
    }

    @Override
    public boolean isSharable() {
        return true;
    }
}

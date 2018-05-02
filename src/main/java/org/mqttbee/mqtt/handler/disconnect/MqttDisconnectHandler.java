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

package org.mqttbee.mqtt.handler.disconnect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Fires {@link ChannelCloseEvent}s if a DISCONNECT message is received or the channel was closed by the server. Only
 * one {@link ChannelCloseEvent} will be fired.
 *
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class MqttDisconnectHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "disconnect";

    @Inject
    MqttDisconnectHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof MqttDisconnect) {
            readDisconnect(ctx, (MqttDisconnect) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void readDisconnect(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttDisconnect disconnect) {

        ctx.pipeline().remove(this);
        closeFromServer(ctx.channel(), new Mqtt5MessageException(disconnect, "Server sent DISCONNECT"));
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        closeFromServer(ctx.channel(), new ChannelClosedException("Server closed channel without DISCONNECT"));
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof ChannelCloseEvent) {
            ctx.pipeline().remove(this);
        }
        ctx.fireUserEventTriggered(evt);
    }

    private static void closeFromServer(@NotNull final Channel channel, @NotNull final Throwable cause) {
        MqttDisconnectUtil.fireChannelCloseEvent(channel, cause, true);
        channel.close();
    }

}

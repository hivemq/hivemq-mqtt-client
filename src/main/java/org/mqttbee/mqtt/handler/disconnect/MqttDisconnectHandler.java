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

import io.netty.channel.*;
import io.reactivex.CompletableEmitter;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.MqttClientConnectionState;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import javax.inject.Inject;

/**
 * If the server initiated the closing of the channel (a Disconnect message is received or the channel is closed without
 * a Disconnect message), this handler fires a {@link ChannelCloseEvent}.
 * <p>
 * If the client initiated the closing of the channel (a {@link ChannelCloseEvent} was fired), the handler sends a
 * Disconnect message or closes the channel without a Disconnect message.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttDisconnectHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "disconnect";

    private final MqttClientData clientData;

    @Inject
    public MqttDisconnectHandler(@NotNull final MqttClientData clientData) {
        this.clientData = clientData;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof MqttDisconnect) {
            readDisconnect(ctx, (MqttDisconnect) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void readDisconnect(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttDisconnect disconnect) {
        ctx.pipeline().remove(this);
        closeFromServer(ctx.channel(), new Mqtt5MessageException(disconnect, "Server sent DISCONNECT"));
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        ctx.pipeline().remove(this);
        closeFromServer(ctx.channel(), new ChannelClosedException("Server closed channel without DISCONNECT"));
    }

    private void closeFromServer(@NotNull final Channel channel, @NotNull final Throwable cause) {
        setStateDisconnected();
        MqttDisconnectUtil.fireChannelCloseEvent(channel, new ChannelCloseEvent(cause, false, null));
        channel.close();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        ctx.fireUserEventTriggered(evt);
        if (evt instanceof ChannelCloseEvent) {
            handleChannelCloseEvent(ctx, (ChannelCloseEvent) evt);
        }
    }

    private void handleChannelCloseEvent(
            @NotNull final ChannelHandlerContext ctx, @NotNull final ChannelCloseEvent channelCloseEvent) {

        ctx.pipeline().remove(this);
        if (channelCloseEvent.fromClient()) {
            closeFromClient(ctx, channelCloseEvent);
        }
    }

    private void closeFromClient(
            @NotNull final ChannelHandlerContext ctx, @NotNull final ChannelCloseEvent channelCloseEvent) {

        setStateDisconnected();
        final MqttDisconnect disconnect = channelCloseEvent.getDisconnect();
        if (disconnect != null) {
            final CompletableEmitter completableEmitter = channelCloseEvent.getCompletableEmitter();
            if (completableEmitter != null) {
                ctx.writeAndFlush(disconnect).addListener((ChannelFuture future) -> {
                    future.channel().close();
                    if (future.isSuccess()) {
                        completableEmitter.onComplete();
                    } else {
                        completableEmitter.onError(future.cause());
                    }
                });
            } else if (clientData.getMqttVersion() == MqttVersion.MQTT_5_0) {
                ctx.writeAndFlush(disconnect).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.channel().close();
            }
        } else {
            ctx.channel().close();
        }
    }

    private void setStateDisconnected() {
//        MqttBeeComponent.INSTANCE.nettyBootstrap().free(clientData.getExecutorConfig()); // TODO
        clientData.setClientConnectionData(null);
        clientData.setServerConnectionData(null);
        clientData.getRawConnectionState().set(MqttClientConnectionState.DISCONNECTED);
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}

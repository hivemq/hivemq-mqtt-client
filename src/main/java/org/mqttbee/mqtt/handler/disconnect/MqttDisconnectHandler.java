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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.MqttClientConnectionState;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.rx.CompletableFlow;

import javax.inject.Inject;
import java.io.IOException;

import static org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil.fireChannelCloseEvent;

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

    public static final @NotNull String NAME = "disconnect";

    private final @NotNull MqttClientData clientData;
    private @Nullable ChannelHandlerContext ctx;

    @Inject
    public MqttDisconnectHandler(@NotNull final MqttClientData clientData) {
        this.clientData = clientData;
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) throws Exception {
        if (msg instanceof MqttDisconnect) {
            readDisconnect(ctx, (MqttDisconnect) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void readDisconnect(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnect disconnect) {
        closeFromServer(ctx, new Mqtt5MessageException(disconnect, "Server sent DISCONNECT"));
    }

    @Override
    public void channelInactive(final @NotNull ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        closeFromServer(ctx, new ChannelClosedException("Server closed channel without DISCONNECT"));
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (cause instanceof IOException) {
            closeFromServer(ctx, new ChannelClosedException(cause));
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    private void closeFromServer(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        setStateDisconnected(ctx);
        fireChannelCloseEvent(ctx.channel(), cause, false);
        ctx.channel().close();
    }

    public void disconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        clientData.getEventLoop().execute(() -> writeDisconnect(disconnect, flow));
    }

    private void writeDisconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            flow.onError(new NotConnectedException());
            return;
        }
        setStateDisconnected(ctx);
        fireChannelCloseEvent(ctx.channel(), new Mqtt5MessageException(disconnect, "Client sent DISCONNECT"), true);
        ctx.writeAndFlush(disconnect).addListener((ChannelFuture future) -> {
            future.channel().close();
            if (future.isSuccess()) {
                flow.onComplete();
            } else {
                flow.onError(future.cause());
            }
        });
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        ctx.fireUserEventTriggered(evt);
        if (evt instanceof ChannelCloseEvent) {
            handleChannelCloseEvent(ctx, (ChannelCloseEvent) evt);
        }
    }

    private void handleChannelCloseEvent(
            final @NotNull ChannelHandlerContext ctx, final @NotNull ChannelCloseEvent channelCloseEvent) {

        setStateDisconnected(ctx);
        if (channelCloseEvent.fromClient()) {
            final MqttDisconnect disconnect = channelCloseEvent.getDisconnect();
            if (disconnect != null) {
                if (clientData.getMqttVersion() == MqttVersion.MQTT_5_0) {
                    ctx.writeAndFlush(disconnect).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.channel().close();
                }
            } else {
                ctx.channel().close();
            }
        }
    }

    private void setStateDisconnected(final @NotNull ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        this.ctx = null;

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

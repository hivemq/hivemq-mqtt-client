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
import org.mqttbee.api.mqtt.MqttClientState;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.rx.CompletableFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil.fireDisconnectEvent;

/**
 * If the server initiated the closing of the channel (a Disconnect message is received or the channel is closed without
 * a Disconnect message), this handler fires a {@link MqttDisconnectEvent}.
 * <p>
 * If the client initiated the closing of the channel (a {@link MqttDisconnectEvent} was fired), the handler sends a
 * Disconnect message or closes the channel without a Disconnect message.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttDisconnectHandler extends ChannelInboundHandlerAdapter {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttDisconnectHandler.class);

    public static final @NotNull String NAME = "disconnect";

    private final @NotNull MqttClientConfig clientConfig;
    private @Nullable ChannelHandlerContext ctx;

    @Inject
    public MqttDisconnectHandler(final @NotNull MqttClientConfig clientConfig) {
        this.clientConfig = clientConfig;
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
        if (this.ctx != null) {
            this.ctx = null;
            fireDisconnectEvent(ctx.channel(), new Mqtt5MessageException(disconnect, "Server sent DISCONNECT."), false);
        }
    }

    @Override
    public void channelInactive(final @NotNull ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        if (this.ctx != null) {
            this.ctx = null;
            fireDisconnectEvent(
                    ctx.channel(), new ChannelClosedException("Server closed channel without DISCONNECT."), false);
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (this.ctx != null) {
            this.ctx = null;
            fireDisconnectEvent(ctx.channel(), new ChannelClosedException(cause), false);
        } else {
            LOGGER.error("Exception while disconnecting.", cause);
        }
    }

    public void disconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        if (!clientConfig.executeInEventLoop(() -> writeDisconnect(disconnect, flow))) {
            flow.onError(new NotConnectedException());
        }
    }

    private void writeDisconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx != null) {
            this.ctx = null;
            fireDisconnectEvent(ctx.channel(), new MqttDisconnectEvent.ByUser(disconnect, flow));
        } else {
            flow.onError(new NotConnectedException());
        }
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        ctx.fireUserEventTriggered(evt);
        if (evt instanceof MqttDisconnectEvent) {
            handleDisconnectEvent(ctx, (MqttDisconnectEvent) evt);
        }
    }

    private void handleDisconnectEvent(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnectEvent disconnectEvent) {

        this.ctx = null;
        clientConfig.setClientConnectionConfig(null);
        clientConfig.setServerConnectionConfig(null);
        clientConfig.getRawState().set(MqttClientState.DISCONNECTED);

        if (disconnectEvent.fromClient()) {
            final MqttDisconnect disconnect = disconnectEvent.getDisconnect();
            if (disconnect != null) {
                if (disconnectEvent instanceof MqttDisconnectEvent.ByUser) {
                    final CompletableFlow flow = ((MqttDisconnectEvent.ByUser) disconnectEvent).getFlow();
                    ctx.writeAndFlush(disconnect).addListener((ChannelFuture future) -> {
                        future.channel().close();
                        if (future.isSuccess()) {
                            flow.onComplete();
                        } else {
                            flow.onError(future.cause());
                        }
                    });
                } else if (clientConfig.getMqttVersion() == MqttVersion.MQTT_5_0) {
                    ctx.writeAndFlush(disconnect).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.channel().close();
                }
            } else {
                ctx.channel().close();
            }
        } else {
            ctx.channel().close();
        }
    }

    @Override
    public void channelUnregistered(final @NotNull ChannelHandlerContext ctx) {
        ctx.fireChannelUnregistered();
        clientConfig.releaseEventLoop();
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}

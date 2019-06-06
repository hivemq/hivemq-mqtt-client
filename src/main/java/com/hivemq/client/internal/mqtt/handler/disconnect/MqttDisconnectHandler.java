/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.disconnect;

import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.handler.MqttConnectionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.MqttSession;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnAckSingle;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictions;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.rx.CompletableFlow;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import static com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil.fireDisconnectEvent;

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
public class MqttDisconnectHandler extends MqttConnectionAwareHandler {

    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttDisconnectHandler.class);
    public static final @NotNull String NAME = "disconnect";

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttSession session;
    private boolean once = true;

    @Inject
    MqttDisconnectHandler(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttSession session) {
        this.clientConfig = clientConfig;
        this.session = session;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttDisconnect) {
            readDisconnect(ctx, (MqttDisconnect) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readDisconnect(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnect disconnect) {
        if (once) {
            once = false;
            fireDisconnectEvent(ctx.channel(), new Mqtt5DisconnectException(disconnect, "Server sent DISCONNECT."),
                    MqttDisconnectSource.SERVER);
        }
    }

    @Override
    public void channelInactive(final @NotNull ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        if (once) {
            once = false;
            fireDisconnectEvent(ctx.channel(),
                    new ConnectionClosedException("Server closed connection without DISCONNECT."),
                    MqttDisconnectSource.SERVER);
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (once) {
            once = false;
            fireDisconnectEvent(ctx.channel(), new ConnectionClosedException(cause), MqttDisconnectSource.CLIENT);
        } else {
            LOGGER.error("Exception while disconnecting.", cause);
        }
    }

    public void disconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        if (!clientConfig.executeInEventLoop(() -> writeDisconnect(disconnect, flow))) {
            flow.onError(MqttClientStateExceptions.notConnected());
        }
    }

    private void writeDisconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        final ChannelHandlerContext ctx = this.ctx;
        if ((ctx != null) && once) {
            once = false;
            fireDisconnectEvent(ctx.channel(), new MqttDisconnectEvent.ByUser(disconnect, flow));
        } else {
            flow.onError(MqttClientStateExceptions.notConnected());
        }
    }

    @Override
    protected void onDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            return;
        }
        super.onDisconnectEvent(disconnectEvent);
        once = false;

        final MqttClientConnectionConfig connectionConfig = clientConfig.getRawConnectionConfig();
        if (connectionConfig != null) {
            session.expire(disconnectEvent.getCause(), connectionConfig, ctx.channel().eventLoop());

            reconnect(disconnectEvent, connectionConfig, ctx.channel().eventLoop());

            clientConfig.setConnectionConfig(null);
        }

        if (disconnectEvent.getSource() == MqttDisconnectSource.SERVER) {
            ctx.channel().close();
        } else {
            final MqttDisconnect disconnect = disconnectEvent.getDisconnect();
            if (disconnect != null) {
                if (disconnectEvent instanceof MqttDisconnectEvent.ByUser) {
                    final CompletableFlow flow = ((MqttDisconnectEvent.ByUser) disconnectEvent).getFlow();
                    ctx.writeAndFlush(disconnect).addListener((ChannelFuture future) -> {
                        future.channel().close();
                        if (future.isSuccess()) {
                            flow.onComplete();
                        } else {
                            flow.onError(new ConnectionClosedException(future.cause()));
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
        }
    }

    private void reconnect(
            final @NotNull MqttDisconnectEvent disconnectEvent,
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        final MqttClientConfig.ConnectDefaults connectDefaults = clientConfig.getConnectDefaults();
        final Mqtt5EnhancedAuthMechanism enhancedAuthMechanism = connectionConfig.getRawEnhancedAuthMechanism();
        // @formatter:off
        final MqttConnect connect = new MqttConnect(
                connectionConfig.getKeepAlive(),
                connectionConfig.getSessionExpiryInterval() == 0,
                connectionConfig.getSessionExpiryInterval(),
                new MqttConnectRestrictions(
                        connectionConfig.getReceiveMaximum(),
                        connectionConfig.getSendMaximum(),
                        connectionConfig.getMaximumPacketSize(),
                        connectionConfig.getSendMaximumPacketSize(),
                        connectionConfig.getTopicAliasMaximum(),
                        connectionConfig.getSendTopicAliasMaximum(),
                        connectionConfig.isProblemInformationRequested(),
                        connectionConfig.isResponseInformationRequested()
                ),
                connectDefaults.getSimpleAuth(),
                (enhancedAuthMechanism == null) ? connectDefaults.getEnhancedAuthMechanism() : enhancedAuthMechanism,
                connectDefaults.getWillPublish(),
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        // @formatter:on
        MqttConnAckSingle.reconnect(clientConfig, disconnectEvent.getSource(), disconnectEvent.getCause(), connect,
                connectionConfig.getTransportConfig(), eventLoop);
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

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

package com.hivemq.client2.internal.mqtt.handler.disconnect;

import com.hivemq.client2.internal.logging.InternalLogger;
import com.hivemq.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client2.internal.mqtt.handler.MqttConnectionAwareHandler;
import com.hivemq.client2.internal.mqtt.handler.MqttSession;
import com.hivemq.client2.internal.mqtt.handler.connect.MqttConnAckSingle;
import com.hivemq.client2.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnectRestrictions;
import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.internal.rx.CompletableFlow;
import com.hivemq.client2.mqtt.MqttVersion;
import com.hivemq.client2.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client2.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client2.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DuplexChannel;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.hivemq.client2.internal.mqtt.handler.disconnect.MqttDisconnectUtil.fireDisconnectEvent;

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

    public static final @NotNull String NAME = "disconnect";
    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttDisconnectHandler.class);
    private static final @NotNull Object STATE_CLOSED = new Object();
    private static final int DISCONNECT_TIMEOUT = 10; // TODO configurable

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttSession session;
    private @Nullable Object state = null;

    @Inject
    MqttDisconnectHandler(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttSession session) {
        this.clientConfig = clientConfig;
        this.session = session;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttDisconnect) {
            readDisconnect(ctx, (MqttDisconnect) msg);
        } else if (msg instanceof MqttConnAck) {
            readConnAck(ctx, (MqttConnAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readDisconnect(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnect disconnect) {
        LOGGER.debug("Read DISCONNECT {} from {}", disconnect, ctx.channel().remoteAddress());
        if (state == null) {
            state = STATE_CLOSED;
            fireDisconnectEvent(ctx.channel(), new Mqtt5DisconnectException(disconnect, "Server sent DISCONNECT."),
                    MqttDisconnectSource.SERVER);
        }
    }

    private void readConnAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttConnAck connAck) {
        LOGGER.debug("Read CONNACK {} from {}", connAck, ctx.channel().remoteAddress());
        if (state == null) {
            state = STATE_CLOSED;
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5ConnAckException(connAck, "Must not receive second CONNACK."));
        }
    }

    @Override
    public void channelInactive(final @NotNull ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
        if (state == null) {
            state = STATE_CLOSED;
            fireDisconnectEvent(ctx.channel(),
                    new ConnectionClosedException("Server closed connection without DISCONNECT."),
                    MqttDisconnectSource.SERVER);
        } else if (state instanceof DisconnectingState) {
            final DisconnectingState disconnectingState = (DisconnectingState) state;
            state = STATE_CLOSED;
            disconnectingState.timeoutFuture.cancel(false);
            disconnected(disconnectingState.channel, disconnectingState.disconnectEvent);
            disconnectingState.disconnectEvent.getFlow().onComplete();
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (state == null) {
            state = STATE_CLOSED;
            // will be logged in MqttDisconnectUtil
            fireDisconnectEvent(ctx.channel(), new ConnectionClosedException(cause), MqttDisconnectSource.CLIENT);
        } else if (!(cause instanceof IOException)) {
            LOGGER.warn("Exception while disconnecting: {}, remote address: {}", cause, ctx.channel().remoteAddress());
        }
    }

    public void disconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        LOGGER.trace("disconnect: schedule DISCONNECT {}", disconnect);
        if (!clientConfig.executeInEventLoop(() -> writeDisconnect(disconnect, flow))) {
            flow.onError(MqttClientStateExceptions.notConnected());
        }
    }

    private void writeDisconnect(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
        final ChannelHandlerContext ctx = this.ctx;
        if ((ctx != null) && (state == null)) {
            state = STATE_CLOSED;
            LOGGER.trace("Fire DISCONNECT event {}", disconnect);
            fireDisconnectEvent(ctx.channel(), new MqttDisconnectEvent.ByUser(disconnect, flow));
        } else {
            flow.onError(MqttClientStateExceptions.notConnected());
        }
    }

    @Override
    protected void onDisconnectEvent(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttDisconnectEvent disconnectEvent) {

        state = STATE_CLOSED;

        final Channel channel = ctx.channel();

        if (disconnectEvent.getSource() == MqttDisconnectSource.SERVER) {
            LOGGER.debug("OnDisconnectedEvent: server closed connection: source: {}, cause: {}, remote address: {}",
                    disconnectEvent.getSource(), disconnectEvent.getCause(), ctx.channel().remoteAddress());
            disconnected(channel, disconnectEvent);
            channel.close();
            return;
        }
        LOGGER.debug("OnDisconnectedEvent: source: {}, cause: {}", disconnectEvent.getSource(),
                disconnectEvent.getCause());

        MqttDisconnect disconnect = disconnectEvent.getDisconnect();
        if (disconnect != null) {

            final long sessionExpiryInterval = disconnect.getRawSessionExpiryInterval();
            if (sessionExpiryInterval != MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
                final MqttClientConnectionConfig connectionConfig = clientConfig.getRawConnectionConfig();
                if (connectionConfig != null) {
                    if ((sessionExpiryInterval > 0) && connectionConfig.isCleanStop()) {
                        LOGGER.warn(
                                "Session expiry interval must not be set in DISCONNECT if it was set to 0 in CONNECT");
                        disconnect = disconnect.extend().sessionExpiryInterval(0).build();
                    } else {
                        connectionConfig.setSessionExpiryInterval(sessionExpiryInterval);
                    }
                }
            }

            if (disconnectEvent instanceof MqttDisconnectEvent.ByUser) {
                final MqttDisconnectEvent.ByUser disconnectEventByUser = (MqttDisconnectEvent.ByUser) disconnectEvent;
                LOGGER.debug("Write DISCONNECT (by user) {} to {}", disconnect, ctx.channel().remoteAddress());
                ctx.writeAndFlush(disconnect).addListener(f -> {
                    if (f.isSuccess()) {
                        ((DuplexChannel) channel).shutdownOutput().addListener(cf -> {
                            if (cf.isSuccess()) {
                                LOGGER.trace("DISCONNECT successful");
                                state = new DisconnectingState(channel, disconnectEventByUser);
                            } else {
                                LOGGER.debug("DISCONNECT failed: {}, remote address {}", cf.cause(), ctx.channel().remoteAddress());
                                disconnected(channel, disconnectEvent);
                                disconnectEventByUser.getFlow().onError(new ConnectionClosedException(cf.cause()));
                            }
                        });
                    } else {
                        LOGGER.debug("DISCONNECT failed: {}, remote address {}", f.cause(), ctx.channel().remoteAddress());
                        disconnected(channel, disconnectEvent);
                        disconnectEventByUser.getFlow().onError(new ConnectionClosedException(f.cause()));
                    }
                });

            } else if (clientConfig.getMqttVersion() == MqttVersion.MQTT_5_0) {
                LOGGER.debug("Write DISCONNECT {} to {}", disconnect, ctx.channel().remoteAddress());
                ctx.writeAndFlush(disconnect)
                        .addListener(f -> channel.close().addListener(cf -> {
                            LOGGER.trace("DISCONNECT done");
                            disconnected(channel, disconnectEvent);}));

            } else {
                LOGGER.debug("Close channel (DISCONNECT) {} to {}", disconnect, ctx.channel().remoteAddress());
                channel.close().addListener(cf -> {
                    LOGGER.trace("DISCONNECT done");
                    disconnected(channel, disconnectEvent);});
            }
        } else {
            LOGGER.debug("Close channel (DISCONNECT) to {}", ctx.channel().remoteAddress());
            channel.close().addListener(cf -> {
                LOGGER.trace("DISCONNECT done");
                disconnected(channel, disconnectEvent);});
        }
    }

    private void disconnected(final @NotNull Channel channel, final @NotNull MqttDisconnectEvent disconnectEvent) {
        final MqttClientConnectionConfig connectionConfig = clientConfig.getRawConnectionConfig();
        if (connectionConfig != null) {
            session.expire(disconnectEvent.getCause(), connectionConfig, channel.eventLoop());

            reconnect(disconnectEvent, connectionConfig, channel.eventLoop());

            clientConfig.setConnectionConfig(null);
        }
    }

    private void reconnect(
            final @NotNull MqttDisconnectEvent disconnectEvent,
            final @NotNull MqttClientConnectionConfig connectionConfig,
            final @NotNull EventLoop eventLoop) {

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
        MqttConnAckSingle.reconnect(
                clientConfig, disconnectEvent.getSource(), disconnectEvent.getCause(), connect, eventLoop);
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    private static class DisconnectingState implements Runnable {

        private final @NotNull Channel channel;
        private final MqttDisconnectEvent.@NotNull ByUser disconnectEvent;
        private final @NotNull ScheduledFuture<?> timeoutFuture;

        DisconnectingState(final @NotNull Channel channel, final MqttDisconnectEvent.@NotNull ByUser disconnectEvent) {
            this.channel = channel;
            this.disconnectEvent = disconnectEvent;
            timeoutFuture = channel.eventLoop().schedule(this, DISCONNECT_TIMEOUT, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            channel.close();
        }
    }
}

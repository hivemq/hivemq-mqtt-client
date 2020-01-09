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

package com.hivemq.client.internal.mqtt.handler.connect;

import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoder;
import com.hivemq.client.internal.mqtt.codec.encoder.MqttEncoder;
import com.hivemq.client.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import com.hivemq.client.internal.mqtt.handler.MqttSession;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.handler.ping.MqttPingHandler;
import com.hivemq.client.internal.mqtt.handler.util.MqttTimeoutInboundHandler;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client.internal.mqtt.lifecycle.MqttClientConnectedContextImpl;
import com.hivemq.client.internal.mqtt.message.MqttMessage;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictions;
import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAckRestrictions;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Handles the connection to a MQTT Server.
 * <ul>
 * <li>Writes the CONNECT message.</li>
 * <li>Handles the CONNACK message.</li>
 * <li>Disconnects or closes the channel on receiving other messages before CONNACK.</li>
 * <li>Disconnects or closes the channel if the CONNACK message is not received in the timeout.</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttConnectHandler extends MqttTimeoutInboundHandler {

    public static final @NotNull String NAME = "connect";
    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttConnectHandler.class);
    private static final int CONNACK_TIMEOUT = 60; // TODO configurable

    private final @NotNull MqttConnect connect;
    private final @NotNull MqttConnAckFlow connAckFlow;
    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttSession session;
    private final @NotNull MqttDecoder decoder;
    private final @NotNull MqttDisconnectOnConnAckHandler disconnectOnConnAckHandler;

    private boolean connectCalled = false;

    @Inject
    MqttConnectHandler(
            final @NotNull MqttConnect connect, final @NotNull MqttConnAckFlow connAckFlow,
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttSession session,
            final @NotNull MqttDecoder decoder,
            final @NotNull MqttDisconnectOnConnAckHandler disconnectOnConnAckHandler) {

        this.connect = connect;
        this.connAckFlow = connAckFlow;
        this.clientConfig = clientConfig;
        this.session = session;
        this.decoder = decoder;
        this.disconnectOnConnAckHandler = disconnectOnConnAckHandler;
    }

    @Override
    public void channelActive(final @NotNull ChannelHandlerContext ctx) {
        if (!connectCalled) {
            connectCalled = true;
            writeConnect(ctx);
        }
        ctx.fireChannelActive();
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        super.handlerAdded(ctx);

        if (!connectCalled && ctx.channel().isActive()) {
            connectCalled = true;
            writeConnect(ctx);
        }
    }

    /**
     * Writes the Connect message.
     * <p>
     * The MQTT message Decoder is added after the write succeeded as the server is not allowed to send messages before
     * the CONNECT is sent.
     * <p>
     * If the write fails, the channel is closed.
     *
     * @param ctx the channel handler context.
     */
    private void writeConnect(final @NotNull ChannelHandlerContext ctx) {
        ctx.writeAndFlush((connect.getRawEnhancedAuthMechanism() == null) ?
                connect.createStateful(clientConfig.getRawClientIdentifier(), null) : connect).addListener(this);
    }

    @Override
    protected void operationSuccessful(final @NotNull ChannelHandlerContext ctx) {
        if (connect.getRawEnhancedAuthMechanism() == null) {
            scheduleTimeout(ctx.channel());
        }
        ctx.pipeline().addAfter(MqttEncoder.NAME, MqttDecoder.NAME, decoder);
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        cancelTimeout();

        if (msg instanceof MqttConnAck) {
            readConnAck((MqttConnAck) msg, ctx.channel());
        } else {
            readOtherThanConnAck(msg, ctx.channel());
        }
    }

    /**
     * Handles the given CONNACK message.
     * <p>
     * If it contains an Error Code, the channel is closed.
     * <p>
     * Otherwise it is validated. Then this handler is removed from the pipeline and the {@link MqttPingHandler} and
     * {@link MqttDisconnectOnConnAckHandler} are added to the pipeline.
     *
     * @param connAck the CONNACK message.
     * @param channel the channel.
     */
    private void readConnAck(final @NotNull MqttConnAck connAck, final @NotNull Channel channel) {
        if (connAck.getReasonCode().isError()) {
            MqttDisconnectUtil.close(channel, new Mqtt5ConnAckException(connAck,
                    "CONNECT failed as CONNACK contained an Error Code: " + connAck.getReasonCode() + "."));

        } else if (validateClientIdentifier(connAck, channel)) {
            final MqttClientConnectionConfig connectionConfig = addConnectionConfig(connAck, channel);

            channel.pipeline().remove(this).addLast(MqttDisconnectOnConnAckHandler.NAME, disconnectOnConnAckHandler);

            ((MqttEncoder) channel.pipeline().get(MqttEncoder.NAME)).onConnected(connectionConfig);

            session.startOrResume(connAck, connectionConfig, channel.pipeline(), channel.eventLoop());

            final int keepAlive = connectionConfig.getKeepAlive();
            if (keepAlive > 0) {
                channel.pipeline().addAfter(MqttDecoder.NAME, MqttPingHandler.NAME, new MqttPingHandler(keepAlive));
            }

            clientConfig.getRawState().set(MqttClientState.CONNECTED);

            final ImmutableList<MqttClientConnectedListener> connectedListeners = clientConfig.getConnectedListeners();
            if (!connectedListeners.isEmpty()) {
                final MqttClientConnectedContext context =
                        MqttClientConnectedContextImpl.of(clientConfig, connect, connAck);
                for (final MqttClientConnectedListener connectedListener : connectedListeners) {
                    try {
                        connectedListener.onConnected(context);
                    } catch (final Throwable t) {
                        LOGGER.error("Unexpected exception thrown by connected listener.", t);
                    }
                }
            }

            connAckFlow.onSuccess(connAck);
        }
    }

    /**
     * The server must not send other messages before CONNACK.
     * <p>
     * If a MQTT message other than CONNACK is received after the CONNECT message was sent, a DISCONNECT message is sent
     * and the channel is closed.
     * <p>
     * If a message is received before the CONNECT message was sent, the channel is closed.
     *
     * @param msg     the received message other than CONNACK.
     * @param channel the channel.
     */
    private void readOtherThanConnAck(final @NotNull Object msg, final @NotNull Channel channel) {
        if (msg instanceof MqttMessage) {
            MqttDisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    ((MqttMessage) msg).getType() + " message must not be received before CONNACK");
        } else {
            MqttDisconnectUtil.close(channel, "No data must be received before CONNECT is sent");
        }
    }

    /**
     * Validates the given CONNACK message.
     * <p>
     * If validation fails, disconnection and closing of the channel is already handled.
     *
     * @param connAck the CONNACK message.
     * @param channel the channel.
     * @return true if the CONNACK message is valid, otherwise false.
     */
    private boolean validateClientIdentifier(final @NotNull MqttConnAck connAck, final @NotNull Channel channel) {
        final MqttClientIdentifierImpl clientIdentifier = clientConfig.getRawClientIdentifier();
        final MqttClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();

        if (clientIdentifier == MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) {
            if ((clientConfig.getMqttVersion() == MqttVersion.MQTT_5_0) && (assignedClientIdentifier == null)) {
                MqttDisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                        new Mqtt5ConnAckException(connAck, "Server did not assign a Client Identifier"));
                return false;
            }
        } else {
            if (assignedClientIdentifier != null) {
                LOGGER.warn("Server overwrote the Client Identifier {} with {}", clientIdentifier,
                        assignedClientIdentifier);
            }
        }
        if (assignedClientIdentifier != null) {
            clientConfig.setClientIdentifier(assignedClientIdentifier);
        }
        return true;
    }

    private @NotNull MqttClientConnectionConfig addConnectionConfig(
            final @NotNull MqttConnAck connAck, final @NotNull Channel channel) {

        int keepAlive = connAck.getRawServerKeepAlive();
        if (keepAlive == MqttConnAck.KEEP_ALIVE_FROM_CONNECT) {
            keepAlive = connect.getKeepAlive();
        }

        long sessionExpiryInterval = connAck.getRawSessionExpiryInterval();
        if (sessionExpiryInterval == MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            sessionExpiryInterval = connect.getSessionExpiryInterval();
        }

        final MqttConnectRestrictions restrictions = connect.getRestrictions();
        final MqttConnAckRestrictions connAckRestrictions = connAck.getRestrictions();

        // @formatter:off
        final MqttClientConnectionConfig connectionConfig = new MqttClientConnectionConfig(
                connAckFlow.getTransportConfig(),
                keepAlive,
                connect.isCleanStart(),
                connect.getSessionExpiryInterval() == 0,
                sessionExpiryInterval,
                connect.getRawSimpleAuth() != null,
                connect.getRawWillPublish() != null,
                connect.getRawEnhancedAuthMechanism(),
                restrictions.getReceiveMaximum(),
                restrictions.getMaximumPacketSize(),
                restrictions.getTopicAliasMaximum(),
                restrictions.isRequestProblemInformation(),
                restrictions.isRequestResponseInformation(),
                Math.min(restrictions.getSendMaximum(), connAckRestrictions.getReceiveMaximum()),
                Math.min(restrictions.getSendMaximumPacketSize(), connAckRestrictions.getMaximumPacketSize()),
                Math.min(restrictions.getSendTopicAliasMaximum(), connAckRestrictions.getTopicAliasMaximum()),
                connAckRestrictions.getMaximumQos(),
                connAckRestrictions.isRetainAvailable(),
                connAckRestrictions.isWildcardSubscriptionAvailable(),
                connAckRestrictions.isSharedSubscriptionAvailable(),
                connAckRestrictions.areSubscriptionIdentifiersAvailable(),
                channel);
        // @formatter:on

        clientConfig.setConnectionConfig(connectionConfig);
        return connectionConfig;
    }

    @Override
    protected void onDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            return;
        }
        super.onDisconnectEvent(disconnectEvent);
        MqttConnAckSingle.reconnect(clientConfig, disconnectEvent.getSource(), disconnectEvent.getCause(), connect,
                connAckFlow, ctx.channel().eventLoop());
    }

    @Override
    protected long getTimeout() {
        return CONNACK_TIMEOUT;
    }

    @Override
    protected @NotNull Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.PROTOCOL_ERROR;
    }

    @Override
    protected @NotNull String getTimeoutReasonString() {
        return "Timeout while waiting for CONNACK";
    }
}

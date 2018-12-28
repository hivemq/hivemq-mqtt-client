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

package org.mqttbee.mqtt.handler.connect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientState;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.MqttClientConnectionConfig;
import org.mqttbee.mqtt.MqttServerConnectionConfig;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.decoder.MqttDecoder;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.handler.MqttSession;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.ping.MqttPingHandler;
import org.mqttbee.mqtt.handler.util.MqttTimeoutInboundHandler;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckRestrictions;
import org.mqttbee.rx.SingleFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttConnectHandler.class);
    private static final int CONNACK_TIMEOUT = 60; // TODO configurable

    private final @NotNull MqttConnect connect;
    private final @NotNull SingleFlow<Mqtt5ConnAck> connAckFlow;
    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttSession session;
    private final @NotNull MqttDecoder decoder;
    private final @NotNull MqttDisconnectOnConnAckHandler disconnectOnConnAckHandler;

    private boolean connectCalled = false;

    @Inject
    MqttConnectHandler(
            final @NotNull MqttConnect connect, final @NotNull SingleFlow<Mqtt5ConnAck> connAckFlow,
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
        ctx.writeAndFlush((connect.getRawEnhancedAuthProvider() == null) ?
                connect.createStateful(clientConfig.getRawClientIdentifier(), null) : connect).addListener(this);
    }

    @Override
    public void operationComplete(final @NotNull ChannelFuture future) {
        if (ctx == null) {
            return;
        }
        if (future.isSuccess()) {
            if (connect.getRawEnhancedAuthProvider() == null) {
                scheduleTimeout(ctx.channel());
            }
            ctx.pipeline().addAfter(MqttEncoder.NAME, MqttDecoder.NAME, decoder);
        } else {
            MqttDisconnectUtil.close(ctx.channel(), future.cause());
        }
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
            MqttDisconnectUtil.close(
                    channel, new Mqtt5MessageException(connAck, "Connection failed with CONNACK with Error Code"));

        } else if (validateClientIdentifier(connAck, channel)) {
            final MqttClientConnectionConfig clientConnectionConfig = addClientConfig(connAck, channel);
            final MqttServerConnectionConfig serverConnectionConfig = addServerConfig(connAck);

            final ChannelPipeline pipeline = channel.pipeline();
            pipeline.remove(this);
            String beforeHandlerName = MqttDecoder.NAME;

            final int keepAlive = clientConnectionConfig.getKeepAlive();
            if (keepAlive > 0) {
                pipeline.addAfter(beforeHandlerName, MqttPingHandler.NAME, new MqttPingHandler(keepAlive));
                beforeHandlerName = MqttPingHandler.NAME;
            }

            session.startOrResume(connAck, pipeline, beforeHandlerName, clientConnectionConfig, serverConnectionConfig);

            pipeline.addLast(MqttDisconnectOnConnAckHandler.NAME, disconnectOnConnAckHandler);

            clientConfig.getRawState().set(MqttClientState.CONNECTED);
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
            final MqttMessage mqttMessage = (MqttMessage) msg;
            final String message = mqttMessage.getType() + " message must not be received before CONNACK";
            MqttDisconnectUtil.disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(mqttMessage, message));
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
                        new Mqtt5MessageException(connAck, "Server did not assign a Client Identifier"));
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

    private @NotNull MqttClientConnectionConfig addClientConfig(
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

        final MqttClientConnectionConfig clientConnectionConfig =
                new MqttClientConnectionConfig(keepAlive, sessionExpiryInterval, restrictions.getReceiveMaximum(),
                        restrictions.getMaximumPacketSize(), restrictions.getTopicAliasMaximum(),
                        connect.getRawEnhancedAuthProvider(), connect.getRawWillPublish() != null,
                        connect.isProblemInformationRequested(), connect.isResponseInformationRequested(), channel);

        clientConfig.setClientConnectionConfig(clientConnectionConfig);
        return clientConnectionConfig;
    }

    private @NotNull MqttServerConnectionConfig addServerConfig(final @NotNull MqttConnAck connAck) {
        final MqttConnAckRestrictions restrictions = connAck.getRestrictions();

        final MqttServerConnectionConfig serverConnectionConfig =
                new MqttServerConnectionConfig(restrictions.getReceiveMaximum(), restrictions.getMaximumPacketSize(),
                        restrictions.getTopicAliasMaximum(), restrictions.getMaximumQos(),
                        restrictions.isRetainAvailable(), restrictions.isWildcardSubscriptionAvailable(),
                        restrictions.isSharedSubscriptionAvailable(),
                        restrictions.areSubscriptionIdentifiersAvailable());

        clientConfig.setServerConnectionConfig(serverConnectionConfig);
        return serverConnectionConfig;
    }

    @Override
    protected void onDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        super.onDisconnectEvent(disconnectEvent);
        connAckFlow.onError(disconnectEvent.getCause());
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

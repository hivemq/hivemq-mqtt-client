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

import dagger.Lazy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.reactivex.SingleEmitter;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoder;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.handler.disconnect.ChannelCloseEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.ping.MqttPingHandler;
import org.mqttbee.mqtt.handler.publish.MqttIncomingQosHandler;
import org.mqttbee.mqtt.handler.publish.MqttOutgoingQosHandler;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt.handler.util.ChannelInboundHandlerWithTimeout;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckRestrictions;
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
public class MqttConnectHandler extends ChannelInboundHandlerWithTimeout {

    public static final String NAME = "connect";
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConnectHandler.class);
    private static final int CONNACK_TIMEOUT = 60; // TODO configurable

    private final MqttConnect connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final MqttClientData clientData;

    private final MqttDecoder decoder;
    private final Lazy<MqttSubscriptionHandler> subscriptionHandler;
    private final Lazy<MqttIncomingQosHandler> incomingQosHandler;
    private final Lazy<MqttOutgoingQosHandler> outgoingQosHandler;
    private final MqttDisconnectOnConnAckHandler disconnectOnConnAckHandler;

    private boolean connectCalled = false;

    @Inject
    MqttConnectHandler(
            final MqttConnect connect, final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            final MqttClientData clientData, final MqttDecoder decoder,
            final Lazy<MqttSubscriptionHandler> subscriptionHandler,
            final Lazy<MqttIncomingQosHandler> incomingQosHandler,
            final Lazy<MqttOutgoingQosHandler> outgoingQosHandler,
            final MqttDisconnectOnConnAckHandler disconnectOnConnAckHandler) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
        this.decoder = decoder;
        this.subscriptionHandler = subscriptionHandler;
        this.incomingQosHandler = incomingQosHandler;
        this.outgoingQosHandler = outgoingQosHandler;
        this.disconnectOnConnAckHandler = disconnectOnConnAckHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        if (!connectCalled) {
            connectCalled = true;
            writeConnect(ctx);
        }
        ctx.fireChannelActive();
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
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
    private void writeConnect(@NotNull final ChannelHandlerContext ctx) {
        addClientData(ctx.channel());

        final MqttMessage message = (connect.getRawEnhancedAuthProvider() == null) ?
                connect.createStateful(clientData.getRawClientIdentifier(), null) : connect;

        ctx.writeAndFlush(message).addListener(this);
    }

    @Override
    public void operationComplete(final ChannelFuture future) {
        final Channel channel = future.channel();
        if (future.isSuccess()) {
            if (connect.getRawEnhancedAuthProvider() == null) {
                scheduleTimeout(channel);
            }
            channel.pipeline().addAfter(MqttEncoder.NAME, MqttDecoder.NAME, decoder);
        } else {
            MqttDisconnectUtil.close(channel, future.cause());
        }
    }

    /**
     * Adds the {@link MqttClientData} and the {@link MqttClientConnectionData} to the channel.
     *
     * @param channel the channel to add the client data to.
     */
    private void addClientData(@NotNull final Channel channel) {
        final MqttConnectRestrictions restrictions = connect.getRestrictions();
        final MqttClientConnectionData clientConnectionData =
                new MqttClientConnectionData(connect.getKeepAlive(), connect.getSessionExpiryInterval(),
                        restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
                        restrictions.getMaximumPacketSize(), connect.getRawEnhancedAuthProvider(),
                        connect.getRawWillPublish() != null, connect.isProblemInformationRequested(),
                        connect.isResponseInformationRequested(), channel);

        clientData.setClientConnectionData(clientConnectionData);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        cancelTimeout();

        if (msg instanceof MqttConnAck) {
            handleConnAck((MqttConnAck) msg, ctx.channel());
        } else {
            handleOtherThanConnAck(msg, ctx.channel());
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
    private void handleConnAck(@NotNull final MqttConnAck connAck, @NotNull final Channel channel) {
        if (connAck.getReasonCode().isError()) {
            MqttDisconnectUtil.close(
                    channel, new Mqtt5MessageException(connAck, "Connection failed with CONNACK with Error Code"));
        } else {
            if (validateConnack(connAck, channel)) {
                updateClientData(connAck);
                addServerData(connAck);

                final ChannelPipeline pipeline = channel.pipeline();

                pipeline.remove(this);
                String beforeHandlerName = MqttDecoder.NAME;

                final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
                assert clientConnectionData != null;
                final int keepAlive = clientConnectionData.getKeepAlive();
                if (keepAlive > 0) {
                    pipeline.addAfter(beforeHandlerName, MqttPingHandler.NAME, new MqttPingHandler(keepAlive));
                    beforeHandlerName = MqttPingHandler.NAME;
                }

                pipeline.addAfter(beforeHandlerName, MqttSubscriptionHandler.NAME, subscriptionHandler.get());
                pipeline.addAfter(beforeHandlerName, MqttIncomingQosHandler.NAME, incomingQosHandler.get());
                pipeline.addAfter(beforeHandlerName, MqttOutgoingQosHandler.NAME, outgoingQosHandler.get());
                pipeline.addLast(MqttDisconnectOnConnAckHandler.NAME, disconnectOnConnAckHandler);

                connAckEmitter.onSuccess(connAck);
            }
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
    private void handleOtherThanConnAck(@NotNull final Object msg, @NotNull final Channel channel) {
        if (msg instanceof Mqtt5Message) {
            final Mqtt5Message mqttMessage = (Mqtt5Message) msg;
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
    private boolean validateConnack(@NotNull final MqttConnAck connAck, @NotNull final Channel channel) {
        final MqttClientIdentifierImpl clientIdentifier = clientData.getRawClientIdentifier();
        final MqttClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();

        if (clientIdentifier == MqttClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) {
            if (assignedClientIdentifier == null) {
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
        return true;
    }

    /**
     * Updates the {@link MqttClientConnectionData} with data of the given CONNACK message.
     *
     * @param connAck the CONNACK message.
     */
    private void updateClientData(@NotNull final MqttConnAck connAck) {
        final MqttClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();
        if (assignedClientIdentifier != null) {
            clientData.setClientIdentifier(assignedClientIdentifier);
        }

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        final int serverKeepAlive = connAck.getRawServerKeepAlive();
        if (serverKeepAlive != MqttConnAck.KEEP_ALIVE_FROM_CONNECT) {
            clientConnectionData.setKeepAlive(serverKeepAlive);
        }

        final long sessionExpiryInterval = connAck.getRawSessionExpiryInterval();
        if (sessionExpiryInterval != MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            clientConnectionData.setSessionExpiryInterval(sessionExpiryInterval);
        }
    }

    /**
     * Adds the {@link MqttServerConnectionData} to the channel.
     *
     * @param connAck the CONNACK message.
     */
    private void addServerData(@NotNull final MqttConnAck connAck) {
        final MqttConnAckRestrictions restrictions = connAck.getRestrictions();

        clientData.setServerConnectionData(
                new MqttServerConnectionData(restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
                        restrictions.getMaximumPacketSize(), restrictions.getMaximumQos(),
                        restrictions.isRetainAvailable(), restrictions.isWildcardSubscriptionAvailable(),
                        restrictions.isSubscriptionIdentifierAvailable(),
                        restrictions.isSharedSubscriptionAvailable()));
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof ChannelCloseEvent) {
            connAckEmitter.onError(((ChannelCloseEvent) evt).getCause());
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    protected long getTimeout() {
        return CONNACK_TIMEOUT;
    }

    @NotNull
    @Override
    protected Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.PROTOCOL_ERROR;
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for CONNACK";
    }

}

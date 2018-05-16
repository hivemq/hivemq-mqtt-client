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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
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
import org.mqttbee.mqtt.handler.publish.MqttIncomingQoSHandler;
import org.mqttbee.mqtt.handler.publish.MqttOutgoingQoSHandler;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt.handler.util.ChannelInboundHandlerWithTimeout;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckRestrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@ChannelScope
public class MqttConnectHandler extends ChannelInboundHandlerWithTimeout {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConnectHandler.class);

    public static final String NAME = "connect";
    private static final int CONNACK_TIMEOUT = 60; // TODO configurable

    private final MqttConnect connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final MqttClientData clientData;

    public MqttConnectHandler(
            @NotNull final MqttConnect connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientData clientData) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        addClientData(ctx.channel());
        writeConnect(ctx);
        ctx.fireChannelActive();
    }

    /**
     * Adds the {@link MqttClientData} and the {@link MqttClientConnectionData} to the channel.
     *
     * @param channel the channel to add the client data to.
     */
    private void addClientData(@NotNull final Channel channel) {
        final MqttConnectRestrictions restrictions = connect.getRestrictions();

        clientData.setClientConnectionData(
                new MqttClientConnectionData(connect.getKeepAlive(), connect.getSessionExpiryInterval(),
                        restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
                        restrictions.getMaximumPacketSize(), connect.getRawEnhancedAuthProvider(),
                        connect.getRawWillPublish() != null, connect.isProblemInformationRequested(),
                        connect.isResponseInformationRequested(), channel));

        clientData.to(channel);
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
        ctx.writeAndFlush(connect).addListener(future -> {
            if (future.isSuccess()) {
                final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
                assert clientConnectionData != null;
                if (clientConnectionData.getEnhancedAuthProvider() == null) {
                    scheduleTimeout();
                }

                ctx.pipeline().addFirst(MqttDecoder.NAME, ChannelComponent.get(ctx.channel()).decoder());
            } else {
                MqttDisconnectUtil.close(ctx.channel(), future.cause());
            }
        });
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
                final ChannelComponent channelComponent = ChannelComponent.get(channel);

                pipeline.remove(this);

                final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
                assert clientConnectionData != null;
                final int keepAlive = clientConnectionData.getKeepAlive();
                if (keepAlive > 0) {
                    pipeline.addAfter(MqttEncoder.NAME, MqttPingHandler.NAME, new MqttPingHandler(keepAlive));
                }

                pipeline.addAfter(
                        MqttPingHandler.NAME, MqttSubscriptionHandler.NAME, channelComponent.subscriptionHandler());
                pipeline.addAfter(
                        MqttPingHandler.NAME, MqttIncomingQoSHandler.NAME, channelComponent.incomingQoSHandler());
                pipeline.addAfter(
                        MqttPingHandler.NAME, MqttOutgoingQoSHandler.NAME, channelComponent.outgoingQoSHandler());
                pipeline.addLast(MqttDisconnectOnConnAckHandler.NAME, channelComponent.disconnectOnConnAckHandler());

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
                        restrictions.getMaximumPacketSize(), restrictions.getMaximumQoS(),
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
    protected long getTimeout(@NotNull final ChannelHandlerContext ctx) {
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

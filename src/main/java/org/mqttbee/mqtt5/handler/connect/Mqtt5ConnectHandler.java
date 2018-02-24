package org.mqttbee.mqtt5.handler.connect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.MqttServerConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttDecoder;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl;
import org.mqttbee.mqtt5.handler.disconnect.ChannelCloseEvent;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt5.handler.ping.Mqtt5PingHandler;
import org.mqttbee.mqtt5.handler.util.ChannelInboundHandlerWithTimeout;
import org.mqttbee.mqtt5.ioc.ChannelComponent;
import org.mqttbee.mqtt5.ioc.ChannelScope;
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
public class Mqtt5ConnectHandler extends ChannelInboundHandlerWithTimeout {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mqtt5ConnectHandler.class);

    public static final String NAME = "connect";
    private static final int CONNACK_TIMEOUT = 60; // TODO configurable

    private final MqttConnectImpl connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final MqttClientDataImpl clientData;

    public Mqtt5ConnectHandler(
            @NotNull final MqttConnectImpl connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientDataImpl clientData) {

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
     * Adds the {@link MqttClientDataImpl} and the {@link MqttClientConnectionDataImpl} to the channel.
     *
     * @param channel the channel to add the client data to.
     */
    private void addClientData(@NotNull final Channel channel) {
        final MqttConnectImpl.RestrictionsImpl restrictions = connect.getRestrictions();

        clientData.setClientConnectionData(
                new MqttClientConnectionDataImpl(connect.getKeepAlive(), connect.getSessionExpiryInterval(),
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
                final MqttClientDataImpl clientData = MqttClientDataImpl.from(ctx.channel());
                if (clientData.getRawClientConnectionData().getEnhancedAuthProvider() != null) {
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

        if (msg instanceof MqttConnAckImpl) {
            handleConnAck((MqttConnAckImpl) msg, ctx.channel());
        } else {
            handleOtherThanConnAck(msg, ctx.channel());
        }
    }

    /**
     * Handles the given CONNACK message.
     * <p>
     * If it contains an Error Code, the channel is closed.
     * <p>
     * Otherwise it is validated. Then this handler is removed from the pipeline and the {@link Mqtt5PingHandler} and
     * {@link Mqtt5DisconnectOnConnAckHandler} are added to the pipeline.
     *
     * @param connAck the CONNACK message.
     * @param channel the channel.
     */
    private void handleConnAck(@NotNull final MqttConnAckImpl connAck, @NotNull final Channel channel) {
        if (connAck.getReasonCode().isError()) {
            MqttDisconnectUtil.close(
                    channel, new Mqtt5MessageException(connAck, "Connection failed with CONNACK with Error Code"));
        } else {
            if (validateConnack(connAck, channel)) {
                updateClientData(connAck);
                addServerData(connAck);

                final ChannelPipeline pipeline = channel.pipeline();
                pipeline.remove(this);
                final int keepAlive = clientData.getRawClientConnectionData().getKeepAlive();
                if (keepAlive > 0) {
                    pipeline.addLast(Mqtt5PingHandler.NAME, new Mqtt5PingHandler(keepAlive));
                }
                pipeline.addLast(Mqtt5DisconnectOnConnAckHandler.NAME,
                        ChannelComponent.get(channel).disconnectOnConnAckHandler());

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
    private boolean validateConnack(@NotNull final MqttConnAckImpl connAck, @NotNull final Channel channel) {
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
     * Updates the {@link MqttClientConnectionDataImpl} with data of the given CONNACK message.
     *
     * @param connAck the CONNACK message.
     */
    private void updateClientData(@NotNull final MqttConnAckImpl connAck) {
        final MqttClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();
        if (assignedClientIdentifier != null) {
            clientData.setClientIdentifier(assignedClientIdentifier);
        }

        final MqttClientConnectionDataImpl clientConnectionData = clientData.getRawClientConnectionData();

        final int serverKeepAlive = connAck.getRawServerKeepAlive();
        if (serverKeepAlive != MqttConnAckImpl.KEEP_ALIVE_FROM_CONNECT) {
            clientConnectionData.setKeepAlive(serverKeepAlive);
        }

        final long sessionExpiryInterval = connAck.getRawSessionExpiryInterval();
        if (sessionExpiryInterval != MqttConnAckImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            clientConnectionData.setSessionExpiryInterval(sessionExpiryInterval);
        }
    }

    /**
     * Adds the {@link MqttServerConnectionDataImpl} to the channel.
     *
     * @param connAck the CONNACK message.
     */
    private void addServerData(@NotNull final MqttConnAckImpl connAck) {
        final MqttConnAckImpl.RestrictionsImpl restrictions = connAck.getRestrictions();

        clientData.setServerConnectionData(
                new MqttServerConnectionDataImpl(restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
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

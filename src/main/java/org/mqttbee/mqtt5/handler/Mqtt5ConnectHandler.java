package org.mqttbee.mqtt5.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.exception.ChannelClosedException;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.api.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.*;
import org.mqttbee.mqtt5.codec.decoder.Mqtt5Decoder;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.connect.connack.Mqtt5ConnAckImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the connection to a MQTT Server.
 * <ul>
 *     <li>Writes the CONNECT message.</li>
 *     <li>Handles the CONNACK message.</li>
 *     <li>Disconnects or closes the channel on receiving other messages before CONNACK.</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
public class Mqtt5ConnectHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mqtt5ConnectHandler.class);

    public static final String NAME = "connect";

    private final Mqtt5ConnectImpl connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final Mqtt5ClientDataImpl clientData;

    Mqtt5ConnectHandler(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final Mqtt5ClientDataImpl clientData) {

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
     * Adds the {@link Mqtt5ClientDataImpl} and the {@link Mqtt5ClientConnectionDataImpl} to the channel.
     *
     * @param channel the channel to add the client data to.
     */
    private void addClientData(@NotNull final Channel channel) {
        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = connect.getRestrictions();

        clientData.setClientConnectionData(
                new Mqtt5ClientConnectionDataImpl(connect.getKeepAlive(), connect.getSessionExpiryInterval(),
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
                ctx.pipeline().addLast(Mqtt5Decoder.NAME, Mqtt5Component.INSTANCE.decoder());
            } else {
                closeChannel(ctx.channel(), future.cause());
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5ConnAckImpl) {
            handleConnAck((Mqtt5ConnAckImpl) msg, ctx.channel());
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
    private void handleConnAck(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (connAck.getReasonCode().isError()) {
            closeChannel(channel, new Mqtt5MessageException("Connection failed with CONNACK with Error Code", connAck));
        } else {
            if (validateConnack(connAck, channel)) {
                updateClientData(connAck);
                addServerData(connAck);

                pipeline.remove(this);
                final int keepAlive = clientData.getRawClientConnectionData().getKeepAlive();
                if (keepAlive > 0) {
                    pipeline.addLast(Mqtt5PingHandler.NAME, new Mqtt5PingHandler(keepAlive));
                }
                pipeline.addLast(
                        Mqtt5DisconnectOnConnAckHandler.NAME, Mqtt5Component.INSTANCE.disconnectOnConnAckHandler());

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
            final Mqtt5Message message = (Mqtt5Message) msg;
            final String errorMessage =
                    message.getClass().getSimpleName() + " message must not be received before CONNACK";
            disconnect(channel, new Mqtt5MessageException(errorMessage, message));
        } else {
            closeChannel(channel, new IllegalStateException("No data must be received before CONNECT is sent"));
        }
    }

    /**
     * Validates the given CONNACK message.
     * <p>
     * If validation fails disconnection and closing of the channel is already handled.
     *
     * @param connAck the CONNACK message.
     * @param channel the channel.
     * @return true if the CONNACK message is valid, otherwise false.
     */
    private boolean validateConnack(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final Channel channel) {
        final Mqtt5ClientIdentifier clientIdentifier = clientData.getRawClientIdentifier();
        final Mqtt5ClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();

        if (clientIdentifier == Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) {
            if (assignedClientIdentifier == null) {
                disconnect(channel, new Mqtt5MessageException("Server did not assign a Client Identifier", connAck));
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
     * Updates the {@link Mqtt5ClientConnectionDataImpl} with data of the given CONNACK message.
     *
     * @param connAck the CONNACK message.
     */
    private void updateClientData(@NotNull final Mqtt5ConnAckImpl connAck) {
        final Mqtt5ClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();
        if (assignedClientIdentifier != null) {
            clientData.setClientIdentifier(assignedClientIdentifier);
        }

        final Mqtt5ClientConnectionDataImpl clientConnectionData = clientData.getRawClientConnectionData();

        final int serverKeepAlive = connAck.getRawServerKeepAlive();
        if (serverKeepAlive != Mqtt5ConnAckImpl.KEEP_ALIVE_FROM_CONNECT) {
            clientConnectionData.setKeepAlive(serverKeepAlive);
        }

        final long sessionExpiryInterval = connAck.getRawSessionExpiryInterval();
        if (sessionExpiryInterval != Mqtt5ConnAckImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            clientConnectionData.setSessionExpiryInterval(sessionExpiryInterval);
        }
    }

    /**
     * Adds the {@link Mqtt5ServerConnectionDataImpl} to the channel.
     *
     * @param connAck the CONNACK message.
     */
    private void addServerData(@NotNull final Mqtt5ConnAckImpl connAck) {
        final Mqtt5ConnAckImpl.RestrictionsImpl restrictions = connAck.getRestrictions();

        clientData.setServerConnectionData(
                new Mqtt5ServerConnectionDataImpl(restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
                        restrictions.getMaximumPacketSize(), restrictions.getMaximumQoS(),
                        restrictions.isRetainAvailable(), restrictions.isWildcardSubscriptionAvailable(),
                        restrictions.isSubscriptionIdentifierAvailable(),
                        restrictions.isSharedSubscriptionAvailable()));
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        connAckEmitter.onError(new ChannelClosedException());
        ctx.fireChannelInactive();
    }

    /**
     * Closes the channel and notifies the {@link #connAckEmitter}.
     *
     * @param channel the channel to close.
     * @param cause   the cause for closing.
     */
    private void closeChannel(@NotNull final Channel channel, @NotNull final Throwable cause) {
        connAckEmitter.onError(cause);
        channel.pipeline().remove(this); // removed to not trigger channelInactive of this handler
        channel.close();
    }

    /**
     * Sends a DISCONNECT message, closes the channel and notifies the {@link #connAckEmitter}.
     *
     * @param channel the channel to disconnect.
     * @param cause   the cause for disconnecting.
     */
    private void disconnect(@NotNull final Channel channel, @NotNull final Throwable cause) {
        connAckEmitter.onError(cause);
        channel.pipeline().remove(this); // removed to not trigger channelInactive of this handler
        Mqtt5Util.disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, cause.getMessage(), channel);
    }

}

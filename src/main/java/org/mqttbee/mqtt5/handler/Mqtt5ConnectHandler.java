package org.mqttbee.mqtt5.handler;

import io.netty.channel.*;
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
 * @author Silvio Giebl
 */
public class Mqtt5ConnectHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mqtt5ConnectHandler.class);

    public static final String NAME = "connect";

    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final Mqtt5ClientDataImpl clientData;

    public Mqtt5ConnectHandler(
            @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter, @NotNull final Mqtt5ClientDataImpl clientData) {

        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof Mqtt5ConnectImpl) {
            handleConnect((Mqtt5ConnectImpl) msg, ctx, promise);
        } else {
            handleOtherThanConnect(promise);
        }
    }

    private void handleConnect(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final ChannelHandlerContext ctx,
            @NotNull final ChannelPromise promise) {

        addClientData(connect, ctx.channel());
        ctx.write(connect, promise);
        promise.addListener(future -> ctx.pipeline().addLast(Mqtt5Decoder.NAME, Mqtt5Component.INSTANCE.decoder()));
    }

    private void handleOtherThanConnect(final ChannelPromise promise) { // TODO
        final IllegalStateException illegalStateException = new IllegalStateException();
        connAckEmitter.onError(illegalStateException);
        promise.setFailure(illegalStateException);
    }

    private void addClientData(@NotNull final Mqtt5ConnectImpl connect, @NotNull final Channel channel) {
        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = connect.getRestrictions();

        clientData.setClientConnectionData(
                new Mqtt5ClientConnectionDataImpl(connect.getKeepAlive(), connect.getSessionExpiryInterval(),
                        restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
                        restrictions.getMaximumPacketSize(), connect.getRawExtendedAuthProvider(),
                        connect.getRawWillPublish() != null, connect.isProblemInformationRequested(),
                        connect.isResponseInformationRequested(), channel));

        clientData.to(channel);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5ConnAckImpl) {
            handleConnAck((Mqtt5ConnAckImpl) msg, ctx.channel());
        } else {
            handleOtherThanConnAck(msg, ctx.channel());
        }
    }

    private void handleConnAck(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (connAck.getReasonCode().isError()) {
            connAckEmitter.onError(
                    new Mqtt5MessageException("Connection failed with CONNACK with Error Code", connAck));
            pipeline.remove(this); // removed to not trigger channelInactive of this handler
            channel.close();
        } else {
            if (validateConnack(connAck)) {
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

    private void handleOtherThanConnAck(@NotNull final Object msg, @NotNull final Channel channel) {
        final String errorMessage;
        if (msg instanceof Mqtt5Message) {
            final Mqtt5Message message = (Mqtt5Message) msg;
            errorMessage = message.getClass().getSimpleName() + " message must not be received before CONNACK";
            connAckEmitter.onError(new Mqtt5MessageException(errorMessage, message));
        } else {
            errorMessage = "No data must be received before CONNECT is sent";
            connAckEmitter.onError(new IllegalStateException(errorMessage));
        }
        channel.pipeline().remove(this); // removed to not trigger channelInactive of this handler
        Mqtt5Util.disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, errorMessage, channel);
    }

    private boolean validateConnack(@NotNull final Mqtt5ConnAckImpl connAck) {
        final Mqtt5ClientIdentifier clientIdentifier = clientData.getRawClientIdentifier();
        final Mqtt5ClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();

        if (clientIdentifier == Mqtt5ClientIdentifierImpl.REQUEST_CLIENT_IDENTIFIER_FROM_SERVER) {
            if (assignedClientIdentifier == null) {
                connAckEmitter.onError(new Mqtt5MessageException("Server did not assign a Client Identifier", connAck));
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

}

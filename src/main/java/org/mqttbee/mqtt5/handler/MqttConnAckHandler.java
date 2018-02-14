package org.mqttbee.mqtt5.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5ServerDataImpl;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifierImpl;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof Mqtt5ConnAckImpl) {
            final Mqtt5ConnAckImpl connAck = (Mqtt5ConnAckImpl) msg;

            if (connAck.getReasonCode() == Mqtt5ConnAckReasonCode.SUCCESS) {
                handleSuccessfulConnAck(connAck, ctx.channel());
            }
        }
        super.channelRead(ctx, msg);
    }

    private void handleSuccessfulConnAck(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final Channel channel) {
        updateClientData(connAck, channel);
        addServerData(connAck, channel);
    }

    private void updateClientData(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final Channel channel) {
        final Mqtt5ClientDataImpl clientData = Mqtt5ClientDataImpl.get(channel);

        final Mqtt5ClientIdentifierImpl assignedClientIdentifier = connAck.getRawAssignedClientIdentifier();
        if (assignedClientIdentifier == null) {
            // TODO server MUST return the assigned client identifier or return an error reason code (0x85)
            return;
        }
        clientData.setClientIdentifier(assignedClientIdentifier);

        final int serverKeepAlive = connAck.getRawServerKeepAlive();
        if (serverKeepAlive != Mqtt5ConnAckImpl.KEEP_ALIVE_FROM_CONNECT) {
            clientData.setKeepAlive(serverKeepAlive);
        }

        final long sessionExpiryInterval = connAck.getRawSessionExpiryInterval();
        if (sessionExpiryInterval != Mqtt5ConnAckImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            clientData.setSessionExpiryInterval(sessionExpiryInterval);
        }
    }

    private void addServerData(@NotNull final Mqtt5ConnAckImpl connAck, @NotNull final Channel channel) {
        final Mqtt5ConnAckImpl.RestrictionsImpl restrictions = connAck.getRestrictions();

        final Mqtt5ServerDataImpl serverData =
                new Mqtt5ServerDataImpl(restrictions.getReceiveMaximum(), restrictions.getTopicAliasMaximum(),
                        restrictions.getMaximumPacketSize(), restrictions.getMaximumQoS(),
                        restrictions.isRetainAvailable(), restrictions.isWildcardSubscriptionAvailable(),
                        restrictions.isSubscriptionIdentifierAvailable(), restrictions.isSharedSubscriptionAvailable());

        serverData.set(channel);
    }

}

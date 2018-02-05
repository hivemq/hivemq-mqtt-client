package org.mqttbee.mqtt5.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
            throws Exception {

        if (msg instanceof Mqtt5ConnectImpl) {
            final Mqtt5ConnectImpl connect = (Mqtt5ConnectImpl) msg;

            addClientData(connect, ctx.channel());
        }
        super.write(ctx, msg, promise);
    }

    private void addClientData(@NotNull final Mqtt5ConnectImpl connect, @NotNull final Channel channel) {
        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = connect.getRestrictions();
        final Mqtt5ExtendedAuthImpl extendedAuth = connect.getRawExtendedAuth();
        final Mqtt5UTF8String authMethod = (extendedAuth == null) ? null : extendedAuth.getMethod();

        new Mqtt5ClientData(connect.getRawClientIdentifier(), connect.getKeepAlive(),
                connect.getSessionExpiryInterval(), restrictions.getReceiveMaximum(),
                restrictions.getTopicAliasMaximum(), restrictions.getMaximumPacketSize(), authMethod,
                connect.getRawWillPublish() != null, connect.isProblemInformationRequested(), channel);
    }

}

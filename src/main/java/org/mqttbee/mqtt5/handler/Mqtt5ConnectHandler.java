package org.mqttbee.mqtt5.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectWrapper;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
            throws Exception {

        if (msg instanceof Mqtt5ConnectWrapper) {
            final Mqtt5ConnectWrapper connectWrapper = (Mqtt5ConnectWrapper) msg;

            addClientData(connectWrapper, ctx.channel());
        }
        super.write(ctx, msg, promise);
    }

    private void addClientData(@NotNull final Mqtt5ConnectWrapper connectWrapper, @NotNull final Channel channel) {
        final Mqtt5ConnectImpl connect = connectWrapper.getWrapped();

        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = connect.getRestrictions();
        final Mqtt5ExtendedAuthImpl extendedAuth = connectWrapper.getExtendedAuth();
        final Mqtt5UTF8String authMethod = (extendedAuth == null) ? null : extendedAuth.getMethod();

        new Mqtt5ClientDataImpl(connectWrapper.getClientIdentifier(), connect.getKeepAlive(),
                connect.getSessionExpiryInterval(), restrictions.getReceiveMaximum(),
                restrictions.getTopicAliasMaximum(), restrictions.getMaximumPacketSize(), authMethod,
                connect.getRawWillPublish() != null, connect.isProblemInformationRequested(), channel);
    }

}

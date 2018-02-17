package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class Mqtt5DisconnectHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "disconnect";

    @Inject
    Mqtt5DisconnectHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof Mqtt5DisconnectImpl) {
            readDisconnect(ctx, (Mqtt5DisconnectImpl) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void readDisconnect(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5DisconnectImpl disconnect) {

        ctx.pipeline().remove(this);
        Mqtt5DisconnectUtil.close(ctx.channel(), new Mqtt5MessageException(disconnect, "Server sent DISCONNECT"));
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        Mqtt5DisconnectUtil.close(ctx.channel(), "Server closed channel without DISCONNECT");
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof ChannelCloseEvent) {
            ctx.pipeline().remove(this);
        }
        ctx.fireUserEventTriggered(evt);
    }

}

package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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

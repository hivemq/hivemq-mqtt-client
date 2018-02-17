package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class Mqtt5DisconnectOnAuthHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "disconnect.on.auth";

    @Inject
    Mqtt5DisconnectOnAuthHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5AuthImpl) {
            Mqtt5DisconnectUtil.disconnect(
                    ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "Server must not send AUTH");
        }
        ctx.fireChannelRead(msg);
    }

}

package org.mqttbee.mqtt5.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;
import org.mqttbee.mqtt5.message.connect.connack.Mqtt5ConnAckImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class Mqtt5DisconnectOnConnAckHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "disconnect.on.connack";

    @Inject
    Mqtt5DisconnectOnConnAckHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5ConnAckImpl) {
            Mqtt5DisconnectUtil.disconnect(
                    ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "Must not receive second CONNACK");
        }
        ctx.fireChannelRead(msg);
    }

}

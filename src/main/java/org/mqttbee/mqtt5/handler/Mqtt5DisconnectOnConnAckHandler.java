package org.mqttbee.mqtt5.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.Mqtt5Util;
import org.mqttbee.mqtt5.message.connect.connack.Mqtt5ConnAckImpl;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
public class Mqtt5DisconnectOnConnAckHandler extends ChannelInboundHandlerAdapter {

    static final Mqtt5DisconnectOnConnAckHandler INSTANCE = new Mqtt5DisconnectOnConnAckHandler();

    private Mqtt5DisconnectOnConnAckHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof Mqtt5ConnAckImpl) {
            Mqtt5Util.disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "Must not receive second CONNACK", ctx.channel());
            // TODO notify API
        }
        super.channelRead(ctx, msg);
    }

}

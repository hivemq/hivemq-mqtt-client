package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.publish.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.publish.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5OutgoingQoSHandler extends ChannelDuplexHandler {

    private final Ranges packetIdentifiers = new Ranges(UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof Mqtt5PublishImpl) {
            handlePublish(ctx, (Mqtt5PublishImpl) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void handlePublish(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5PublishImpl publish,
            @NotNull final ChannelPromise promise) {


    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Mqtt5PubAckImpl) {
            handlePubAck(ctx, (Mqtt5PubAckImpl) msg);
        } else if (msg instanceof Mqtt5PubRecImpl) {
            handlePubRec(ctx, (Mqtt5PubRecImpl) msg);
        } else if (msg instanceof Mqtt5PubCompImpl) {
            handlePubComp(ctx, (Mqtt5PubCompImpl) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePubAck(final ChannelHandlerContext ctx, final Mqtt5PubAckImpl pubAck) {

    }

    private void handlePubRec(final ChannelHandlerContext ctx, final Mqtt5PubRecImpl pubRec) {

    }

    private void handlePubComp(final ChannelHandlerContext ctx, final Mqtt5PubCompImpl pubComp) {

    }

}

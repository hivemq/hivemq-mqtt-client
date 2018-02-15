package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class Mqtt5Encoder extends MessageToByteEncoder<Mqtt5Message> {

    @Inject
    Mqtt5Encoder() {
        super(Mqtt5Message.class, true);
    }

    @Override
    protected ByteBuf allocateBuffer(
            final ChannelHandlerContext ctx, final Mqtt5Message message, final boolean preferDirect) {

        return message.getEncoder().allocateBuffer(ctx.channel());
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Mqtt5Message message, final ByteBuf out) {
        message.getEncoder().encode(ctx.channel(), out);
    }

}

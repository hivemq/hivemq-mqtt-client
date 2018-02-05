package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
public class Mqtt5Encoder extends MessageToByteEncoder<Mqtt5Message> {

    @Inject
    Mqtt5Encoder() {
        super(Mqtt5Message.class, true);
    }

    @Override
    protected ByteBuf allocateBuffer(
            final ChannelHandlerContext ctx, final Mqtt5Message message, final boolean preferDirect) throws Exception {

        return message.allocateBuffer(ctx.channel());
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Mqtt5Message message, final ByteBuf out)
            throws Exception {

        message.encode(ctx.channel(), out);
    }

}

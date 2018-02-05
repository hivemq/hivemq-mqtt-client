package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5MaximumPacketSizeExceededException;
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

//        final Integer test = ctx.channel().attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).get();
//        final int maximumPacketSize = (test == null) ? Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT : test;
        final int maximumPacketSize = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT;
        final int encodedLength = message.encodedLength(maximumPacketSize); // TODO

        if (encodedLength < 0) {
            throw new Mqtt5MaximumPacketSizeExceededException(message, maximumPacketSize);
        }

        return ctx.alloc().ioBuffer(encodedLength, encodedLength);
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Mqtt5Message message, final ByteBuf out)
            throws Exception {

        message.encode(ctx.channel(), out);
    }

}

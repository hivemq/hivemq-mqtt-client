package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.exceptions.Mqtt5MaximumPacketSizeExceededException;
import org.mqttbee.mqtt5.message.Mqtt5Message;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 * @author Daniel Krüger
 */
@ChannelHandler.Sharable
public class Mqtt3Encoder extends MessageToByteEncoder<Mqtt3Message> {

    @Inject
    Mqtt3Encoder() {
        super(Mqtt3Message.class, true);
    }

    @Override
    protected ByteBuf allocateBuffer(
            final ChannelHandlerContext ctx, final Mqtt3Message message, final boolean preferDirect) throws Exception {

        final int encodedLength = message.encodedLength();

        final Integer maximumPacketSize = ctx.channel().attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).get();
        if ((maximumPacketSize != null) && (encodedLength > maximumPacketSize)) {
            throw new Mqtt5MaximumPacketSizeExceededException(); //TODO Wird das eine allgemeine Exception?
        }

        return ctx.alloc().ioBuffer(encodedLength, encodedLength);
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Mqtt3Message message, final ByteBuf out)
            throws Exception {

        message.encode(ctx.channel(), out);
    }

}

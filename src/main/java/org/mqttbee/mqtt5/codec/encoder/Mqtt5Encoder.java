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

    private final Mqtt5MessageEncoders encoders;

    @Inject
    public Mqtt5Encoder(final Mqtt5MessageEncoders encoders) {
        super(Mqtt5Message.class, true);
        this.encoders = encoders;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Mqtt5Message message, final ByteBuf out)
            throws Exception {
        message.encode(encoders, ctx.channel(), out);
    }

}

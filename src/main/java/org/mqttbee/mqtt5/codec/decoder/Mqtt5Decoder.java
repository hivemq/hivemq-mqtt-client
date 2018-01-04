package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5Message;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Decoder extends ByteToMessageDecoder {

    private final Mqtt5MessageDecoders decoders;

    @Inject
    public Mqtt5Decoder(final Mqtt5MessageDecoders decoders) {
        this.decoders = decoders;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (in.readableBytes() < 2) {
            return;
        }
        in.markReaderIndex();
        final int readerIndexBeforeFixedHeader = in.readerIndex();

        final byte fixedHeader = in.readByte();
        final int remainingLength = Mqtt5DataTypes.decodeVariableByteInteger(in);

        if (remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES) {
            in.resetReaderIndex();
            return;
        }

        if (remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_TOO_LARGE ||
                remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return;
        }

        final int readerIndexAfterFixedHeader = in.readerIndex();
        final int fixedHeaderLength = readerIndexAfterFixedHeader - readerIndexBeforeFixedHeader;
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = ctx.channel().attr(ChannelAttributes.MAXIMUM_INCOMING_PACKET_SIZE_KEY).get();

        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: send Disconnect with reason code 0x95 Packet too large and close channel
            in.clear();
            return;
        }

        if (in.readableBytes() < remainingLength) {
            in.resetReaderIndex();
            return;
        }

        final int messageType = fixedHeader >> 4;
        final int flags = fixedHeader & 0xF;
        final ByteBuf messageBuffer = in.readSlice(remainingLength);
        in.markReaderIndex();

        final Mqtt5MessageDecoder decoder = decoders.get(messageType);
        if (decoder == null) {
            // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
            in.clear();
            return;
        }

        final Mqtt5Message message = decoder.decode(flags, ctx.channel(), messageBuffer);

        if (message != null) {
            out.add(message);
        }
    }

}

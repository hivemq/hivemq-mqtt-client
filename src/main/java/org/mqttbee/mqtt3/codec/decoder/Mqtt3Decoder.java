package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;

import javax.inject.Inject;
import java.util.List;


/**
 * @author Daniel Krüger
 */
public class Mqtt3Decoder extends ByteToMessageDecoder {

    private static final int MIN_FIXED_HEADER_LENGTH = 2;

    private final Mqtt3MessageDecoders decoders;

    @Inject
    Mqtt3Decoder(final Mqtt3MessageDecoders decoders) {
        this.decoders = decoders;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        final Channel channel = ctx.channel();
        if (!channel.isOpen()) {
            return;
        }

        if (in.readableBytes() < MIN_FIXED_HEADER_LENGTH) {
            return;
        }

        in.markReaderIndex();

        final int readerIndexBeforeFixedHeader = in.readerIndex();

        final short fixedHeader = in.readUnsignedByte();
        final int remainingLength = Mqtt5DataTypes.decodeVariableByteInteger(in);

        if (remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES) {
            in.resetReaderIndex();
            return;
        }

        if (remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_TOO_LARGE ||
                remainingLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES) {

            //TODO
            //disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed remaining length", channel);
            return;
        }

        final int readerIndexAfterFixedHeader = in.readerIndex();
        final int fixedHeaderLength = readerIndexAfterFixedHeader - readerIndexBeforeFixedHeader;
        final int packetSize = fixedHeaderLength + remainingLength;

        final Integer maximumPacketSize = channel.attr(ChannelAttributes.INCOMING_MAXIMUM_PACKET_SIZE).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            //TODO
            // disconnect(Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE, null, channel);
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

        final Mqtt3MessageDecoder decoder = decoders.get(messageType);
        if (decoder == null) {
            //TODO
            //disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "wrong packet", channel);
            return;
        }

        final Mqtt3Message message = decoder.decode(flags, channel, messageBuffer);

        if (message != null) {
            out.add(message);
        }
    }

}

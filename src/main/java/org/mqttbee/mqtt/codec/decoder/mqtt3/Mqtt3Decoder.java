package org.mqttbee.mqtt.codec.decoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.ioc.ChannelScope;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@ChannelScope
public class Mqtt3Decoder extends ByteToMessageDecoder {

    private static final int MIN_FIXED_HEADER_LENGTH = 2;

    private final MqttMessageDecoders decoders;

    @Inject
    Mqtt3Decoder(final MqttMessageDecoders decoders) {
        this.decoders = decoders;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
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
        final int remainingLength = MqttVariableByteInteger.decode(in);

        if (remainingLength == MqttVariableByteInteger.NOT_ENOUGH_BYTES) {
            in.resetReaderIndex();
            return;
        }

        if (remainingLength == MqttVariableByteInteger.TOO_LARGE ||
                remainingLength == MqttVariableByteInteger.NOT_MINIMUM_BYTES) {

            channel.close(); // TODO
            return;
        }

        final int fixedHeaderLength = in.readerIndex() - readerIndexBeforeFixedHeader;
        final int packetSize = fixedHeaderLength + remainingLength;

        final Mqtt5ClientConnectionDataImpl clientConnectionData =
                Mqtt5ClientDataImpl.from(channel).getRawClientConnectionData();
        if (packetSize > clientConnectionData.getMaximumPacketSize()) {
            channel.close(); // TODO
            return;
        }

        if (in.readableBytes() < remainingLength) {
            in.resetReaderIndex();
            return;
        }

        final int messageType = fixedHeader >> 4;
        final int flags = fixedHeader & 0xF;
        final ByteBuf messageBuffer = in.readSlice(remainingLength);

        final MqttMessageDecoder decoder = decoders.get(messageType);
        if (decoder == null) {
            channel.close(); // TODO
            return;
        }

        final MqttMessage message = decoder.decode(flags, messageBuffer, clientConnectionData);

        if (message != null) {
            out.add(message);
        }
    }

}

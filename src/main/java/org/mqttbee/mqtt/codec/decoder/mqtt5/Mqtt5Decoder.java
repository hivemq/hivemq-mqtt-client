package org.mqttbee.mqtt.codec.decoder.mqtt5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;

import javax.inject.Inject;
import java.util.List;

import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.disconnect;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Decoder extends ByteToMessageDecoder {

    public static final String NAME = "decoder.mqtt5";

    private static final int MIN_FIXED_HEADER_LENGTH = 2;

    private final MqttMessageDecoders decoders;

    @Inject
    Mqtt5Decoder(final MqttMessageDecoders decoders) {
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

            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed remaining length", channel);
            return;
        }

        final int readerIndexAfterFixedHeader = in.readerIndex();
        final int fixedHeaderLength = readerIndexAfterFixedHeader - readerIndexBeforeFixedHeader;
        final int packetSize = fixedHeaderLength + remainingLength;

        final Mqtt5ClientConnectionDataImpl clientConnectionData =
                Mqtt5ClientDataImpl.from(channel).getRawClientConnectionData();
        if (packetSize > clientConnectionData.getMaximumPacketSize()) {
            disconnect(Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE, "incoming packet exceeded maximum packet size",
                    channel);
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

        final MqttMessageDecoder decoder = decoders.get(messageType);
        if (decoder == null) {
            disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "wrong packet", channel);
            return;
        }

        final MqttMessage message = decoder.decode(flags, messageBuffer, clientConnectionData);

        if (message != null) {
            out.add(message);
        }
    }

}

package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt5.ioc.ChannelScope;

import javax.inject.Inject;
import java.util.List;

import static org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil.disconnect;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttDecoder extends ByteToMessageDecoder {

    public static final String NAME = "decoder";

    private static final int MIN_FIXED_HEADER_LENGTH = 2;

    private final MqttMessageDecoders decoders;

    @Inject
    MqttDecoder(final MqttMessageDecoders decoders) {
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

            disconnect(channel, Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed remaining length");
            return;
        }

        final int fixedHeaderLength = in.readerIndex() - readerIndexBeforeFixedHeader;
        final int packetSize = fixedHeaderLength + remainingLength;

        final MqttClientConnectionDataImpl clientConnectionData =
                MqttClientDataImpl.from(channel).getRawClientConnectionData();
        if (packetSize > clientConnectionData.getMaximumPacketSize()) {
            disconnect(channel, Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE,
                    "incoming packet exceeded maximum packet size");
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
            disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "wrong packet");
            return;
        }

        final MqttMessage message = decoder.decode(flags, messageBuffer, clientConnectionData);

        if (message != null) {
            out.add(message);
        }
    }

}

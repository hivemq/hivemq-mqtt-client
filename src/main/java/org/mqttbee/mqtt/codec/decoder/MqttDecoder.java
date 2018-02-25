package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt5.ioc.ChannelScope;

import javax.inject.Inject;
import java.util.List;

/**
 * Main decoder for MQTT messages which delegates to the individual {@link MqttMessageDecoder}s when the fixed header
 * has been read and validated.
 *
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
        if (in.readableBytes() < MIN_FIXED_HEADER_LENGTH) {
            return;
        }
        final int readerIndexBeforeFixedHeader = in.readerIndex();

        final short fixedHeader = in.readUnsignedByte();
        final int messageType = fixedHeader >> 4;
        final int flags = fixedHeader & 0xF;
        final int remainingLength = MqttVariableByteInteger.decode(in);

        try {
            if (remainingLength == MqttVariableByteInteger.NOT_ENOUGH_BYTES) {
                in.readerIndex(readerIndexBeforeFixedHeader);
                return;
            }

            if (remainingLength < MqttVariableByteInteger.NOT_ENOUGH_BYTES) {
                throw new MqttDecoderException(
                        Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "malformed remaining length");
            }

            final int fixedHeaderLength = in.readerIndex() - readerIndexBeforeFixedHeader;
            final int packetSize = fixedHeaderLength + remainingLength;

            final MqttClientConnectionDataImpl clientConnectionData =
                    MqttClientDataImpl.from(ctx.channel()).getRawClientConnectionData();
            if (packetSize > clientConnectionData.getMaximumPacketSize()) {
                throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE,
                        "incoming packet exceeded maximum packet size");
            }

            if (in.readableBytes() < remainingLength) {
                in.readerIndex(readerIndexBeforeFixedHeader);
                return;
            }

            final MqttMessageDecoder decoder = decoders.get(messageType);
            if (decoder == null) {
                throw new MqttDecoderException(
                        Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must not receive this packet type");
            }

            final MqttMessage message = decoder.decode(flags, in.readSlice(remainingLength), clientConnectionData);

            if (message != null) {
                out.add(message);
            }

        } catch (final MqttDecoderException e) {
            in.clear();
            e.setMessageType(Mqtt5MessageType.fromCode(messageType));
            MqttDisconnectUtil.disconnect(ctx.channel(), e.getReasonCode(), e);
        }
    }

}

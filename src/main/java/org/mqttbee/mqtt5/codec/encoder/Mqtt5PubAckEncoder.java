package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckEncoder implements Mqtt5MessageEncoder<Mqtt5PubAckImpl> {

    public static final Mqtt5PubAckEncoder INSTANCE = new Mqtt5PubAckEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5PubAckImpl pubAck, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(channel, pubAck, out);
        encodeVariableHeader(channel, pubAck, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubAckImpl pubAck) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        if ((pubAck.encodedPropertyLength() != 0) || (pubAck.getReasonCode() != DEFAULT_REASON_CODE)) {
            remainingLength += 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubAckImpl pubAck) {
        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(pubAck.getRawReasonString());
        propertyLength += pubAck.getUserProperties().encodedLength();

        return propertyLength;
    }

    private void encodeFixedHeader(
            final Channel channel, final Mqtt5PubAckImpl pubAck, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
//        final Integer test = channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).get();
//        final int maximumPacketSize = (test == null) ? Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT : test;
        final int maximumPacketSize = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT;
        Mqtt5DataTypes.encodeVariableByteInteger(pubAck.encodedRemainingLength(maximumPacketSize), out); // TODO
    }

    private void encodeVariableHeader(
            final Channel channel, @NotNull final Mqtt5PubAckImpl pubAck, @NotNull final ByteBuf out) {
        out.writeShort(pubAck.getPacketIdentifier());

        final Mqtt5PubAckReasonCode reasonCode = pubAck.getReasonCode();
//        final Integer test = channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).get();
//        final int maximumPacketSize = (test == null) ? Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT : test;
        final int maximumPacketSize = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT;
        final int propertyLength = pubAck.encodedPropertyLength(maximumPacketSize); // TODO
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(channel, pubAck, propertyLength, out);
        }
    }

    private void encodeProperties(
            final Channel channel, @NotNull final Mqtt5PubAckImpl pubAck, final int propertyLength,
            @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

//        final Integer test = channel.attr(ChannelAttributes.OUTGOING_MAXIMUM_PACKET_SIZE).get();
//        final int maximumPacketSize = (test == null) ? Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT : test;
        final int maximumPacketSize = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT;
        pubAck.encodeReasonString(maximumPacketSize, out); // TODO
        pubAck.encodeUserProperties(maximumPacketSize, out); // TODO
    }

}

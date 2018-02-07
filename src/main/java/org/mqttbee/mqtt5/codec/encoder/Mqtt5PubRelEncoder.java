package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelEncoder implements Mqtt5MessageEncoder<Mqtt5PubRelImpl> {

    public static final Mqtt5PubRelEncoder INSTANCE = new Mqtt5PubRelEncoder();

    private static final int FIXED_HEADER = (Mqtt5MessageType.PUBREL.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5PubRelImpl pubRel, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

        encodeFixedHeader(pubRel, out, maximumPacketSize);
        encodeVariableHeader(pubRel, out, maximumPacketSize);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubRelImpl pubRel) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        if ((pubRel.maxEncodedPropertyLength() != 0) || (pubRel.getReasonCode() != DEFAULT_REASON_CODE)) {
            remainingLength += 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubRelImpl pubRel) {
        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(pubRel.getRawReasonString());
        propertyLength += pubRel.getUserProperties().encodedLength();

        return propertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5PubRelImpl pubRel, @NotNull final ByteBuf out, final int maximumPacketSize) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(pubRel.encodedRemainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5PubRelImpl pubRel, @NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeShort(pubRel.getPacketIdentifier());

        final Mqtt5PubRelReasonCode reasonCode = pubRel.getReasonCode();
        final int propertyLength = pubRel.encodedPropertyLength(maximumPacketSize);
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(pubRel, propertyLength, out, maximumPacketSize);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5PubRelImpl pubRel, final int propertyLength, @NotNull final ByteBuf out,
            final int maximumPacketSize) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);
        pubRel.encodeReasonString(maximumPacketSize, out);
        pubRel.encodeUserProperties(maximumPacketSize, out);
    }

}

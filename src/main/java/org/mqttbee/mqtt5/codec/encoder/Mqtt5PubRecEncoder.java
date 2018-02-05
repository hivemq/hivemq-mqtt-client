package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRecEncoder implements Mqtt5MessageEncoder<Mqtt5PubRecImpl> {

    public static final Mqtt5PubRecEncoder INSTANCE = new Mqtt5PubRecEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBREC.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5PubRecImpl pubRec, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

        encodeFixedHeader(pubRec, out, maximumPacketSize);
        encodeVariableHeader(pubRec, out, maximumPacketSize);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubRecImpl pubRec) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        if ((pubRec.maxEncodedPropertyLength() != 0) || (pubRec.getReasonCode() != DEFAULT_REASON_CODE)) {
            remainingLength += 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubRecImpl pubRec) {
        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(pubRec.getRawReasonString());
        propertyLength += pubRec.getUserProperties().encodedLength();

        return propertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5PubRecImpl pubRec, @NotNull final ByteBuf out, final int maximumPacketSize) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(pubRec.encodedRemainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5PubRecImpl pubRec, @NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeShort(pubRec.getPacketIdentifier());

        final Mqtt5PubRecReasonCode reasonCode = pubRec.getReasonCode();
        final int propertyLength = pubRec.encodedPropertyLength(maximumPacketSize);
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(pubRec, propertyLength, out, maximumPacketSize);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5PubRecImpl pubRec, final int propertyLength, @NotNull final ByteBuf out,
            final int maximumPacketSize) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);
        pubRec.encodeReasonString(maximumPacketSize, out);
        pubRec.encodeUserProperties(maximumPacketSize, out);
    }

}

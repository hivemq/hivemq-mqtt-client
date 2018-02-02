package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelInternal;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodeNullableProperty;
import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelProperty.REASON_STRING;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelEncoder implements Mqtt5MessageEncoder<Mqtt5PubRelInternal> {

    public static final Mqtt5PubRelEncoder INSTANCE = new Mqtt5PubRelEncoder();

    private static final int FIXED_HEADER = (Mqtt5MessageType.PUBREL.getCode() << 4) | 0b0010;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5PubRelInternal pubRelInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(pubRelInternal, out);
        encodeVariableHeader(pubRelInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubRelInternal pubRelInternal) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final int propertyLength = pubRelInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (pubRelInternal.getPubRel().getReasonCode() != DEFAULT_REASON_CODE) {
                remainingLength += 1;
            }
        } else {
            remainingLength += 1;
            remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length"); // TODO
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubRelInternal pubRelInternal) {
        final Mqtt5PubRelImpl pubRel = pubRelInternal.getPubRel();

        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(pubRel.getRawReasonString());
        propertyLength += pubRel.getRawUserProperties().encodedLength();

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length"); // TODO
        }
        return propertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5PubRelInternal pubRelInternal, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(pubRelInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5PubRelInternal pubRelInternal, @NotNull final ByteBuf out) {
        out.writeShort(pubRelInternal.getPacketIdentifier());

        final Mqtt5PubRelImpl pubRel = pubRelInternal.getPubRel();
        final Mqtt5PubRelReasonCode reasonCode = pubRel.getReasonCode();
        final int propertyLength = pubRelInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(pubRel, propertyLength, out);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5PubRelImpl pubRel, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeNullableProperty(REASON_STRING, pubRel.getRawReasonString(), out);
        pubRel.getRawUserProperties().encode(out);
    }

}

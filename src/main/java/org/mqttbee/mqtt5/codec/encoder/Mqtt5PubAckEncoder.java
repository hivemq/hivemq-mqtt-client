package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodeNullableProperty;
import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.puback.Mqtt5PubAckProperty.REASON_STRING;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckEncoder implements Mqtt5MessageEncoder<Mqtt5PubAckInternal> {

    public static final Mqtt5PubAckEncoder INSTANCE = new Mqtt5PubAckEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5PubAckInternal pubAckInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(pubAckInternal, out);
        encodeVariableHeader(pubAckInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubAckInternal pubAckInternal) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final int propertyLength = pubAckInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (pubAckInternal.getPubAck().getReasonCode() != DEFAULT_REASON_CODE) {
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

    public int encodedPropertyLength(@NotNull final Mqtt5PubAckInternal pubAckInternal) {
        final Mqtt5PubAckImpl pubAck = pubAckInternal.getPubAck();

        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(pubAck.getRawReasonString());
        propertyLength += pubAck.getRawUserProperties().encodedLength();

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length"); // TODO
        }
        return propertyLength;
    }

    private void encodeFixedHeader(final Mqtt5PubAckInternal pubAckInternal, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(pubAckInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5PubAckInternal pubAckInternal, @NotNull final ByteBuf out) {
        out.writeShort(pubAckInternal.getPacketIdentifier());

        final Mqtt5PubAckImpl pubAck = pubAckInternal.getPubAck();
        final Mqtt5PubAckReasonCode reasonCode = pubAck.getReasonCode();
        final int propertyLength = pubAckInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(pubAck, propertyLength, out);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5PubAckImpl pubAck, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeNullableProperty(REASON_STRING, pubAck.getRawReasonString(), out);
        pubAck.getRawUserProperties().encode(out);
    }

}

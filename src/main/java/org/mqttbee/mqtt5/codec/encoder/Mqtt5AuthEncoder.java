package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodeProperty;
import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodePropertyNullable;
import static org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthEncoder implements Mqtt5MessageEncoder<Mqtt5AuthImpl> {

    public static final Mqtt5AuthEncoder INSTANCE = new Mqtt5AuthEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;

    @Override
    public void encode(@NotNull final Mqtt5AuthImpl auth, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encodeFixedHeader(auth, out);
        encodeVariableHeader(auth, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5AuthImpl auth) {
        int remainingLength = 1;

        final int propertyLength = auth.encodedPropertyLength();
        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length");
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5AuthImpl auth) {
        int propertyLength = 0;

        propertyLength += 1 + auth.getMethod().encodedLength();

        final byte[] data = auth.getRawData();
        if (data != null) {
            if (!Mqtt5DataTypes.isInBinaryDataRange(data)) {
                throw new Mqtt5BinaryDataExceededException("authentication data");
            }
            propertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(data);
        }

        final Mqtt5UTF8String reasonString = auth.getRawReasonString();
        if (reasonString != null) {
            propertyLength += 1 + reasonString.encodedLength();
        }

        propertyLength += auth.getRawUserProperties().encodedLength();

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return propertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5AuthImpl auth, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(auth.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5AuthImpl auth, @NotNull final ByteBuf out) {
        out.writeByte(auth.getReasonCode().getCode());
        encodeProperties(auth, out);
    }

    private void encodeProperties(@NotNull final Mqtt5AuthImpl auth, @NotNull final ByteBuf out) {
        final int propertyLength = auth.encodedPropertyLength();
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeProperty(AUTHENTICATION_METHOD, auth.getMethod(), out);
        encodePropertyNullable(AUTHENTICATION_DATA, auth.getRawData(), out);
        encodePropertyNullable(REASON_STRING, auth.getRawReasonString(), out);
        auth.getRawUserProperties().encode(out);
    }

}

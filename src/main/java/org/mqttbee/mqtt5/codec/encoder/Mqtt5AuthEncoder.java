package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty.AUTHENTICATION_DATA;
import static org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty.AUTHENTICATION_METHOD;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthEncoder implements Mqtt5MessageEncoder<Mqtt5AuthImpl> {

    public static final Mqtt5AuthEncoder INSTANCE = new Mqtt5AuthEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 1; // reason code

    @Override
    public void encode(@NotNull final Mqtt5AuthImpl auth, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

        encodeFixedHeader(auth, out, maximumPacketSize);
        encodeVariableHeader(auth, out, maximumPacketSize);
    }

    public int encodedRemainingLength() {
        return VARIABLE_HEADER_FIXED_LENGTH;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5AuthImpl auth) {
        int propertyLength = 0;

        propertyLength += propertyEncodedLength(auth.getMethod());
        propertyLength += nullablePropertyEncodedLength(auth.getRawData());
        propertyLength += nullablePropertyEncodedLength(auth.getRawReasonString());
        propertyLength += auth.getUserProperties().encodedLength();

        return propertyLength;
    }

    private void encodeFixedHeader(
            @NotNull final Mqtt5AuthImpl auth, @NotNull final ByteBuf out, final int maximumPacketSize) {

        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(auth.encodedRemainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5AuthImpl auth, @NotNull final ByteBuf out, final int maximumPacketSize) {

        out.writeByte(auth.getReasonCode().getCode());
        encodeProperties(auth, out, maximumPacketSize);
    }

    private void encodeProperties(
            @NotNull final Mqtt5AuthImpl auth, @NotNull final ByteBuf out, final int maximumPacketSize) {

        Mqtt5DataTypes.encodeVariableByteInteger(auth.encodedPropertyLength(maximumPacketSize), out);
        encodeProperty(AUTHENTICATION_METHOD, auth.getMethod(), out);
        encodeNullableProperty(AUTHENTICATION_DATA, auth.getRawData(), out);
        auth.encodeReasonString(maximumPacketSize, out);
        auth.encodeUserProperties(maximumPacketSize, out);
    }

}

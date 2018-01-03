package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthEncoder implements Mqtt5MessageEncoder<Mqtt5AuthImpl> {

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;

    @Override
    public void encode(@NotNull final Mqtt5AuthImpl auth, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        final int propertyLength = calculatePropertyLength(auth);
        final int remainingLength = calculateRemainingLength(auth, propertyLength);

        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(remainingLength, out);
        encodeVariableHeader(auth, propertyLength, out);
    }

    private int calculateRemainingLength(@NotNull final Mqtt5AuthImpl auth, final int propertyLength) {
        int remainingLength = 1;

        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            // TODO exception remaining size exceeded
        }

        return remainingLength;
    }

    private int calculatePropertyLength(@NotNull final Mqtt5AuthImpl auth) {
        int propertyLength = 0;

        propertyLength += 1 + auth.getMethod().encodedLength();

        final byte[] data = auth.getRawData();
        if (data != null) {
            propertyLength += 1 + data.length;
        }

        final Mqtt5UTF8String reasonString = auth.getRawReasonString();
        if (reasonString != null) {
            propertyLength += 1 + reasonString.encodedLength();
        }

        propertyLength += Mqtt5UserProperty.encodedLength(auth.getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            // TODO exception remaining size exceeded
        }

        return propertyLength;
    }

    private void encodeFixedHeader(final int remainingLength, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5AuthImpl auth, final int propertyLength, @NotNull final ByteBuf out) {

        out.writeByte(auth.getReasonCode().getCode());
        encodeProperties(auth, propertyLength, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5AuthImpl auth, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        out.writeByte(Mqtt5AuthProperty.AUTHENTICATION_METHOD);
        auth.getMethod().to(out);

        final byte[] data = auth.getRawData();
        if (data != null) {
            out.writeByte(Mqtt5AuthProperty.AUTHENTICATION_DATA);
            Mqtt5DataTypes.encodeBinaryData(data, out);
        }

        final Mqtt5UTF8String reasonString = auth.getRawReasonString();
        if (reasonString != null) {
            out.writeByte(Mqtt5AuthProperty.REASON_STRING);
            reasonString.to(out);
        }

        Mqtt5UserProperty.encode(auth.getUserProperties(), out);
    }

}

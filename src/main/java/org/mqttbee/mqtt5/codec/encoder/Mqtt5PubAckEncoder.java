package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckInternal;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckProperty;
import org.mqttbee.mqtt5.message.puback.Mqtt5PubAckReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.puback.Mqtt5PubAckImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubAckEncoder implements Mqtt5MessageEncoder<Mqtt5PubAckInternal> {

    public static final Mqtt5PubAckEncoder INSTANCE = new Mqtt5PubAckEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4;

    @Override
    public void encode(
            @NotNull final Mqtt5PubAckInternal pubAckInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(pubAckInternal, out);
        encodeVariableHeader(pubAckInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubAckInternal pubAckInternal) {
        int remainingLength = 2;

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
            throw new Mqtt5VariableByteIntegerExceededException("remaining length");
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubAckInternal pubAckInternal) {
        final Mqtt5PubAckImpl pubAck = pubAckInternal.getPubAck();

        int properyLength = 0;

        final Mqtt5UTF8String reasonString = pubAck.getRawReasonString();
        if (reasonString != null) {
            properyLength += 1 + reasonString.encodedLength();
        }

        properyLength += Mqtt5UserProperty.encodedLength(pubAck.getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(properyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return properyLength;
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

        final Mqtt5UTF8String reasonString = pubAck.getRawReasonString();
        if (reasonString != null) {
            out.writeByte(Mqtt5PubAckProperty.REASON_STRING);
            reasonString.to(out);
        }

        Mqtt5UserProperty.encode(pubAck.getUserProperties(), out);
    }

}

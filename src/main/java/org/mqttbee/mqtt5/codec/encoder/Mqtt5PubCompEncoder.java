package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.encodePropertyNullable;
import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompProperty.REASON_STRING;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompEncoder implements Mqtt5MessageEncoder<Mqtt5PubCompInternal> {

    public static final Mqtt5PubCompEncoder INSTANCE = new Mqtt5PubCompEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;

    @Override
    public void encode(
            @NotNull final Mqtt5PubCompInternal pubCompInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(pubCompInternal, out);
        encodeVariableHeader(pubCompInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubCompInternal pubCompInternal) {
        int remainingLength = 2;

        final int propertyLength = pubCompInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (pubCompInternal.getPubComp().getReasonCode() != DEFAULT_REASON_CODE) {
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

    public int encodedPropertyLength(@NotNull final Mqtt5PubCompInternal pubCompInternal) {
        final Mqtt5PubCompImpl pubComp = pubCompInternal.getPubComp();

        int propertyLength = 0;

        final Mqtt5UTF8String reasonString = pubComp.getRawReasonString();
        if (reasonString != null) {
            propertyLength += 1 + reasonString.encodedLength();
        }

        propertyLength += pubComp.getRawUserProperties().encodedLength();

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return propertyLength;
    }

    private void encodeFixedHeader(final Mqtt5PubCompInternal pubCompInternal, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(pubCompInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5PubCompInternal pubCompInternal, @NotNull final ByteBuf out) {
        out.writeShort(pubCompInternal.getPacketIdentifier());

        final Mqtt5PubCompImpl pubComp = pubCompInternal.getPubComp();
        final Mqtt5PubCompReasonCode reasonCode = pubComp.getReasonCode();
        final int propertyLength = pubCompInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(pubComp, propertyLength, out);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5PubCompImpl pubComp, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodePropertyNullable(REASON_STRING, pubComp.getRawReasonString(), out);
        pubComp.getRawUserProperties().encode(out);
    }

}

package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelInternal;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelProperty;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelEncoder implements Mqtt5MessageEncoder<Mqtt5PubRelInternal> {

    public static final Mqtt5PubRelEncoder INSTANCE = new Mqtt5PubRelEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4 + 0b0010;

    @Override
    public void encode(
            @NotNull final Mqtt5PubRelInternal pubRelInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        encodeFixedHeader(pubRelInternal, out);
        encodeVariableHeader(pubRelInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubRelInternal pubRelInternal) {
        int remainingLength = 2;

        final int propertyLength = pubRelInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (pubRelInternal.getPubRel().getReasonCode() != DEFAULT_REASON_CODE) {
                remainingLength += 1;
            }
        } else {
            remainingLength += 1;
            remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubRelInternal pubRelInternal) {
        final Mqtt5PubRelImpl pubRel = pubRelInternal.getPubRel();

        int properyLength = 0;

        final Mqtt5UTF8String reasonString = pubRel.getRawReasonString();
        if (reasonString != null) {
            properyLength += 1 + reasonString.encodedLength();
        }

        properyLength += Mqtt5UserProperty.encodedLength(pubRel.getUserProperties());

        return properyLength;
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

        final Mqtt5UTF8String reasonString = pubRel.getRawReasonString();
        if (reasonString != null) {
            out.writeByte(Mqtt5PubRelProperty.REASON_STRING);
            reasonString.to(out);
        }

        Mqtt5UserProperty.encode(pubRel.getUserProperties(), out);
    }

}

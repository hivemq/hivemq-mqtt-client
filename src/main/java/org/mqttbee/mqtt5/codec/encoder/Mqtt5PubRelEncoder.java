package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ChannelAttributes;
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

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4 + 0b0010;

    @Override
    public void encode(
            @NotNull final Mqtt5PubRelInternal pubRelInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        final int propertiesLength = calculatePropertyLength(pubRelInternal);
        final int remainingLength = calculateRemainingLength(pubRelInternal, propertiesLength);

        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(remainingLength, out);
        encodeVariableHeader(pubRelInternal, propertiesLength, out);
    }

    private int calculateRemainingLength(
            @NotNull final Mqtt5PubRelInternal pubRelInternal, final int propertyLength) {
        final Mqtt5PubRelImpl pubRel = pubRelInternal.getPubRel();

        int remainingLength = 2;

        if (propertyLength == 0) {
            if (pubRel.getReasonCode() != DEFAULT_REASON_CODE) {
                remainingLength += 1;
            }
        } else {
            remainingLength += 1;
            remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            // TODO exception remaining size exceeded
        }

        return remainingLength;
    }

    private int calculatePropertyLength(@NotNull final Mqtt5PubRelInternal pubRelInternal) {
        final Mqtt5PubRelImpl pubRel = pubRelInternal.getPubRel();

        int properyLength = 0;

        final Mqtt5UTF8String reasonString = pubRel.getRawReasonString();
        if (reasonString != null) {
            properyLength += 1 + reasonString.encodedLength();
        }

        properyLength += Mqtt5UserProperty.encodedLength(pubRel.getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(properyLength)) {
            // TODO exception remaining size exceeded
        }

        return properyLength;
    }

    private void encodeFixedHeader(final int remainingLength, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5PubRelInternal pubRelInternal, final int propertyLength, @NotNull final ByteBuf out) {
        final Mqtt5PubRelImpl pubRel = pubRelInternal.getPubRel();

        out.writeShort(pubRelInternal.getPacketIdentifier());

        final Mqtt5PubRelReasonCode reasonCode = pubRel.getReasonCode();
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

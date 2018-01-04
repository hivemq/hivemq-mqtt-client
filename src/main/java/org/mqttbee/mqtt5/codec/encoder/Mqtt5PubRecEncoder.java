package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecInternal;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecProperty;
import org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.pubrec.Mqtt5PubRecImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRecEncoder implements Mqtt5MessageEncoder<Mqtt5PubRecInternal> {

    public static final Mqtt5PubRecEncoder INSTANCE = new Mqtt5PubRecEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4;

    @Override
    public void encode(
            @NotNull final Mqtt5PubRecInternal pubRecInternal, @NotNull final Channel channel,
            @NotNull final ByteBuf out) {

        final int packetSize = pubRecInternal.encodedLength();
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(pubRecInternal, out);
        encodeVariableHeader(pubRecInternal, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubRecInternal pubRecInternal) {
        int remainingLength = 2;

        final int propertyLength = pubRecInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (pubRecInternal.getPubRec().getReasonCode() != DEFAULT_REASON_CODE) {
                remainingLength += 1;
            }
        } else {
            remainingLength += 1;
            remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubRecInternal pubRecInternal) {
        final Mqtt5PubRecImpl pubRec = pubRecInternal.getPubRec();

        int properyLength = 0;

        final Mqtt5UTF8String reasonString = pubRec.getRawReasonString();
        if (reasonString != null) {
            properyLength += 1 + reasonString.encodedLength();
        }

        properyLength += Mqtt5UserProperty.encodedLength(pubRec.getUserProperties());

        return properyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5PubRecInternal pubRecInternal, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(pubRecInternal.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5PubRecInternal pubRecInternal, @NotNull final ByteBuf out) {
        out.writeShort(pubRecInternal.getPacketIdentifier());

        final Mqtt5PubRecImpl pubRec = pubRecInternal.getPubRec();
        final Mqtt5PubRecReasonCode reasonCode = pubRec.getReasonCode();
        final int propertyLength = pubRecInternal.encodedPropertyLength();
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(pubRec, propertyLength, out);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5PubRecImpl pubRec, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        final Mqtt5UTF8String reasonString = pubRec.getRawReasonString();
        if (reasonString != null) {
            out.writeByte(Mqtt5PubRecProperty.REASON_STRING);
            reasonString.to(out);
        }

        Mqtt5UserProperty.encode(pubRec.getUserProperties(), out);
    }

}

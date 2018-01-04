package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompInternal;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompProperty;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompEncoder implements Mqtt5MessageEncoder<Mqtt5PubCompInternal> {

    public static final Mqtt5PubCompEncoder INSTANCE = new Mqtt5PubCompEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4;

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

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubCompInternal pubCompInternal) {
        final Mqtt5PubCompImpl pubComp = pubCompInternal.getPubComp();

        int properyLength = 0;

        final Mqtt5UTF8String reasonString = pubComp.getRawReasonString();
        if (reasonString != null) {
            properyLength += 1 + reasonString.encodedLength();
        }

        properyLength += Mqtt5UserProperty.encodedLength(pubComp.getUserProperties());

        return properyLength;
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

        final Mqtt5UTF8String reasonString = pubComp.getRawReasonString();
        if (reasonString != null) {
            out.writeByte(Mqtt5PubCompProperty.REASON_STRING);
            reasonString.to(out);
        }

        Mqtt5UserProperty.encode(pubComp.getUserProperties(), out);
    }

}

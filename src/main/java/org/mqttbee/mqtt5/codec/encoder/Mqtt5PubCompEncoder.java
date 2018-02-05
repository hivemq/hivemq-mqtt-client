package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompEncoder implements Mqtt5MessageEncoder<Mqtt5PubCompImpl> {

    public static final Mqtt5PubCompEncoder INSTANCE = new Mqtt5PubCompEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 2; // packet identifier

    @Override
    public void encode(
            @NotNull final Mqtt5PubCompImpl pubComp, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(pubComp, out);
        encodeVariableHeader(pubComp, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5PubCompImpl pubComp) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        if ((pubComp.encodedPropertyLength() != 0) || (pubComp.getReasonCode() != DEFAULT_REASON_CODE)) {
            remainingLength += 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5PubCompImpl pubComp) {
        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(pubComp.getRawReasonString());
        propertyLength += pubComp.getUserProperties().encodedLength();

        return propertyLength;
    }

    private void encodeFixedHeader(final Mqtt5PubCompImpl pubComp, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes
                .encodeVariableByteInteger(pubComp.encodedRemainingLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT),
                        out); // TODO
    }

    private void encodeVariableHeader(@NotNull final Mqtt5PubCompImpl pubComp, @NotNull final ByteBuf out) {
        out.writeShort(pubComp.getPacketIdentifier());

        final Mqtt5PubCompReasonCode reasonCode = pubComp.getReasonCode();
        final int propertyLength = pubComp.encodedPropertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT); // TODO
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

        pubComp.encodeReasonString(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out); // TODO
        pubComp.encodeUserProperties(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out); // TODO
    }

}

package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder.Mqtt5MessageWithOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompEncoder extends Mqtt5MessageWithOmissibleReasonCodeEncoder<Mqtt5PubCompImpl> {

    public static final Function<Mqtt5PubCompImpl, Mqtt5PubCompEncoder> PROVIDER = Mqtt5PubCompEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;
    private static final int VARIABLE_HEADER_LENGTH = 3;// packet identifier (2) + reason code (1)

    private Mqtt5PubCompEncoder(@NotNull final Mqtt5PubCompImpl message) {
        super(message);
    }

    @Override
    boolean canOmitReasonCode() {
        return message.getReasonCode() == DEFAULT_REASON_CODE;
    }

    @Override
    int calculateRemainingLength() {
        return VARIABLE_HEADER_LENGTH;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength += nullablePropertyEncodedLength(message.getRawReasonString());
        propertyLength += message.getUserProperties().encodedLength();

        return propertyLength;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        final int maximumPacketSize = Mqtt5ServerData.get(channel).getMaximumPacketSize();

        encodeFixedHeader(out, maximumPacketSize);
        encodeVariableHeader(out, maximumPacketSize);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeShort(message.getPacketIdentifier());

        final Mqtt5PubCompReasonCode reasonCode = message.getReasonCode();
        final int propertyLength = propertyLength(maximumPacketSize);
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(propertyLength, out, maximumPacketSize);
        }
    }

    private void encodeProperties(
            final int propertyLength, @NotNull final ByteBuf out, final int maximumPacketSize) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);
        encodeOmissibleProperties(maximumPacketSize, out);
    }

}

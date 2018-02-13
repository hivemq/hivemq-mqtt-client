package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithReasonStringEncoder;
import org.mqttbee.mqtt5.handler.Mqtt5ServerDataImpl;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty.AUTHENTICATION_DATA;
import static org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty.AUTHENTICATION_METHOD;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthEncoder extends Mqtt5MessageWithReasonStringEncoder<Mqtt5AuthImpl> {

    public static final Function<Mqtt5AuthImpl, Mqtt5AuthEncoder> PROVIDER = Mqtt5AuthEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.AUTH.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 1; // reason code

    private Mqtt5AuthEncoder(@NotNull final Mqtt5AuthImpl message) {
        super(message);
    }

    @Override
    int calculateRemainingLength() {
        return VARIABLE_HEADER_FIXED_LENGTH;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength += propertyEncodedLength(message.getMethod());
        propertyLength += nullablePropertyEncodedLength(message.getRawData());
        propertyLength += omissiblePropertiesLength();

        return propertyLength;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        final int maximumPacketSize = Mqtt5ServerDataImpl.get(channel).getMaximumPacketSize();

        encodeFixedHeader(out, maximumPacketSize);
        encodeVariableHeader(out, maximumPacketSize);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength(maximumPacketSize), out);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
        out.writeByte(message.getReasonCode().getCode());
        encodeProperties(out, maximumPacketSize);
    }

    private void encodeProperties(@NotNull final ByteBuf out, final int maximumPacketSize) {
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength(maximumPacketSize), out);
        encodeProperty(AUTHENTICATION_METHOD, message.getMethod(), out);
        encodeNullableProperty(AUTHENTICATION_DATA, message.getRawData(), out);
        encodeOmissibleProperties(maximumPacketSize, out);
    }

}

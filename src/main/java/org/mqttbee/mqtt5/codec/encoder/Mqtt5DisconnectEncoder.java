package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.handler.Mqtt5ServerDataImpl;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty.SERVER_REFERENCE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty.SESSION_EXPIRY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectEncoder extends Mqtt5MessageWithOmissibleReasonCodeEncoder<Mqtt5DisconnectImpl> {

    public static final Function<Mqtt5DisconnectImpl, Mqtt5DisconnectEncoder> PROVIDER = Mqtt5DisconnectEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.DISCONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_LENGTH = 1; // reason code

    private Mqtt5DisconnectEncoder(@NotNull final Mqtt5DisconnectImpl message) {
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

        propertyLength +=
                intPropertyEncodedLength(message.getRawSessionExpiryInterval(), SESSION_EXPIRY_INTERVAL_FROM_CONNECT);
        propertyLength += nullablePropertyEncodedLength(message.getRawServerReference());
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
        final Mqtt5DisconnectReasonCode reasonCode = message.getReasonCode();
        final int propertyLength = propertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT);
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(propertyLength, out, maximumPacketSize);
        }
    }

    private void encodeProperties(final int propertyLength, @NotNull final ByteBuf out, final int maximumPacketSize) {
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);
        encodeIntProperty(SESSION_EXPIRY_INTERVAL, message.getRawSessionExpiryInterval(),
                SESSION_EXPIRY_INTERVAL_FROM_CONNECT, out);
        encodeNullableProperty(SERVER_REFERENCE, message.getRawServerReference(), out);
        encodeOmissibleProperties(maximumPacketSize, out);
    }

}

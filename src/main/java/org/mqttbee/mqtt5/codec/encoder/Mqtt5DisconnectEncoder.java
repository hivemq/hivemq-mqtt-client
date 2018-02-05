package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty.SERVER_REFERENCE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty.SESSION_EXPIRY_INTERVAL;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5DisconnectEncoder implements Mqtt5MessageEncoder<Mqtt5DisconnectImpl> {

    public static final Mqtt5DisconnectEncoder INSTANCE = new Mqtt5DisconnectEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.DISCONNECT.getCode() << 4;

    @Override
    public void encode(
            @NotNull final Mqtt5DisconnectImpl disconnect, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(disconnect, out);
        encodeVariableHeader(disconnect, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5DisconnectImpl disconnect) {
        int remainingLength = 0;

        if ((disconnect.maxEncodedPropertyLength() != 0) || (disconnect.getReasonCode() != DEFAULT_REASON_CODE)) {
            remainingLength += 1;
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5DisconnectImpl disconnect) {
        int propertyLength = 0;

        propertyLength += intPropertyEncodedLength(disconnect.getRawSessionExpiryInterval(),
                SESSION_EXPIRY_INTERVAL_FROM_CONNECT);
        propertyLength += nullablePropertyEncodedLength(disconnect.getRawServerReference());
        propertyLength += nullablePropertyEncodedLength(disconnect.getRawReasonString());
        propertyLength += disconnect.getUserProperties().encodedLength();

        return propertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5DisconnectImpl disconnect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes
                .encodeVariableByteInteger(disconnect.encodedRemainingLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT),
                        out); // TODO
    }

    private void encodeVariableHeader(@NotNull final Mqtt5DisconnectImpl disconnect, @NotNull final ByteBuf out) {
        final Mqtt5DisconnectReasonCode reasonCode = disconnect.getReasonCode();
        final int propertyLength = disconnect.encodedPropertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT); // TODO
        if (propertyLength == 0) {
            if (reasonCode != DEFAULT_REASON_CODE) {
                out.writeByte(reasonCode.getCode());
            }
        } else {
            out.writeByte(reasonCode.getCode());
            encodeProperties(disconnect, propertyLength, out);
        }
    }

    private void encodeProperties(
            @NotNull final Mqtt5DisconnectImpl disconnect, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeIntProperty(SESSION_EXPIRY_INTERVAL, disconnect.getRawSessionExpiryInterval(),
                SESSION_EXPIRY_INTERVAL_FROM_CONNECT, out);
        encodeNullableProperty(SERVER_REFERENCE, disconnect.getRawServerReference(), out);
        disconnect.encodeReasonString(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out); // TODO
        disconnect.encodeUserProperties(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out); // TODO
    }

}

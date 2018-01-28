package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;

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

        final int propertyLength = disconnect.encodedPropertyLength();
        if (propertyLength == 0) {
            if (disconnect.getReasonCode() != DEFAULT_REASON_CODE) {
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

    public int encodedPropertyLength(@NotNull final Mqtt5DisconnectImpl disconnect) {
        int propertyLength = 0;

        if (disconnect.getRawSessionExpiryInterval() != SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            propertyLength += 5;
        }

        final Mqtt5UTF8String serverReference = disconnect.getRawServerReference();
        if (serverReference != null) {
            propertyLength += 1 + serverReference.encodedLength();
        }

        final Mqtt5UTF8String reasonString = disconnect.getRawReasonString();
        if (reasonString != null) {
            propertyLength += 1 + reasonString.encodedLength();
        }

        propertyLength += disconnect.getRawUserProperties().encodedLength();

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return propertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5DisconnectImpl disconnect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(disconnect.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final Mqtt5DisconnectImpl disconnect, @NotNull final ByteBuf out) {
        final Mqtt5DisconnectReasonCode reasonCode = disconnect.getReasonCode();
        final int propertyLength = disconnect.encodedPropertyLength();
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

        final long sessionExpiryInterval = disconnect.getRawSessionExpiryInterval();
        if (sessionExpiryInterval != SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
            out.writeByte(Mqtt5DisconnectProperty.SESSION_EXPIRY_INTERVAL);
            out.writeInt((int) sessionExpiryInterval);
        }

        final Mqtt5UTF8String serverReference = disconnect.getRawServerReference();
        if (serverReference != null) {
            out.writeByte(Mqtt5DisconnectProperty.SERVER_REFERENCE);
            serverReference.to(out);
        }

        final Mqtt5UTF8String reasonString = disconnect.getRawReasonString();
        if (reasonString != null) {
            out.writeByte(Mqtt5DisconnectProperty.REASON_STRING);
            reasonString.to(out);
        }

        disconnect.getRawUserProperties().encode(out);
    }

}

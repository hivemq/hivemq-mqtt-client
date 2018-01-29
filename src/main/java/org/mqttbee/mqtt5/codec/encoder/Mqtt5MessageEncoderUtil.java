package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;

/**
 * @author Silvio Giebl
 */
class Mqtt5MessageEncoderUtil {

    private Mqtt5MessageEncoderUtil() {
    }

    static void encodeProperty(
            final int propertyIdentifier, @NotNull final Mqtt5UTF8String string, @NotNull final ByteBuf out) {

        out.writeByte(propertyIdentifier);
        string.to(out);
    }

    static void encodePropertyNullable(
            final int propertyIdentifier, @Nullable final Mqtt5UTF8String string, @NotNull final ByteBuf out) {

        if (string != null) {
            encodeProperty(propertyIdentifier, string, out);
        }
    }

    static void encodePropertyNullable(
            final int propertyIdentifier, @Nullable final byte[] binary, @NotNull final ByteBuf out) {

        if (binary != null) {
            out.writeByte(propertyIdentifier);
            Mqtt5DataTypes.encodeBinaryData(binary, out);
        }
    }

    static void encodePropertyNullable(
            final int propertyIdentifier, @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @NotNull final ByteBuf out) {

        if (payloadFormatIndicator != null) {
            out.writeByte(propertyIdentifier);
            out.writeByte(payloadFormatIndicator.getCode());
        }
    }

    static void encodeShortProperty(
            final int propertyIdentifier, final int value, final int defaultValue, @NotNull final ByteBuf out) {

        if (value != defaultValue) {
            out.writeByte(propertyIdentifier);
            out.writeShort(value);
        }
    }

    static void encodeIntProperty(
            final int propertyIdentifier, final long value, final long defaultValue, @NotNull final ByteBuf out) {

        if (value != defaultValue) {
            out.writeByte(propertyIdentifier);
            out.writeInt((int) value);
        }
    }

    static void encodeVariableByteIntegerProperty(
            final int propertyIdentifier, final int value, @NotNull final ByteBuf out) {

        out.writeByte(propertyIdentifier);
        Mqtt5DataTypes.encodeVariableByteInteger(value, out);
    }

    static void encodeVariableByteIntegerProperty(
            final int propertyIdentifier, final int value, final long defaultValue, @NotNull final ByteBuf out) {

        if (value != defaultValue) {
            encodeVariableByteIntegerProperty(propertyIdentifier, value, out);
        }
    }

}

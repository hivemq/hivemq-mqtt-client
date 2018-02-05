package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;

/**
 * @author Silvio Giebl
 */
public class Mqtt5MessageEncoderUtil {

    private Mqtt5MessageEncoderUtil() {
    }

    /**
     * Calculates the encoded length of a MQTT message with the given remaining length.
     *
     * @param remainingLength the remaining length of the MQTT message.
     * @return the encoded length of the MQTT message.
     */
    public static int encodedPacketLength(final int remainingLength) {
        return 1 + encodedLengthWithHeader(remainingLength);
    }

    /**
     * Calculates the encoded length with a prefixed header.
     *
     * @param encodedLength the encoded length.
     * @return the encoded length with a prefixed header.
     */
    public static int encodedLengthWithHeader(final int encodedLength) {
        return Mqtt5DataTypes.encodedVariableByteIntegerLength(encodedLength) + encodedLength;
    }

    static int nullableEncodedLength(@Nullable final Mqtt5UTF8StringImpl string) {
        return (string == null) ? 0 : string.encodedLength();
    }

    static int nullableEncodedLength(@Nullable final byte[] binary) {
        return (binary == null) ? 0 : Mqtt5DataTypes.encodedBinaryDataLength(binary);
    }

    static void encodeNullable(@Nullable final Mqtt5UTF8StringImpl string, @NotNull final ByteBuf out) {
        if (string != null) {
            string.to(out);
        }
    }

    static void encodeNullable(@Nullable final byte[] binary, @NotNull final ByteBuf out) {
        if (binary != null) {
            Mqtt5DataTypes.encodeBinaryData(binary, out);
        }
    }

    static int propertyEncodedLength(@NotNull final Mqtt5UTF8StringImpl string) {
        return 1 + string.encodedLength();
    }

    public static int nullablePropertyEncodedLength(@Nullable final Mqtt5UTF8StringImpl string) {
        return (string == null) ? 0 : propertyEncodedLength(string);
    }

    static int nullablePropertyEncodedLength(@Nullable final byte[] binary) {
        return (binary == null) ? 0 : 1 + Mqtt5DataTypes.encodedBinaryDataLength(binary);
    }

    static int propertyEncodedLength(
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {

        return (payloadFormatIndicator == null) ? 0 : 2;
    }

    static int booleanPropertyEncodedLength(final boolean value, final boolean defaultValue) {
        return (value == defaultValue) ? 0 : 2;
    }

    static int shortPropertyEncodedLength(final int value, final int defaultValue) {
        return (value == defaultValue) ? 0 : 3;
    }

    static int intPropertyEncodedLength(final long value, final long defaultValue) {
        return (value == defaultValue) ? 0 : 5;
    }

    static int variableByteIntegerPropertyEncodedLength(final int value) {
        return 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(value);
    }

    static int variableByteIntegerPropertyEncodedLength(final int value, final int defaultValue) {
        return (value == defaultValue) ? 0 : variableByteIntegerPropertyEncodedLength(value);
    }

    static void encodeProperty(
            final int propertyIdentifier, @NotNull final Mqtt5UTF8StringImpl string, @NotNull final ByteBuf out) {

        out.writeByte(propertyIdentifier);
        string.to(out);
    }

    public static void encodeNullableProperty(
            final int propertyIdentifier, @Nullable final Mqtt5UTF8StringImpl string, @NotNull final ByteBuf out) {

        if (string != null) {
            encodeProperty(propertyIdentifier, string, out);
        }
    }

    static void encodeNullableProperty(
            final int propertyIdentifier, @Nullable final byte[] binary, @NotNull final ByteBuf out) {

        if (binary != null) {
            out.writeByte(propertyIdentifier);
            Mqtt5DataTypes.encodeBinaryData(binary, out);
        }
    }

    static void encodeNullableProperty(
            final int propertyIdentifier, @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @NotNull final ByteBuf out) {

        if (payloadFormatIndicator != null) {
            out.writeByte(propertyIdentifier);
            out.writeByte(payloadFormatIndicator.getCode());
        }
    }

    static void encodeBooleanProperty(
            final int propertyIdentifier, final boolean value, final boolean defaultValue, @NotNull final ByteBuf out) {

        if (value != defaultValue) {
            out.writeByte(propertyIdentifier);
            out.writeByte(value ? 1 : 0);
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

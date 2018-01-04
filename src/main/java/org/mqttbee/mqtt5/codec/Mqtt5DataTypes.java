package org.mqttbee.mqtt5.codec;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DataTypes {

    public static final int VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES = -1;
    public static final int VARIABLE_BYTE_INTEGER_TOO_LARGE = -2;
    public static final int VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES = -3;
    private static final int VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK = 0x80;
    private static final int VARIABLE_BYTE_INTEGER_VALUE_MASK = 0x7f;
    private static final int VARIABLE_BYTE_INTEGER_MAX_SHIFT = 7 * 3;
    private static final int VARIABLE_BYTE_INTEGER_ONE_BYTE_MAX_VALUE = (1 << 7) - 1;
    private static final int VARIABLE_BYTE_INTEGER_TWO_BYTES_MAX_VALUE = (1 << (7 * 2)) - 1;
    private static final int VARIABLE_BYTE_INTEGER_THREE_BYTES_MAX_VALUE = (1 << (7 * 3)) - 1;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE = (1 << (7 * 4)) - 1;

    private Mqtt5DataTypes() {
    }

    public static int decodeVariableByteInteger(@NotNull final ByteBuf byteBuf) {
        byte encodedByte;
        int value = 0;
        byte shift = 0;

        do {
            if (shift > VARIABLE_BYTE_INTEGER_MAX_SHIFT) {
                return VARIABLE_BYTE_INTEGER_TOO_LARGE;
            }
            if (!byteBuf.isReadable()) {
                return VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES;
            }
            encodedByte = byteBuf.readByte();
            final int encodedByteValue = encodedByte & VARIABLE_BYTE_INTEGER_VALUE_MASK;
            if (shift > 0 && encodedByteValue == 0) {
                return VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES;
            }
            value += encodedByteValue << shift;
            shift += 7;
        } while ((encodedByte & VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK) != 0);

        return value;
    }

    public static boolean encodeVariableByteInteger(int value, @NotNull final ByteBuf byteBuf) {
        if (!isInVariableByteIntegerRange(value)) {
            return false;
        }

        do {
            int encodedByte = value & VARIABLE_BYTE_INTEGER_VALUE_MASK;
            value >>>= 7;
            if (value > 0) {
                encodedByte |= VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK;
            }
            byteBuf.writeByte(encodedByte);
        } while (value > 0);

        return true;
    }

    public static boolean isInVariableByteIntegerRange(final int value) {
        return (value >= 0) && (value <= VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE);
    }

    public static int encodedVariableByteIntegerLength(final int value) {
        int length = 1;
        if (value > VARIABLE_BYTE_INTEGER_ONE_BYTE_MAX_VALUE) {
            length++;
        }
        if (value > VARIABLE_BYTE_INTEGER_TWO_BYTES_MAX_VALUE) {
            length++;
        }
        if (value > VARIABLE_BYTE_INTEGER_THREE_BYTES_MAX_VALUE) {
            length++;
        }
        return length;
    }

    @Nullable
    public static byte[] decodeBinaryData(@NotNull final ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 2) {
            return null;
        }
        final int length = byteBuf.readUnsignedShort();
        if (byteBuf.readableBytes() < length) {
            return null;
        }
        final byte[] binary = new byte[length];
        byteBuf.readBytes(binary);
        return binary;
    }

    public static void encodeBinaryData(@NotNull final byte[] binary, @NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(binary.length);
        byteBuf.writeBytes(binary);
    }

    public static int encodedBinaryDataLength(@NotNull final byte[] binary) {
        return 2 + binary.length;
    }

    public static boolean skipBinaryData(@NotNull final ByteBuf byteBuf) {
        if (byteBuf.readableBytes() < 2) {
            return false;
        }
        final int length = byteBuf.readUnsignedShort();
        if (byteBuf.readableBytes() < length) {
            return false;
        }
        byteBuf.skipBytes(length);
        return true;
    }

}

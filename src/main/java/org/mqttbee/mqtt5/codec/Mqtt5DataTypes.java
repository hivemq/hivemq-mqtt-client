package org.mqttbee.mqtt5.codec;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;

/**
 * Utility for decoding, encoding and checking variable byte integers and binary data according to the MQTT 5
 * specification.
 *
 * @author Silvio Giebl
 */
public class Mqtt5DataTypes {

    public static final int VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES = -1;
    public static final int VARIABLE_BYTE_INTEGER_TOO_LARGE = -2;
    public static final int VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES = -3;
    private static final int VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK = 0x80;
    private static final int VARIABLE_BYTE_INTEGER_VALUE_MASK = 0x7f;
    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_MAX_SHIFT = VARIABLE_BYTE_INTEGER_VALUE_BITS * 3;
    private static final int VARIABLE_BYTE_INTEGER_ONE_BYTE_MAX_VALUE = (1 << VARIABLE_BYTE_INTEGER_VALUE_BITS) - 1;
    private static final int VARIABLE_BYTE_INTEGER_TWO_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 2)) - 1;
    private static final int VARIABLE_BYTE_INTEGER_THREE_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 3)) - 1;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;
    public static final int MAXIMUM_PACKET_SIZE_LIMIT =
            1 + 4 + Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE;
    private static final int BINARY_DATA_MAX_LENGTH = 65_535;
    public static final int EMPTY_BINARY_DATA_LENGTH = 2;

    private Mqtt5DataTypes() {
    }

    /**
     * Decodes a variable byte integer from the given byte buffer at the current reader index.
     * <p>
     * In case of a wrong encoding the reader index of the byte buffer will be in an undefined state after the method
     * returns.
     *
     * @param byteBuf the buffer to decode from.
     * @return the decoded integer value or {@link #VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES} if there are not enough
     * bytes in the byte buffer or {@link #VARIABLE_BYTE_INTEGER_TOO_LARGE} if the encoded variable byte integer has
     * more than 4 bytes or {@link #VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES} if the value is not encoded with a minimum
     * number of bytes.
     */
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
            value += encodedByteValue << shift;
            shift += VARIABLE_BYTE_INTEGER_VALUE_BITS;
        } while ((encodedByte & VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK) != 0);

        if (shift > VARIABLE_BYTE_INTEGER_VALUE_BITS && encodedByte == 0) {
            return VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES;
        }

        return value;
    }

    /**
     * Encodes the given value as a variable byte integer to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if the value is in range of a 4 byte variable byte integer.
     *
     * @param value   the value to encode.
     * @param byteBuf the byte buffer to encode to.
     */
    public static void encodeVariableByteInteger(int value, @NotNull final ByteBuf byteBuf) {
        do {
            int encodedByte = value & VARIABLE_BYTE_INTEGER_VALUE_MASK;
            value >>>= 7;
            if (value > 0) {
                encodedByte |= VARIABLE_BYTE_INTEGER_CONTINUATION_BIT_MASK;
            }
            byteBuf.writeByte(encodedByte);
        } while (value > 0);
    }

    /**
     * Checks if the given value is in range of a 4 byte variable byte integer.
     *
     * @param value the value to check.
     * @return whether the value is in range of a 4 byte variable byte integer.
     */
    public static boolean isInVariableByteIntegerRange(final int value) {
        return (value >= 0) && (value <= VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE);
    }

    /**
     * Calculates the byte count of the given value encoded as a variable byte integer.
     * <p>
     * This method does not check if the value is in range of a 4 byte variable byte integer.
     *
     * @param value the value to calculate the encoded length for.
     * @return the encoded length of the value.
     */
    public static int encodedVariableByteIntegerLength(final int value) {
        int length = 1;
        if (value > VARIABLE_BYTE_INTEGER_ONE_BYTE_MAX_VALUE) {
            length++;
            if (value > VARIABLE_BYTE_INTEGER_TWO_BYTES_MAX_VALUE) {
                length++;
                if (value > VARIABLE_BYTE_INTEGER_THREE_BYTES_MAX_VALUE) {
                    length++;
                }
            }
        }
        return length;
    }

    /**
     * Decodes binary data from the given byte buffer at the current reader index.
     *
     * @param byteBuf the byte buffer to decode from.
     * @return the decoded binary data or null if there are not enough bytes in the byte buffer.
     */
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

    /**
     * Decodes binary data from the given byte buffer at the current reader index.
     *
     * @param byteBuf the byte buffer to decode from.
     * @param direct  whether the created byte buffer should be direct.
     * @return the decoded binary data or null if there are not enough bytes in the byte buffer.
     */
    @Nullable
    public static ByteBuffer decodeBinaryData(@NotNull final ByteBuf byteBuf, final boolean direct) {
        if (byteBuf.readableBytes() < 2) {
            return null;
        }
        final int length = byteBuf.readUnsignedShort();
        if (byteBuf.readableBytes() < length) {
            return null;
        }
        final ByteBuffer byteBuffer = ByteBufferUtil.allocate(length, direct);
        byteBuf.readBytes(byteBuffer);
        byteBuffer.position(0);
        return byteBuffer;
    }

    /**
     * Encodes the given byte array as binary data to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if the byte array can be encoded as binary data.
     *
     * @param binary  the byte array to encode.
     * @param byteBuf the byte buffer to encode to.
     */
    public static void encodeBinaryData(@NotNull final byte[] binary, @NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(binary.length);
        byteBuf.writeBytes(binary);
    }

    /**
     * Encodes the given byte buffer as binary data to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if the byte buffer can be encoded as binary data.
     *
     * @param byteBuffer the byte buffer to encode.
     * @param byteBuf    the byte buffer to encode to.
     */
    public static void encodeBinaryData(@NotNull final ByteBuffer byteBuffer, @NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(byteBuffer.remaining());
        byteBuf.writeBytes(byteBuffer);
    }

    /**
     * Encodes a zero length binary data to the given byte buffer at the current writer index.
     *
     * @param byteBuf the byte buffer to encode to.
     */
    public static void encodeEmptyBinaryData(@NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(0);
    }

    /**
     * Checks if the given byte array can be encoded as binary data.
     *
     * @param binary the byte array to check.
     * @return whether the byte array can be encoded as binary data.
     */
    public static boolean isInBinaryDataRange(@NotNull final byte[] binary) {
        return binary.length <= BINARY_DATA_MAX_LENGTH;
    }

    /**
     * Checks if the given byte buffer can be encoded as binary data.
     *
     * @param byteBuffer the byte buffer to check.
     * @return whether the byte buffer can be encoded as binary data.
     */
    public static boolean isInBinaryDataRange(@NotNull final ByteBuffer byteBuffer) {
        return byteBuffer.remaining() <= BINARY_DATA_MAX_LENGTH;
    }

    /**
     * Calculates the byte count of the given byte array encoded as binary data.
     * <p>
     * This method does not check if the byte array can be encoded as binary data.
     *
     * @param binary the byte array to calculate the encoded length for.
     * @return the encoded length of the byte array.
     */
    public static int encodedBinaryDataLength(@NotNull final byte[] binary) {
        return 2 + binary.length;
    }

    /**
     * Calculates the byte count of the given byte buffer encoded as binary data.
     * <p>
     * This method does not check if the byte buffer can be encoded as binary data.
     *
     * @param byteBuffer the byte buffer to calculate the encoded length for.
     * @return the encoded length of the byte buffer.
     */
    public static int encodedBinaryDataLength(@NotNull final ByteBuffer byteBuffer) {
        return 2 + byteBuffer.remaining();
    }

}

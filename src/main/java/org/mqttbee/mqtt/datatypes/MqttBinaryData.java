package org.mqttbee.mqtt.datatypes;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;

/**
 * Utility for decoding, encoding and checking binary data.
 *
 * @author Silvio Giebl
 */
public class MqttBinaryData {

    private static final int MAX_LENGTH = 65_535;
    public static final int EMPTY_LENGTH = 2;

    private MqttBinaryData() {
    }

    /**
     * Decodes binary data from the given byte buffer at the current reader index.
     *
     * @param byteBuf the byte buffer to decode from.
     * @return the decoded binary data or null if there are not enough bytes in the byte buffer.
     */
    @Nullable
    public static byte[] decode(@NotNull final ByteBuf byteBuf) {
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
    public static ByteBuffer decode(@NotNull final ByteBuf byteBuf, final boolean direct) {
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
    public static void encode(@NotNull final byte[] binary, @NotNull final ByteBuf byteBuf) {
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
    public static void encode(@NotNull final ByteBuffer byteBuffer, @NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(byteBuffer.remaining());
        byteBuf.writeBytes(byteBuffer.duplicate());
    }

    /**
     * Encodes a zero length binary data to the given byte buffer at the current writer index.
     *
     * @param byteBuf the byte buffer to encode to.
     */
    public static void encodeEmpty(@NotNull final ByteBuf byteBuf) {
        byteBuf.writeShort(0);
    }

    /**
     * Checks if the given byte array can be encoded as binary data.
     *
     * @param binary the byte array to check.
     * @return whether the byte array can be encoded as binary data.
     */
    public static boolean isInRange(@NotNull final byte[] binary) {
        return binary.length <= MAX_LENGTH;
    }

    /**
     * Checks if the given byte buffer can be encoded as binary data.
     *
     * @param byteBuffer the byte buffer to check.
     * @return whether the byte buffer can be encoded as binary data.
     */
    public static boolean isInRange(@NotNull final ByteBuffer byteBuffer) {
        return byteBuffer.remaining() <= MAX_LENGTH;
    }

    /**
     * Calculates the byte count of the given byte array encoded as binary data.
     * <p>
     * This method does not check if the byte array can be encoded as binary data.
     *
     * @param binary the byte array to calculate the encoded length for.
     * @return the encoded length of the byte array.
     */
    public static int encodedLength(@NotNull final byte[] binary) {
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
    public static int encodedLength(@NotNull final ByteBuffer byteBuffer) {
        return 2 + byteBuffer.remaining();
    }

}

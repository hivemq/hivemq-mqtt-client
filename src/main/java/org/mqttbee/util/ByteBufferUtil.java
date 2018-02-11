package org.mqttbee.util;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class ByteBufferUtil {

    @Nullable
    public static ByteBuffer wrap(@Nullable final byte[] binary) {
        return (binary == null) ? null : ByteBuffer.wrap(binary);
    }

    @Nullable
    public static ByteBuffer slice(@Nullable final ByteBuffer byteBuffer) {
        return (byteBuffer == null) ? null : byteBuffer.slice();
    }

    @NotNull
    public static Optional<ByteBuffer> optionalReadOnly(@Nullable final ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return Optional.empty();
        }
        final ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        readOnlyBuffer.clear();
        return Optional.of(readOnlyBuffer);
    }

    @NotNull
    public static byte[] getBytes(@NotNull final ByteBuffer byteBuffer) {
        final byte[] binary = new byte[byteBuffer.remaining()];
        byteBuffer.get(binary).position(0);
        return binary;
    }

}

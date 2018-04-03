package org.mqttbee.util;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class ByteArray {

    protected final byte[] array;
    protected int start;
    protected int end;

    public ByteArray(@NotNull final byte[] array, final int start, final int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    public int length() {
        return end - start;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteArray)) {
            return false;
        }
        final ByteArray byteArray = (ByteArray) o;
        return ByteArrayUtil.equals(array, start, end, byteArray.array, byteArray.start, byteArray.end);
    }

    @Override
    public int hashCode() {
        return ByteArrayUtil.hashCode(array, start, end);
    }

}

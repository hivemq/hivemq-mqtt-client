package org.mqttbee.util;

/**
 * @author Silvio Giebl
 */
public class UnsignedDataTypes {

    public static final int UNSIGNED_SHORT_MAX_VALUE = 0xFFFF;
    public static final long UNSIGNED_INT_MAX_VALUE = 0xFFFF_FFFFL;

    public static boolean isUnsignedShort(final int value) {
        return (value >= 0) && (value <= UNSIGNED_SHORT_MAX_VALUE);
    }

    public static boolean isUnsignedInt(final long value) {
        return (value >= 0) && (value <= UNSIGNED_INT_MAX_VALUE);
    }

}

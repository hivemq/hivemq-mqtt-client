package org.mqttbee.api.mqtt5.message.publish;

import org.mqttbee.annotations.Nullable;

/**
 * Payload Format Indicator according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5PayloadFormatIndicator {

    UNSPECIFIED,
    UTF_8;

    /**
     * @return the byte code of this Payload Format Indicator.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the Payload Format Indicator belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the Payload Format Indicator belonging to the byte code or null if the byte code is not a valid Payload
     * Format Indicator.
     */
    @Nullable
    public static Mqtt5PayloadFormatIndicator fromCode(final int code) {
        final Mqtt5PayloadFormatIndicator[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

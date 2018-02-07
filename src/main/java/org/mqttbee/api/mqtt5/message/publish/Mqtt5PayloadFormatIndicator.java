package org.mqttbee.api.mqtt5.message.publish;

import org.mqttbee.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5PayloadFormatIndicator {

    UNSPECIFIED,
    UTF_8;

    public int getCode() {
        return ordinal();
    }

    @Nullable
    public static Mqtt5PayloadFormatIndicator fromCode(final int code) {
        final Mqtt5PayloadFormatIndicator[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

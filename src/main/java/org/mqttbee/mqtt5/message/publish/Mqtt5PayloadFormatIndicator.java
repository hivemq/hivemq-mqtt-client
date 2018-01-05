package org.mqttbee.mqtt5.message.publish;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5PayloadFormatIndicator {

    UNSPECIFIED,
    UTF_8;

    public int getCode() {
        return ordinal();
    }

    public static Mqtt5PayloadFormatIndicator fromCode(final int code) {
        final Mqtt5PayloadFormatIndicator[] values = values();
        if (code < 0 || code >= values.length) {
            throw new IllegalArgumentException("not a MQTT 5 payload format indicator code");
        }
        return values[code];
    }

}

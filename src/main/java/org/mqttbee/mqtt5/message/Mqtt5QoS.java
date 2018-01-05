package org.mqttbee.mqtt5.message;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5QoS {

    AT_MOST_ONCE,
    AT_LEAST_ONCE,
    EXACTLY_ONCE;

    public int getCode() {
        return ordinal();
    }

    public static Mqtt5QoS fromCode(final int code) {
        final Mqtt5QoS[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

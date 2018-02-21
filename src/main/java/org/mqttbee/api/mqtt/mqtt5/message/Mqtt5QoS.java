package org.mqttbee.api.mqtt.mqtt5.message;

import org.mqttbee.annotations.Nullable;

/**
 * MQTT Quality of Service according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5QoS {

    /**
     * QoS for at most once delivery according to the capabilities of the underlying network.
     */
    AT_MOST_ONCE,
    /**
     * QoS for ensuring at least once delivery.
     */
    AT_LEAST_ONCE,
    /**
     * QoS for ensuring exactly once delivery.
     */
    EXACTLY_ONCE;

    /**
     * @return the byte code of this QoS.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the QoS belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the QoS belonging to the given byte code or null if the byte code is not a valid QoS code.
     */
    @Nullable
    public static Mqtt5QoS fromCode(final int code) {
        final Mqtt5QoS[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

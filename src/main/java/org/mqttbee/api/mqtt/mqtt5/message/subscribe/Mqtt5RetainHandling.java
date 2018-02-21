package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import org.mqttbee.annotations.Nullable;

/**
 * Retain Handling according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5RetainHandling {

    SEND,
    SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
    DO_NOT_SEND;

    /**
     * @return the byte code of this Retain Handling.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the Retain Handling belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the Retain Handling belonging to the byte code or null if the byte code is not a valid Retain Handling.
     */
    @Nullable
    public static Mqtt5RetainHandling fromCode(final int code) {
        final Mqtt5RetainHandling[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

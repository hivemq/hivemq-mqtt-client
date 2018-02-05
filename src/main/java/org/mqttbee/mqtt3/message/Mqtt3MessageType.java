package org.mqttbee.mqtt3.message;

import org.mqttbee.annotations.NotNull;

/**
 * MQTT message type according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt3MessageType {

    RESERVED_ZERO,
    CONNECT,
    CONNACK,
    PUBLISH,
    PUBACK,
    PUBREC,
    PUBREL,
    PUBCOMP,
    SUBSCRIBE,
    SUBACK,
    UNSUBSCRIBE,
    UNSUBACK,
    PINGREQ,
    PINGRESP,
    DISCONNECT,
    AUTH;

    /**
     * Returns the MQTT message type belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the MQTT message type belonging to the given byte code.
     * @throws IllegalArgumentException if the byte code is not a valid MQTT message type code.
     */
    @NotNull
    public static Mqtt3MessageType fromCode(final int code) {
        final Mqtt3MessageType[] values = values();
        if (code < 0 || code >= values.length) {
            throw new IllegalArgumentException("not a MQTT 5 message type code");
        }
        return values[code];
    }

    /**
     * @return the byte code of this MQTT message type.
     */
    public int getCode() {
        return ordinal();
    }

}

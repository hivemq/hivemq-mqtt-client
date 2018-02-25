package org.mqttbee.api.mqtt.mqtt5.message;

import org.mqttbee.annotations.Nullable;

/**
 * MQTT message type according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5MessageType {

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
     * @return the byte code of this MQTT message type.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the MQTT message type belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the MQTT message type belonging to the given byte code or null if the byte code is not a valid MQTT
     * message type code.
     */
    @Nullable
    public static Mqtt5MessageType fromCode(final int code) {
        final Mqtt5MessageType[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

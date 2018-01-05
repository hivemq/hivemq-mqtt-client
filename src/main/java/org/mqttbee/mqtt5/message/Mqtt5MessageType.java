package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;

/**
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

    public int getCode() {
        return ordinal();
    }

    @NotNull
    public static Mqtt5MessageType fromCode(final int code) {
        final Mqtt5MessageType[] values = values();
        if (code < 0 || code >= values.length) {
            throw new IllegalArgumentException("not a MQTT 5 message type code");
        }
        return values[code];
    }

}

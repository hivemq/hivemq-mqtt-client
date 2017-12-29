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

    @NotNull
    public static Mqtt5MessageType fromCode(final int code) {
        if (code < 0 || code > 15) {
            throw new IllegalArgumentException("wrong MQTT 5 message type");
        }
        return values()[code];
    }

    public int getCode() {
        return ordinal();
    }

}

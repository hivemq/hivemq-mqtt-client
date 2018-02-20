package org.mqttbee.mqtt3.message;

import org.mqttbee.annotations.NotNull;

/**
 * MQTT message type according to the MQTT 3 specification.
 * See also <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html">the official MQTT 3.1.1 specification</a>.
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
     * @throws IllegalArgumentException if the byte code is not a valid MQTT 3 message type code.
     */
    @NotNull
    public static Mqtt3MessageType fromCode(final int code) {
        final Mqtt3MessageType[] values = values();
        if (code < 0 || code >= values.length) {
            throw new IllegalArgumentException(code + " is not a valid MQTT 3 message type code");
        }
        return values[code];
    }

    /**
     * @return the byte code of this MQTT 3 message type.
     */
    public int getCode() {
        return ordinal();
    }

}

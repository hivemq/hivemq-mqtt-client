package org.mqttbee.api.mqtt.mqtt3.message.connect.connack;

import org.mqttbee.annotations.Nullable;

/**
 * CONNACK Return Code according to the MQTT 3.1.1 specification.
 */
public enum Mqtt3ConnAckReturnCode {

    SUCCESS,
    UNSUPPORTED_PROTOCOL_VERSION,
    IDENTIFIER_REJECTED,
    SERVER_UNAVAILABLE,
    BAD_USER_NAME_OR_PASSWORD,
    NOT_AUTHORIZED;

    /**
     * @return the byte code of this CONNACK Return Code.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the CONNACK Return Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the CONNACK Return Code belonging to the given byte code or null if the byte code is not a valid CONNACK
     * Return Code code.
     */
    @Nullable
    public static Mqtt3ConnAckReturnCode fromCode(final int code) {
        final Mqtt3ConnAckReturnCode[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }

}

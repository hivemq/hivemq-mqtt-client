package org.mqttbee.api.mqtt.mqtt3.message.connect.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3ReasonCode;

public enum Mqtt3ConnAckReturnCode {

    SUCCESS(Mqtt3ReasonCode.SUCCESS),
    UNSUPPORTED_PROTOCOL_VERSION(1),
    IDENTIFIER_REJECTED(2),
    SERVER_UNAVAILABLE(3),
    BAD_USERNAME_OR_PASSWORD(4),
    NOT_AUTHORIZED(5);


    private final int code;

    Mqtt3ConnAckReturnCode(final int code) {
        this.code = code;
    }

    Mqtt3ConnAckReturnCode(@NotNull final Mqtt3ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * Returns the CONNACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the CONNACK Reason Code belonging to the given byte code or null if the byte code is not a valid CONNACK
     * Reason Code code.
     */
    @Nullable
    public static Mqtt3ConnAckReturnCode fromCode(final int code) {
        if (code < 0) {
            return null;
        }
        if (code > NOT_AUTHORIZED.ordinal()) {
            return null;
        }
        return Mqtt3ConnAckReturnCode.values()[code];
    }

    /**
     * @return the byte code of this CONNACK Reason Code.
     */
    public int getCode() {
        return code;
    }

}

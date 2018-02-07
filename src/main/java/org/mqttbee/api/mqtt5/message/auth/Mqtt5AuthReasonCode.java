package org.mqttbee.api.mqtt5.message.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * MQTT Reason Codes that can be used in AUTH packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5AuthReasonCode {

    SUCCESS(Mqtt5ReasonCode.SUCCESS),
    CONTINUE_AUTHENTICATION(0x18),
    REAUTHENTICATE(0x19);

    private final int code;

    Mqtt5AuthReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5AuthReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this AUTH Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the AUTH Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the AUTH Reason Code belonging to the given byte code or null if the byte code is not a valid AUTH Reason
     * Code code.
     */
    @Nullable
    public static Mqtt5AuthReasonCode fromCode(final int code) {
        for (final Mqtt5AuthReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}

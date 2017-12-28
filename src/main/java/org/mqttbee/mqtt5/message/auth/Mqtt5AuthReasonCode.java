package org.mqttbee.mqtt5.message.auth;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
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

    public int getCode() {
        return code;
    }


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

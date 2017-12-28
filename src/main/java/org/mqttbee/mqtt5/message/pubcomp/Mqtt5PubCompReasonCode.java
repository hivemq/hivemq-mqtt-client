package org.mqttbee.mqtt5.message.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5PubCompReasonCode {

    SUCCESS(Mqtt5ReasonCode.SUCCESS),
    PACKET_IDENTIFIER_NOT_FOUND(Mqtt5ReasonCode.PACKET_IDENTIFIER_NOT_FOUND);

    private final int code;

    Mqtt5PubCompReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubCompReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    public int getCode() {
        return code;
    }


    @Nullable
    public static Mqtt5PubCompReasonCode fromCode(final int code) {
        for (final Mqtt5PubCompReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}

package org.mqttbee.mqtt5.message.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5PubRecReasonCode {

    SUCCESS(Mqtt5ReasonCode.SUCCESS),
    NO_MATCHING_SUBSCRIBERS(Mqtt5ReasonCode.NO_MATCHING_SUBSCRIBERS),
    UNSPECIFIED_ERROR(Mqtt5ReasonCode.UNSPECIFIED_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(Mqtt5ReasonCode.NOT_AUTHORIZED),
    TOPIC_NAME_INVALID(Mqtt5ReasonCode.TOPIC_NAME_INVALID),
    PACKET_IDENTIFIER_IN_USE(Mqtt5ReasonCode.PACKET_IDENTIFIER_IN_USE),
    QUOTA_EXCEEDED(Mqtt5ReasonCode.QUOTA_EXCEEDED),
    PAYLOAD_FORMAT_INVALID(Mqtt5ReasonCode.PAYLOAD_FORMAT_INVALID);

    private final int code;

    Mqtt5PubRecReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubRecReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    public int getCode() {
        return code;
    }


    @Nullable
    public static Mqtt5PubRecReasonCode fromCode(final int code) {
        for (final Mqtt5PubRecReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}

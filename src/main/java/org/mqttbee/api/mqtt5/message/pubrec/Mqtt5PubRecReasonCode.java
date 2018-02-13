package org.mqttbee.api.mqtt5.message.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5CommonReasonCode;

/**
 * MQTT Reason Codes that can be used in PUBREC packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5PubRecReasonCode implements Mqtt5ReasonCode {

    SUCCESS(Mqtt5CommonReasonCode.SUCCESS),
    NO_MATCHING_SUBSCRIBERS(Mqtt5CommonReasonCode.NO_MATCHING_SUBSCRIBERS),
    UNSPECIFIED_ERROR(Mqtt5CommonReasonCode.UNSPECIFIED_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5CommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(Mqtt5CommonReasonCode.NOT_AUTHORIZED),
    TOPIC_NAME_INVALID(Mqtt5CommonReasonCode.TOPIC_NAME_INVALID),
    PACKET_IDENTIFIER_IN_USE(Mqtt5CommonReasonCode.PACKET_IDENTIFIER_IN_USE),
    QUOTA_EXCEEDED(Mqtt5CommonReasonCode.QUOTA_EXCEEDED),
    PAYLOAD_FORMAT_INVALID(Mqtt5CommonReasonCode.PAYLOAD_FORMAT_INVALID);

    private final int code;

    Mqtt5PubRecReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubRecReasonCode(@NotNull final Mqtt5CommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this PUBREC Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the PUBREC Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PUBREC Reason Code belonging to the given byte code or null if the byte code is not a valid PUBREC
     * Reason Code code.
     */
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

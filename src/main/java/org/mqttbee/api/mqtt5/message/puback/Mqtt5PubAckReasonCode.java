package org.mqttbee.api.mqtt5.message.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * MQTT Reason Codes that can be used in PUBACK packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5PubAckReasonCode {

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

    Mqtt5PubAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubAckReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this PUBACK Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the PUBACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PUBACK Reason Code belonging to the given byte code or null if the byte code is not a valid PUBACK
     * Reason Code code.
     */
    @Nullable
    public static Mqtt5PubAckReasonCode fromCode(final int code) {
        for (final Mqtt5PubAckReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}

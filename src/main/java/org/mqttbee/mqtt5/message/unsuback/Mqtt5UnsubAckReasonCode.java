package org.mqttbee.mqtt5.message.unsuback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * MQTT Reason Codes that can be used in UNSUBACK packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5UnsubAckReasonCode {

    SUCCESS(Mqtt5ReasonCode.SUCCESS),
    NO_SUBSCRIPTIONS_EXISTED(0x11),
    UNSPECIFIED_ERROR(Mqtt5ReasonCode.UNSPECIFIED_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(Mqtt5ReasonCode.NOT_AUTHORIZED),
    TOPIC_FILTER_INVALID(Mqtt5ReasonCode.TOPIC_FILTER_INVALID),
    PACKET_IDENTIFIER_IN_USE(Mqtt5ReasonCode.PACKET_IDENTIFIER_IN_USE);

    private final int code;

    Mqtt5UnsubAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5UnsubAckReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this UNSUBACK Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the UNSUBACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the UNSUBACK Reason Code belonging to the given byte code or null if the byte code is not a valid
     * UNSUBACK Reason Code code.
     */
    @Nullable
    public static Mqtt5UnsubAckReasonCode fromCode(final int code) {
        for (final Mqtt5UnsubAckReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}

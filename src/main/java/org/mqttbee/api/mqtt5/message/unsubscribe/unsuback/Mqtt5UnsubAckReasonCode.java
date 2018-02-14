package org.mqttbee.api.mqtt5.message.unsubscribe.unsuback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5CommonReasonCode;

/**
 * MQTT Reason Codes that can be used in UNSUBACK packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5UnsubAckReasonCode implements Mqtt5ReasonCode {

    SUCCESS(Mqtt5CommonReasonCode.SUCCESS),
    NO_SUBSCRIPTIONS_EXISTED(0x11),
    UNSPECIFIED_ERROR(Mqtt5CommonReasonCode.UNSPECIFIED_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5CommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(Mqtt5CommonReasonCode.NOT_AUTHORIZED),
    TOPIC_FILTER_INVALID(Mqtt5CommonReasonCode.TOPIC_FILTER_INVALID),
    PACKET_IDENTIFIER_IN_USE(Mqtt5CommonReasonCode.PACKET_IDENTIFIER_IN_USE);

    private final int code;

    Mqtt5UnsubAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5UnsubAckReasonCode(@NotNull final Mqtt5CommonReasonCode reasonCode) {
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

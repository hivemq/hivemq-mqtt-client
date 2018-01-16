package org.mqttbee.mqtt5.message.disconnect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * MQTT Reason Codes that can be used in DISCONNECT packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5DisconnectReasonCode {

    NORMAL_DISCONNECTION(0x00),
    DISCONNECT_WITH_WILL_MESSAGE(0x04),
    UNSPECIFIED_ERROR(Mqtt5ReasonCode.UNSPECIFIED_ERROR),
    MALFORMED_PACKET(Mqtt5ReasonCode.MALFORMED_PACKET),
    PROTOCOL_ERROR(Mqtt5ReasonCode.PROTOCOL_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(Mqtt5ReasonCode.NOT_AUTHORIZED),
    SERVER_BUSY(Mqtt5ReasonCode.SERVER_BUSY),
    SERVER_SHUTTING_DOWN(0x8B),
    BAD_AUTHENTICATION_METHOD(Mqtt5ReasonCode.BAD_AUTHENTICATION_METHOD),
    KEEP_ALIVE_TIMEOUT(0x8D),
    SESSION_TAKEN_OVER(0x8E),
    TOPIC_FILTER_INVALID(Mqtt5ReasonCode.TOPIC_FILTER_INVALID),
    TOPIC_NAME_INVALID(Mqtt5ReasonCode.TOPIC_NAME_INVALID),
    RECEIVE_MAXIMUM_EXCEEDED(0x93),
    TOPIC_ALIAS_INVALID(0x94),
    PACKET_TOO_LARGE(Mqtt5ReasonCode.PACKET_TOO_LARGE),
    MESSAGE_RATE_TOO_HIGH(0x96),
    QUOTA_EXCEEDED(Mqtt5ReasonCode.QUOTA_EXCEEDED),
    ADMINISTRATIVE_ACTION(0x98),
    PAYLOAD_FORMAT_INVALID(Mqtt5ReasonCode.PAYLOAD_FORMAT_INVALID),
    RETAIN_NOT_SUPPORTED(Mqtt5ReasonCode.RETAIN_NOT_SUPPORTED),
    QOS_NOT_SUPPORTED(Mqtt5ReasonCode.QOS_NOT_SUPPORTED),
    USE_ANOTHER_SERVER(Mqtt5ReasonCode.USE_ANOTHER_SERVER),
    SERVER_MOVED(Mqtt5ReasonCode.SERVER_MOVED),
    SHARED_SUBSCRIPTION_NOT_SUPPORTED(Mqtt5ReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED),
    CONNECTION_RATE_EXCEEDED(Mqtt5ReasonCode.CONNECTION_RATE_EXCEEDED),
    MAXIMUM_CONNECT_TIME(0xA0),
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(Mqtt5ReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED),
    WILDCARD_SUBSCRIPTION_NOT_SUPPORTED(Mqtt5ReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED);

    private final int code;

    Mqtt5DisconnectReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5DisconnectReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this DISCONNECT Reason Code.
     */
    public int getCode() {
        return code;
    }


    private static final int ERROR_CODE_MIN = UNSPECIFIED_ERROR.code;
    private static final int ERROR_CODE_MAX = WILDCARD_SUBSCRIPTION_NOT_SUPPORTED.code;
    private static final Mqtt5DisconnectReasonCode[] ERROR_CODE_LOOKUP =
            new Mqtt5DisconnectReasonCode[ERROR_CODE_MAX - ERROR_CODE_MIN + 1];

    static {
        for (final Mqtt5DisconnectReasonCode reasonCode : values()) {
            if (reasonCode != NORMAL_DISCONNECTION && reasonCode != DISCONNECT_WITH_WILL_MESSAGE) {
                ERROR_CODE_LOOKUP[reasonCode.code - ERROR_CODE_MIN] = reasonCode;
            }
        }
    }

    /**
     * Returns the DISCONNECT Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the DISCONNECT Reason Code belonging to the given byte code or null if the byte code is not a valid
     * DISCONNECT Reason Code code.
     */
    @Nullable
    public static Mqtt5DisconnectReasonCode fromCode(final int code) {
        if (code == NORMAL_DISCONNECTION.code) {
            return NORMAL_DISCONNECTION;
        }
        if (code == DISCONNECT_WITH_WILL_MESSAGE.code) {
            return DISCONNECT_WITH_WILL_MESSAGE;
        }
        if (code < ERROR_CODE_MIN || code > ERROR_CODE_MAX) {
            return null;
        }
        return ERROR_CODE_LOOKUP[code - ERROR_CODE_MIN];
    }

}

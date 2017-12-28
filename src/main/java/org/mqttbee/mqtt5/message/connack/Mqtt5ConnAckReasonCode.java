package org.mqttbee.mqtt5.message.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * @author Silvio Giebl
 */
public enum Mqtt5ConnAckReasonCode {

    SUCCESS(Mqtt5ReasonCode.SUCCESS),
    UNSPECIFIED_ERROR(Mqtt5ReasonCode.UNSPECIFIED_ERROR),
    MALFORMED_PACKET(Mqtt5ReasonCode.MALFORMED_PACKET),
    PROTOCOL_ERROR(Mqtt5ReasonCode.PROTOCOL_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    UNSUPPORTED_PROTOCOL_VERSION(0x84),
    CLIENT_IDENTIFIER_NOT_VALID(0x85),
    BAD_USER_NAME_OR_PASSWORD(0x86),
    NOT_AUTHORIZED(Mqtt5ReasonCode.NOT_AUTHORIZED),
    SERVER_UNAVAILABLE(0x88),
    SERVER_BUSY(Mqtt5ReasonCode.SERVER_BUSY),
    BANNED(0x8A),
    BAD_AUTHENTICATION_METHOD(Mqtt5ReasonCode.BAD_AUTHENTICATION_METHOD),
    TOPIC_NAME_INVALID(Mqtt5ReasonCode.TOPIC_NAME_INVALID),
    PACKET_TOO_LARGE(Mqtt5ReasonCode.PACKET_TOO_LARGE),
    QUOTA_EXCEEDED(Mqtt5ReasonCode.QUOTA_EXCEEDED),
    PAYLOAD_FORMAT_INVALID(Mqtt5ReasonCode.PAYLOAD_FORMAT_INVALID),
    RETAIN_NOT_SUPPORTED(Mqtt5ReasonCode.RETAIN_NOT_SUPPORTED),
    QOS_NOT_SUPPORTED(Mqtt5ReasonCode.QOS_NOT_SUPPORTED),
    USE_ANOTHER_SERVER(Mqtt5ReasonCode.USE_ANOTHER_SERVER),
    SERVER_MOVED(Mqtt5ReasonCode.SERVER_MOVED),
    CONNECTION_RATE_EXCEEDED(Mqtt5ReasonCode.CONNECTION_RATE_EXCEEDED);

    private final int code;

    Mqtt5ConnAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5ConnAckReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    public int getCode() {
        return code;
    }


    private static final int ERROR_CODE_MIN = UNSPECIFIED_ERROR.code;
    private static final int ERROR_CODE_MAX = CONNECTION_RATE_EXCEEDED.code;
    private static final Mqtt5ConnAckReasonCode[] ERROR_CODE_LOOKUP =
            new Mqtt5ConnAckReasonCode[ERROR_CODE_MAX - ERROR_CODE_MIN];

    static {
        for (final Mqtt5ConnAckReasonCode reasonCode : values()) {
            if (reasonCode != SUCCESS) {
                ERROR_CODE_LOOKUP[reasonCode.code - ERROR_CODE_MIN] = reasonCode;
            }
        }
    }

    @Nullable
    public static Mqtt5ConnAckReasonCode fromCode(final int code) {
        if (code == SUCCESS.code) {
            return SUCCESS;
        }
        if (code < ERROR_CODE_MIN || code > ERROR_CODE_MAX) {
            return null;
        }
        return ERROR_CODE_LOOKUP[code - ERROR_CODE_MIN];
    }

}

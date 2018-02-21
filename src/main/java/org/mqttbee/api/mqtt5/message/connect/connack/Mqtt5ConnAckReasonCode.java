package org.mqttbee.api.mqtt5.message.connect.connack;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt.message.MqttCommonReasonCode;

/**
 * MQTT Reason Codes that can be used in CONNACK packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5ConnAckReasonCode implements Mqtt5ReasonCode {

    SUCCESS(MqttCommonReasonCode.SUCCESS),
    UNSPECIFIED_ERROR(MqttCommonReasonCode.UNSPECIFIED_ERROR),
    MALFORMED_PACKET(MqttCommonReasonCode.MALFORMED_PACKET),
    PROTOCOL_ERROR(MqttCommonReasonCode.PROTOCOL_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(MqttCommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    UNSUPPORTED_PROTOCOL_VERSION(0x84),
    CLIENT_IDENTIFIER_NOT_VALID(0x85),
    BAD_USER_NAME_OR_PASSWORD(0x86),
    NOT_AUTHORIZED(MqttCommonReasonCode.NOT_AUTHORIZED),
    SERVER_UNAVAILABLE(0x88),
    SERVER_BUSY(MqttCommonReasonCode.SERVER_BUSY),
    BANNED(0x8A),
    BAD_AUTHENTICATION_METHOD(MqttCommonReasonCode.BAD_AUTHENTICATION_METHOD),
    TOPIC_NAME_INVALID(MqttCommonReasonCode.TOPIC_NAME_INVALID),
    PACKET_TOO_LARGE(MqttCommonReasonCode.PACKET_TOO_LARGE),
    QUOTA_EXCEEDED(MqttCommonReasonCode.QUOTA_EXCEEDED),
    PAYLOAD_FORMAT_INVALID(MqttCommonReasonCode.PAYLOAD_FORMAT_INVALID),
    RETAIN_NOT_SUPPORTED(MqttCommonReasonCode.RETAIN_NOT_SUPPORTED),
    QOS_NOT_SUPPORTED(MqttCommonReasonCode.QOS_NOT_SUPPORTED),
    USE_ANOTHER_SERVER(MqttCommonReasonCode.USE_ANOTHER_SERVER),
    SERVER_MOVED(MqttCommonReasonCode.SERVER_MOVED),
    CONNECTION_RATE_EXCEEDED(MqttCommonReasonCode.CONNECTION_RATE_EXCEEDED);

    private final int code;

    Mqtt5ConnAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5ConnAckReasonCode(@NotNull final MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this CONNACK Reason Code.
     */
    public int getCode() {
        return code;
    }


    private static final int ERROR_CODE_MIN = UNSPECIFIED_ERROR.code;
    private static final int ERROR_CODE_MAX = CONNECTION_RATE_EXCEEDED.code;
    private static final Mqtt5ConnAckReasonCode[] ERROR_CODE_LOOKUP =
            new Mqtt5ConnAckReasonCode[ERROR_CODE_MAX - ERROR_CODE_MIN + 1];

    static {
        for (final Mqtt5ConnAckReasonCode reasonCode : values()) {
            if (reasonCode != SUCCESS) {
                ERROR_CODE_LOOKUP[reasonCode.code - ERROR_CODE_MIN] = reasonCode;
            }
        }
    }

    /**
     * Returns the CONNACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the CONNACK Reason Code belonging to the given byte code or null if the byte code is not a valid CONNACK
     * Reason Code code.
     */
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

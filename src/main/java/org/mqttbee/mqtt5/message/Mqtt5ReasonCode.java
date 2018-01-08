package org.mqttbee.mqtt5.message;

/**
 * MQTT Reason Codes that are used in 2 ore more MQTT packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5ReasonCode {

    SUCCESS(0x00),
    NO_MATCHING_SUBSCRIBERS(0x10),
    UNSPECIFIED_ERROR(0x80),
    MALFORMED_PACKET(0x81),
    PROTOCOL_ERROR(0x82),
    IMPLEMENTATION_SPECIFIC_ERROR(0x83),
    NOT_AUTHORIZED(0x87),
    SERVER_BUSY(0x89),
    BAD_AUTHENTICATION_METHOD(0x8C),
    TOPIC_FILTER_INVALID(0x8F),
    TOPIC_NAME_INVALID(0x90),
    PACKET_IDENTIFIER_IN_USE(0x91),
    PACKET_IDENTIFIER_NOT_FOUND(0x92),
    PACKET_TOO_LARGE(0x95),
    QUOTA_EXCEEDED(0x97),
    PAYLOAD_FORMAT_INVALID(0x99),
    RETAIN_NOT_SUPPORTED(0x9A),
    QOS_NOT_SUPPORTED(0x9B),
    USE_ANOTHER_SERVER(0x9C),
    SERVER_MOVED(0x9D),
    SHARED_SUBSCRIPTION_NOT_SUPPORTED(0x9E),
    CONNECTION_RATE_EXCEEDED(0x9F),
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(0xA1),
    WILDCARD_SUBSCRIPTION_NOT_SUPPORTED(0xA2);

    private final int code;

    Mqtt5ReasonCode(final int code) {
        this.code = code;
    }

    /**
     * @return the byte code of this Reason Code.
     */
    public int getCode() {
        return code;
    }

}

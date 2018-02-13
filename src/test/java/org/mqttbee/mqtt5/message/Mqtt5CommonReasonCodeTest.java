package org.mqttbee.mqtt5.message;


import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Silvio Giebl
 */
class Mqtt5CommonReasonCodeTest {

    @Test
    void test_getCode_success() {
        assertEquals(0x00, Mqtt5CommonReasonCode.SUCCESS.getCode());
    }

    @Test
    void test_getCode_noMatchingSubscribers() {
        assertEquals(0x10, Mqtt5CommonReasonCode.NO_MATCHING_SUBSCRIBERS.getCode());
    }

    @Test
    void test_getCode_unspecifiedError() {
        assertEquals(0x80, Mqtt5CommonReasonCode.UNSPECIFIED_ERROR.getCode());
    }

    @Test
    void test_getCode_malformedPacket() {
        assertEquals(0x81, Mqtt5CommonReasonCode.MALFORMED_PACKET.getCode());
    }

    @Test
    void test_getCode_protocolError() {
        assertEquals(0x82, Mqtt5CommonReasonCode.PROTOCOL_ERROR.getCode());
    }

    @Test
    void test_getCode_implementationSpecificError() {
        assertEquals(0x83, Mqtt5CommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.getCode());
    }

    @Test
    void test_getCode_notAuthorized() {
        assertEquals(0x87, Mqtt5CommonReasonCode.NOT_AUTHORIZED.getCode());
    }

    @Test
    void test_getCode_SererBusy() {
        assertEquals(0x89, Mqtt5CommonReasonCode.SERVER_BUSY.getCode());
    }

    @Test
    void test_getCode_badAuthenticationMethod() {
        assertEquals(0x8C, Mqtt5CommonReasonCode.BAD_AUTHENTICATION_METHOD.getCode());
    }

    @Test
    void test_getCode_topicFilterInvalid() {
        assertEquals(0x8F, Mqtt5CommonReasonCode.TOPIC_FILTER_INVALID.getCode());
    }

    @Test
    void test_getCode_TopicNameInvalid() {
        assertEquals(0x90, Mqtt5CommonReasonCode.TOPIC_NAME_INVALID.getCode());
    }

    @Test
    void test_getCode_packetIdentifierInUse() {
        assertEquals(0x91, Mqtt5CommonReasonCode.PACKET_IDENTIFIER_IN_USE.getCode());
    }

    @Test
    void test_getCode_packetIdentifierNotFound() {
        assertEquals(0x92, Mqtt5CommonReasonCode.PACKET_IDENTIFIER_NOT_FOUND.getCode());
    }

    @Test
    void test_getCode_packetTooLarge() {
        assertEquals(0x95, Mqtt5CommonReasonCode.PACKET_TOO_LARGE.getCode());
    }

    @Test
    void test_getCode_quotaExceeded() {
        assertEquals(0x97, Mqtt5CommonReasonCode.QUOTA_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_payloadFormatInvalid() {
        assertEquals(0x99, Mqtt5CommonReasonCode.PAYLOAD_FORMAT_INVALID.getCode());
    }

    @Test
    void test_getCode_retainNotSupported() {
        assertEquals(0x9A, Mqtt5CommonReasonCode.RETAIN_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_qosNotSupported() {
        assertEquals(0x9B, Mqtt5CommonReasonCode.QOS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_useAnotherServer() {
        assertEquals(0x9C, Mqtt5CommonReasonCode.USE_ANOTHER_SERVER.getCode());
    }

    @Test
    void test_getCode_serverMoved() {
        assertEquals(0x9D, Mqtt5CommonReasonCode.SERVER_MOVED.getCode());
    }

    @Test
    void test_getCode_sharedSubscriptionNotSupported() {
        assertEquals(0x9E, Mqtt5CommonReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_connectionRateExceeded() {
        assertEquals(0x9F, Mqtt5CommonReasonCode.CONNECTION_RATE_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_subscriptionIdentifiersNotSupported() {
        assertEquals(0xA1, Mqtt5CommonReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_wildcardSubscriptionNotSupported() {
        assertEquals(0xA2, Mqtt5CommonReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

}

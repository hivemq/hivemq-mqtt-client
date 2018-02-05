package org.mqttbee.mqtt5.message;


import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Silvio Giebl
 */
class Mqtt5ReasonCodeTest {

    @Test
    void test_getCode_success() {
        assertEquals(0x00, Mqtt5ReasonCode.SUCCESS.getCode());
    }

    @Test
    void test_getCode_noMatchingSubscribers() {
        assertEquals(0x10, Mqtt5ReasonCode.NO_MATCHING_SUBSCRIBERS.getCode());
    }

    @Test
    void test_getCode_unspecifiedError() {
        assertEquals(0x80, Mqtt5ReasonCode.UNSPECIFIED_ERROR.getCode());
    }

    @Test
    void test_getCode_malformedPacket() {
        assertEquals(0x81, Mqtt5ReasonCode.MALFORMED_PACKET.getCode());
    }

    @Test
    void test_getCode_protocolError() {
        assertEquals(0x82, Mqtt5ReasonCode.PROTOCOL_ERROR.getCode());
    }

    @Test
    void test_getCode_implementationSpecificError() {
        assertEquals(0x83, Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.getCode());
    }

    @Test
    void test_getCode_notAuthorized() {
        assertEquals(0x87, Mqtt5ReasonCode.NOT_AUTHORIZED.getCode());
    }

    @Test
    void test_getCode_SererBusy() {
        assertEquals(0x89, Mqtt5ReasonCode.SERVER_BUSY.getCode());
    }

    @Test
    void test_getCode_badAuthenticationMethod() {
        assertEquals(0x8C, Mqtt5ReasonCode.BAD_AUTHENTICATION_METHOD.getCode());
    }

    @Test
    void test_getCode_topicFilterInvalid() {
        assertEquals(0x8F, Mqtt5ReasonCode.TOPIC_FILTER_INVALID.getCode());
    }

    @Test
    void test_getCode_TopicNameInvalid() {
        assertEquals(0x90, Mqtt5ReasonCode.TOPIC_NAME_INVALID.getCode());
    }

    @Test
    void test_getCode_packetIdentifierInUse() {
        assertEquals(0x91, Mqtt5ReasonCode.PACKET_IDENTIFIER_IN_USE.getCode());
    }

    @Test
    void test_getCode_packetIdentifierNotFound() {
        assertEquals(0x92, Mqtt5ReasonCode.PACKET_IDENTIFIER_NOT_FOUND.getCode());
    }

    @Test
    void test_getCode_packetTooLarge() {
        assertEquals(0x95, Mqtt5ReasonCode.PACKET_TOO_LARGE.getCode());
    }

    @Test
    void test_getCode_quotaExceeded() {
        assertEquals(0x97, Mqtt5ReasonCode.QUOTA_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_payloadFormatInvalid() {
        assertEquals(0x99, Mqtt5ReasonCode.PAYLOAD_FORMAT_INVALID.getCode());
    }

    @Test
    void test_getCode_retainNotSupported() {
        assertEquals(0x9A, Mqtt5ReasonCode.RETAIN_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_qosNotSupported() {
        assertEquals(0x9B, Mqtt5ReasonCode.QOS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_useAnotherServer() {
        assertEquals(0x9C, Mqtt5ReasonCode.USE_ANOTHER_SERVER.getCode());
    }

    @Test
    void test_getCode_serverMoved() {
        assertEquals(0x9D, Mqtt5ReasonCode.SERVER_MOVED.getCode());
    }

    @Test
    void test_getCode_sharedSubscriptionNotSupported() {
        assertEquals(0x9E, Mqtt5ReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_connectionRateExceeded() {
        assertEquals(0x9F, Mqtt5ReasonCode.CONNECTION_RATE_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_subscriptionIdentifiersNotSupported() {
        assertEquals(0xA1, Mqtt5ReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_wildcardSubscriptionNotSupported() {
        assertEquals(0xA2, Mqtt5ReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

}

package org.mqttbee.mqtt5.message;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ReasonCodeTest {

    @Test
    public void test_getCode_success() throws Exception {
        assertEquals(0x00, Mqtt5ReasonCode.SUCCESS.getCode());
    }

    @Test
    public void test_getCode_noMatchingSubscribers() throws Exception {
        assertEquals(0x10, Mqtt5ReasonCode.NO_MATCHING_SUBSCRIBERS.getCode());
    }

    @Test
    public void test_getCode_unspecifiedError() throws Exception {
        assertEquals(0x80, Mqtt5ReasonCode.UNSPECIFIED_ERROR.getCode());
    }

    @Test
    public void test_getCode_malformedPacket() throws Exception {
        assertEquals(0x81, Mqtt5ReasonCode.MALFORMED_PACKET.getCode());
    }

    @Test
    public void test_getCode_protocolError() throws Exception {
        assertEquals(0x82, Mqtt5ReasonCode.PROTOCOL_ERROR.getCode());
    }

    @Test
    public void test_getCode_implementationSpecificError() throws Exception {
        assertEquals(0x83, Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.getCode());
    }

    @Test
    public void test_getCode_notAuthorized() throws Exception {
        assertEquals(0x87, Mqtt5ReasonCode.NOT_AUTHORIZED.getCode());
    }

    @Test
    public void test_getCode_SererBusy() throws Exception {
        assertEquals(0x89, Mqtt5ReasonCode.SERVER_BUSY.getCode());
    }

    @Test
    public void test_getCode_badAuthenticationMethod() throws Exception {
        assertEquals(0x8C, Mqtt5ReasonCode.BAD_AUTHENTICATION_METHOD.getCode());
    }

    @Test
    public void test_getCode_topicFilterInvalid() throws Exception {
        assertEquals(0x8F, Mqtt5ReasonCode.TOPIC_FILTER_INVALID.getCode());
    }

    @Test
    public void test_getCode_TopicNameInvalid() throws Exception {
        assertEquals(0x90, Mqtt5ReasonCode.TOPIC_NAME_INVALID.getCode());
    }

    @Test
    public void test_getCode_packetIdentifierInUse() throws Exception {
        assertEquals(0x91, Mqtt5ReasonCode.PACKET_IDENTIFIER_IN_USE.getCode());
    }

    @Test
    public void test_getCode_packetIdentifierNotFound() throws Exception {
        assertEquals(0x92, Mqtt5ReasonCode.PACKET_IDENTIFIER_NOT_FOUND.getCode());
    }

    @Test
    public void test_getCode_packetTooLarge() throws Exception {
        assertEquals(0x95, Mqtt5ReasonCode.PACKET_TOO_LARGE.getCode());
    }

    @Test
    public void test_getCode_quotaExceeded() throws Exception {
        assertEquals(0x97, Mqtt5ReasonCode.QUOTA_EXCEEDED.getCode());
    }

    @Test
    public void test_getCode_payloadFormatInvalid() throws Exception {
        assertEquals(0x99, Mqtt5ReasonCode.PAYLOAD_FORMAT_INVALID.getCode());
    }

    @Test
    public void test_getCode_retainNotSupported() throws Exception {
        assertEquals(0x9A, Mqtt5ReasonCode.RETAIN_NOT_SUPPORTED.getCode());
    }

    @Test
    public void test_getCode_qosNotSupported() throws Exception {
        assertEquals(0x9B, Mqtt5ReasonCode.QOS_NOT_SUPPORTED.getCode());
    }

    @Test
    public void test_getCode_useAnotherServer() throws Exception {
        assertEquals(0x9C, Mqtt5ReasonCode.USE_ANOTHER_SERVER.getCode());
    }

    @Test
    public void test_getCode_serverMoved() throws Exception {
        assertEquals(0x9D, Mqtt5ReasonCode.SERVER_MOVED.getCode());
    }

    @Test
    public void test_getCode_sharedSubscriptionNotSupported() throws Exception {
        assertEquals(0x9E, Mqtt5ReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

    @Test
    public void test_getCode_connectionRateExceeded() throws Exception {
        assertEquals(0x9F, Mqtt5ReasonCode.CONNECTION_RATE_EXCEEDED.getCode());
    }

    @Test
    public void test_getCode_subscriptionIdentifiersNotSupported() throws Exception {
        assertEquals(0xA1, Mqtt5ReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.getCode());
    }

    @Test
    public void test_getCode_wildcardSubscriptionNotSupported() throws Exception {
        assertEquals(0xA2, Mqtt5ReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

}
/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client.internal.mqtt.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
class MqttCommonReasonCodeTest {

    @Test
    void test_getCode_success() {
        assertEquals(0x00, MqttCommonReasonCode.SUCCESS.getCode());
    }

    @Test
    void test_getCode_noMatchingSubscribers() {
        assertEquals(0x10, MqttCommonReasonCode.NO_MATCHING_SUBSCRIBERS.getCode());
    }

    @Test
    void test_getCode_unspecifiedError() {
        assertEquals(0x80, MqttCommonReasonCode.UNSPECIFIED_ERROR.getCode());
    }

    @Test
    void test_getCode_malformedPacket() {
        assertEquals(0x81, MqttCommonReasonCode.MALFORMED_PACKET.getCode());
    }

    @Test
    void test_getCode_protocolError() {
        assertEquals(0x82, MqttCommonReasonCode.PROTOCOL_ERROR.getCode());
    }

    @Test
    void test_getCode_implementationSpecificError() {
        assertEquals(0x83, MqttCommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.getCode());
    }

    @Test
    void test_getCode_notAuthorized() {
        assertEquals(0x87, MqttCommonReasonCode.NOT_AUTHORIZED.getCode());
    }

    @Test
    void test_getCode_SererBusy() {
        assertEquals(0x89, MqttCommonReasonCode.SERVER_BUSY.getCode());
    }

    @Test
    void test_getCode_badAuthenticationMethod() {
        assertEquals(0x8C, MqttCommonReasonCode.BAD_AUTHENTICATION_METHOD.getCode());
    }

    @Test
    void test_getCode_topicFilterInvalid() {
        assertEquals(0x8F, MqttCommonReasonCode.TOPIC_FILTER_INVALID.getCode());
    }

    @Test
    void test_getCode_TopicNameInvalid() {
        assertEquals(0x90, MqttCommonReasonCode.TOPIC_NAME_INVALID.getCode());
    }

    @Test
    void test_getCode_packetIdentifierInUse() {
        assertEquals(0x91, MqttCommonReasonCode.PACKET_IDENTIFIER_IN_USE.getCode());
    }

    @Test
    void test_getCode_packetIdentifierNotFound() {
        assertEquals(0x92, MqttCommonReasonCode.PACKET_IDENTIFIER_NOT_FOUND.getCode());
    }

    @Test
    void test_getCode_packetTooLarge() {
        assertEquals(0x95, MqttCommonReasonCode.PACKET_TOO_LARGE.getCode());
    }

    @Test
    void test_getCode_quotaExceeded() {
        assertEquals(0x97, MqttCommonReasonCode.QUOTA_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_payloadFormatInvalid() {
        assertEquals(0x99, MqttCommonReasonCode.PAYLOAD_FORMAT_INVALID.getCode());
    }

    @Test
    void test_getCode_retainNotSupported() {
        assertEquals(0x9A, MqttCommonReasonCode.RETAIN_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_qosNotSupported() {
        assertEquals(0x9B, MqttCommonReasonCode.QOS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_useAnotherServer() {
        assertEquals(0x9C, MqttCommonReasonCode.USE_ANOTHER_SERVER.getCode());
    }

    @Test
    void test_getCode_serverMoved() {
        assertEquals(0x9D, MqttCommonReasonCode.SERVER_MOVED.getCode());
    }

    @Test
    void test_getCode_sharedSubscriptionNotSupported() {
        assertEquals(0x9E, MqttCommonReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_connectionRateExceeded() {
        assertEquals(0x9F, MqttCommonReasonCode.CONNECTION_RATE_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_subscriptionIdentifiersNotSupported() {
        assertEquals(0xA1, MqttCommonReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_wildcardSubscriptionNotSupported() {
        assertEquals(0xA2, MqttCommonReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED.getCode());
    }

}

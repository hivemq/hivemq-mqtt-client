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

package com.hivemq.client2.internal.mqtt.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
class MqttReasonCodesTest {

    @Test
    void test_getCode_success() {
        assertEquals(0x00, MqttReasonCodes.SUCCESS);
    }

    @Test
    void test_getCode_grantedQos1() {
        assertEquals(0x01, MqttReasonCodes.GRANTED_QOS_1);
    }

    @Test
    void test_getCode_grantedQos2() {
        assertEquals(0x02, MqttReasonCodes.GRANTED_QOS_2);
    }

    @Test
    void test_getCode_disconnectWithWillMessage() {
        assertEquals(0x04, MqttReasonCodes.DISCONNECT_WITH_WILL_MESSAGE);
    }

    @Test
    void test_getCode_noMatchingSubscribers() {
        assertEquals(0x10, MqttReasonCodes.NO_MATCHING_SUBSCRIBERS);
    }

    @Test
    void test_getCode_noSubscriptionsExisted() {
        assertEquals(0x11, MqttReasonCodes.NO_SUBSCRIPTIONS_EXISTED);
    }

    @Test
    void test_getCode_continueAuthentication() {
        assertEquals(0x18, MqttReasonCodes.CONTINUE_AUTHENTICATION);
    }

    @Test
    void test_getCode_reauthenticate() {
        assertEquals(0x19, MqttReasonCodes.REAUTHENTICATE);
    }

    @Test
    void test_getCode_unspecifiedError() {
        assertEquals(0x80, MqttReasonCodes.UNSPECIFIED_ERROR);
    }

    @Test
    void test_getCode_malformedPacket() {
        assertEquals(0x81, MqttReasonCodes.MALFORMED_PACKET);
    }

    @Test
    void test_getCode_protocolError() {
        assertEquals(0x82, MqttReasonCodes.PROTOCOL_ERROR);
    }

    @Test
    void test_getCode_implementationSpecificError() {
        assertEquals(0x83, MqttReasonCodes.IMPLEMENTATION_SPECIFIC_ERROR);
    }

    @Test
    void test_getCode_unsupportedProtocolVersion() {
        assertEquals(0x84, MqttReasonCodes.UNSUPPORTED_PROTOCOL_VERSION);
    }

    @Test
    void test_getCode_clientIdentifierNotValid() {
        assertEquals(0x85, MqttReasonCodes.CLIENT_IDENTIFIER_NOT_VALID);
    }

    @Test
    void test_getCode_badUsernameOrPassword() {
        assertEquals(0x86, MqttReasonCodes.BAD_USER_NAME_OR_PASSWORD);
    }

    @Test
    void test_getCode_notAuthorized() {
        assertEquals(0x87, MqttReasonCodes.NOT_AUTHORIZED);
    }

    @Test
    void test_getCode_serverUnavailable() {
        assertEquals(0x88, MqttReasonCodes.SERVER_UNAVAILABLE);
    }

    @Test
    void test_getCode_SererBusy() {
        assertEquals(0x89, MqttReasonCodes.SERVER_BUSY);
    }

    @Test
    void test_getCode_banned() {
        assertEquals(0x8A, MqttReasonCodes.BANNED);
    }

    @Test
    void test_getCode_badAuthenticationMethod() {
        assertEquals(0x8C, MqttReasonCodes.BAD_AUTHENTICATION_METHOD);
    }

    @Test
    void test_getCode_keepAliveTimeout() {
        assertEquals(0x8D, MqttReasonCodes.KEEP_ALIVE_TIMEOUT);
    }

    @Test
    void test_getCode_sessionTakenOver() {
        assertEquals(0x8E, MqttReasonCodes.SESSION_TAKEN_OVER);
    }

    @Test
    void test_getCode_topicFilterInvalid() {
        assertEquals(0x8F, MqttReasonCodes.TOPIC_FILTER_INVALID);
    }

    @Test
    void test_getCode_TopicNameInvalid() {
        assertEquals(0x90, MqttReasonCodes.TOPIC_NAME_INVALID);
    }

    @Test
    void test_getCode_packetIdentifierInUse() {
        assertEquals(0x91, MqttReasonCodes.PACKET_IDENTIFIER_IN_USE);
    }

    @Test
    void test_getCode_packetIdentifierNotFound() {
        assertEquals(0x92, MqttReasonCodes.PACKET_IDENTIFIER_NOT_FOUND);
    }

    @Test
    void test_getCode_receiveMaximumExceeded() {
        assertEquals(0x93, MqttReasonCodes.RECEIVE_MAXIMUM_EXCEEDED);
    }

    @Test
    void test_getCode_topicAliasInvalid() {
        assertEquals(0x94, MqttReasonCodes.TOPIC_ALIAS_INVALID);
    }

    @Test
    void test_getCode_packetTooLarge() {
        assertEquals(0x95, MqttReasonCodes.PACKET_TOO_LARGE);
    }

    @Test
    void test_getCode_messageRateTooHigh() {
        assertEquals(0x96, MqttReasonCodes.MESSAGE_RATE_TOO_HIGH);
    }

    @Test
    void test_getCode_quotaExceeded() {
        assertEquals(0x97, MqttReasonCodes.QUOTA_EXCEEDED);
    }

    @Test
    void test_getCode_administrativeAction() {
        assertEquals(0x98, MqttReasonCodes.ADMINISTRATIVE_ACTION);
    }

    @Test
    void test_getCode_payloadFormatInvalid() {
        assertEquals(0x99, MqttReasonCodes.PAYLOAD_FORMAT_INVALID);
    }

    @Test
    void test_getCode_retainNotSupported() {
        assertEquals(0x9A, MqttReasonCodes.RETAIN_NOT_SUPPORTED);
    }

    @Test
    void test_getCode_qosNotSupported() {
        assertEquals(0x9B, MqttReasonCodes.QOS_NOT_SUPPORTED);
    }

    @Test
    void test_getCode_useAnotherServer() {
        assertEquals(0x9C, MqttReasonCodes.USE_ANOTHER_SERVER);
    }

    @Test
    void test_getCode_serverMoved() {
        assertEquals(0x9D, MqttReasonCodes.SERVER_MOVED);
    }

    @Test
    void test_getCode_sharedSubscriptionNotSupported() {
        assertEquals(0x9E, MqttReasonCodes.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED);
    }

    @Test
    void test_getCode_connectionRateExceeded() {
        assertEquals(0x9F, MqttReasonCodes.CONNECTION_RATE_EXCEEDED);
    }

    @Test
    void test_getCode_maximumConnectTime() {
        assertEquals(0xA0, MqttReasonCodes.MAXIMUM_CONNECT_TIME);
    }

    @Test
    void test_getCode_subscriptionIdentifiersNotSupported() {
        assertEquals(0xA1, MqttReasonCodes.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED);
    }

    @Test
    void test_getCode_wildcardSubscriptionNotSupported() {
        assertEquals(0xA2, MqttReasonCodes.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);
    }

}

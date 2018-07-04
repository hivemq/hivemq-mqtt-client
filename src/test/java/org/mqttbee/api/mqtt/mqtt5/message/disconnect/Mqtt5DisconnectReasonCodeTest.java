/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.api.mqtt.mqtt5.message.disconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** @author David Katz */
class Mqtt5DisconnectReasonCodeTest {

    @Test
    void test_getCode_normalDisconnection() {
        assertEquals(0x00, Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION.getCode());
    }

    @Test
    void test_getCode_disconnectWithWill() {
        assertEquals(0x04, Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE.getCode());
    }

    @Test
    void test_getCode_unspecifiedError() {
        assertEquals(0x80, Mqtt5DisconnectReasonCode.UNSPECIFIED_ERROR.getCode());
    }

    @Test
    void test_getCode_malformedPacket() {
        assertEquals(0x81, Mqtt5DisconnectReasonCode.MALFORMED_PACKET.getCode());
    }

    @Test
    void test_getCode_protocolError() {
        assertEquals(0x82, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR.getCode());
    }

    @Test
    void test_getCode_implementationSpecificError() {
        assertEquals(0x83, Mqtt5DisconnectReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.getCode());
    }

    @Test
    void test_getCode_notAuthorized() {
        assertEquals(0x87, Mqtt5DisconnectReasonCode.NOT_AUTHORIZED.getCode());
    }

    @Test
    void test_getCode_ServerBusy() {
        assertEquals(0x89, Mqtt5DisconnectReasonCode.SERVER_BUSY.getCode());
    }

    @Test
    void test_getCode_ServerShuttingDown() {
        assertEquals(0x8B, Mqtt5DisconnectReasonCode.SERVER_SHUTTING_DOWN.getCode());
    }

    @Test
    void test_getCode_badAuthenticationMethod() {
        assertEquals(0x8C, Mqtt5DisconnectReasonCode.BAD_AUTHENTICATION_METHOD.getCode());
    }

    @Test
    void test_getCode_keepAliveTimeout() {
        assertEquals(0x8D, Mqtt5DisconnectReasonCode.KEEP_ALIVE_TIMEOUT.getCode());
    }

    @Test
    void test_getCode_sessionTakenOver() {
        assertEquals(0x8E, Mqtt5DisconnectReasonCode.SESSION_TAKEN_OVER.getCode());
    }

    @Test
    void test_getCode_topicFilterInvalid() {
        assertEquals(0x8F, Mqtt5DisconnectReasonCode.TOPIC_FILTER_INVALID.getCode());
    }

    @Test
    void test_getCode_TopicNameInvalid() {
        assertEquals(0x90, Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID.getCode());
    }

    @Test
    void test_getCode_receiveMaximumExceeded() {
        assertEquals(0x93, Mqtt5DisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_topicAliasInvalid() {
        assertEquals(0x94, Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID.getCode());
    }

    @Test
    void test_getCode_packetTooLarge() {
        assertEquals(0x95, Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE.getCode());
    }

    @Test
    void test_getCode_messageRateTooHigh() {
        assertEquals(0x96, Mqtt5DisconnectReasonCode.MESSAGE_RATE_TOO_HIGH.getCode());
    }

    @Test
    void test_getCode_quotaExceeded() {
        assertEquals(0x97, Mqtt5DisconnectReasonCode.QUOTA_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_administrativeAction() {
        assertEquals(0x98, Mqtt5DisconnectReasonCode.ADMINISTRATIVE_ACTION.getCode());
    }

    @Test
    void test_getCode_payloadFormatInvalid() {
        assertEquals(0x99, Mqtt5DisconnectReasonCode.PAYLOAD_FORMAT_INVALID.getCode());
    }

    @Test
    void test_getCode_retainNotSupported() {
        assertEquals(0x9A, Mqtt5DisconnectReasonCode.RETAIN_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_qosNotSupported() {
        assertEquals(0x9B, Mqtt5DisconnectReasonCode.QOS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_useAnotherServer() {
        assertEquals(0x9C, Mqtt5DisconnectReasonCode.USE_ANOTHER_SERVER.getCode());
    }

    @Test
    void test_getCode_serverMoved() {
        assertEquals(0x9D, Mqtt5DisconnectReasonCode.SERVER_MOVED.getCode());
    }

    @Test
    void test_getCode_sharedSubscriptionNotSupported() {
        assertEquals(0x9E, Mqtt5DisconnectReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_connectionRateExceeded() {
        assertEquals(0x9F, Mqtt5DisconnectReasonCode.CONNECTION_RATE_EXCEEDED.getCode());
    }

    @Test
    void test_getCode_maximumConnectTime() {
        assertEquals(0xA0, Mqtt5DisconnectReasonCode.MAXIMUM_CONNECT_TIME.getCode());
    }

    @Test
    void test_getCode_subscriptionIdentifiersNotSupported() {
        assertEquals(
                0xA1, Mqtt5DisconnectReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.getCode());
    }

    @Test
    void test_getCode_wildcardSubscriptionNotSupported() {
        assertEquals(0xA2, Mqtt5DisconnectReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED.getCode());
    }

    @ParameterizedTest
    @EnumSource(Mqtt5DisconnectReasonCode.class)
    void test_fromCode(final Mqtt5DisconnectReasonCode reasonCode) {
        final Mqtt5DisconnectReasonCode mqtt5DisconnectReasonCode =
                Mqtt5DisconnectReasonCode.fromCode(reasonCode.getCode());
        assertEquals(reasonCode, mqtt5DisconnectReasonCode);
    }

    @Test
    void test_invalidReasonCodes() {
        assertNull(Mqtt5DisconnectReasonCode.fromCode(0x03));
        assertNull(Mqtt5DisconnectReasonCode.fromCode(0xFF));
    }
}

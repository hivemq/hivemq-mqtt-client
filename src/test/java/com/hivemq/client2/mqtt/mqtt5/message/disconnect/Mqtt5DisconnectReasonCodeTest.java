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

package com.hivemq.client2.mqtt.mqtt5.message.disconnect;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
class Mqtt5DisconnectReasonCodeTest {

    @Test
    void getCode_normalDisconnection() {
        assertEquals(0x00, Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION.getCode());
    }

    @Test
    void getCode_disconnectWithWill() {
        assertEquals(0x04, Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE.getCode());
    }

    @Test
    void getCode_unspecifiedError() {
        assertEquals(0x80, Mqtt5DisconnectReasonCode.UNSPECIFIED_ERROR.getCode());
    }

    @Test
    void getCode_malformedPacket() {
        assertEquals(0x81, Mqtt5DisconnectReasonCode.MALFORMED_PACKET.getCode());
    }

    @Test
    void getCode_protocolError() {
        assertEquals(0x82, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR.getCode());
    }

    @Test
    void getCode_implementationSpecificError() {
        assertEquals(0x83, Mqtt5DisconnectReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.getCode());
    }

    @Test
    void getCode_notAuthorized() {
        assertEquals(0x87, Mqtt5DisconnectReasonCode.NOT_AUTHORIZED.getCode());
    }

    @Test
    void getCode_serverBusy() {
        assertEquals(0x89, Mqtt5DisconnectReasonCode.SERVER_BUSY.getCode());
    }

    @Test
    void getCode_serverShuttingDown() {
        assertEquals(0x8B, Mqtt5DisconnectReasonCode.SERVER_SHUTTING_DOWN.getCode());
    }

    @Test
    void getCode_badAuthenticationMethod() {
        assertEquals(0x8C, Mqtt5DisconnectReasonCode.BAD_AUTHENTICATION_METHOD.getCode());
    }

    @Test
    void getCode_keepAliveTimeout() {
        assertEquals(0x8D, Mqtt5DisconnectReasonCode.KEEP_ALIVE_TIMEOUT.getCode());
    }

    @Test
    void getCode_sessionTakenOver() {
        assertEquals(0x8E, Mqtt5DisconnectReasonCode.SESSION_TAKEN_OVER.getCode());
    }

    @Test
    void getCode_topicFilterInvalid() {
        assertEquals(0x8F, Mqtt5DisconnectReasonCode.TOPIC_FILTER_INVALID.getCode());
    }

    @Test
    void getCode_topicNameInvalid() {
        assertEquals(0x90, Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID.getCode());
    }

    @Test
    void getCode_receiveMaximumExceeded() {
        assertEquals(0x93, Mqtt5DisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED.getCode());
    }

    @Test
    void getCode_topicAliasInvalid() {
        assertEquals(0x94, Mqtt5DisconnectReasonCode.TOPIC_ALIAS_INVALID.getCode());
    }

    @Test
    void getCode_packetTooLarge() {
        assertEquals(0x95, Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE.getCode());
    }

    @Test
    void getCode_messageRateTooHigh() {
        assertEquals(0x96, Mqtt5DisconnectReasonCode.MESSAGE_RATE_TOO_HIGH.getCode());
    }

    @Test
    void getCode_quotaExceeded() {
        assertEquals(0x97, Mqtt5DisconnectReasonCode.QUOTA_EXCEEDED.getCode());
    }

    @Test
    void getCode_administrativeAction() {
        assertEquals(0x98, Mqtt5DisconnectReasonCode.ADMINISTRATIVE_ACTION.getCode());
    }

    @Test
    void getCode_payloadFormatInvalid() {
        assertEquals(0x99, Mqtt5DisconnectReasonCode.PAYLOAD_FORMAT_INVALID.getCode());
    }

    @Test
    void getCode_retainNotSupported() {
        assertEquals(0x9A, Mqtt5DisconnectReasonCode.RETAIN_NOT_SUPPORTED.getCode());
    }

    @Test
    void getCode_qosNotSupported() {
        assertEquals(0x9B, Mqtt5DisconnectReasonCode.QOS_NOT_SUPPORTED.getCode());
    }

    @Test
    void getCode_useAnotherServer() {
        assertEquals(0x9C, Mqtt5DisconnectReasonCode.USE_ANOTHER_SERVER.getCode());
    }

    @Test
    void getCode_serverMoved() {
        assertEquals(0x9D, Mqtt5DisconnectReasonCode.SERVER_MOVED.getCode());
    }

    @Test
    void getCode_sharedSubscriptionNotSupported() {
        assertEquals(0x9E, Mqtt5DisconnectReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED.getCode());
    }

    @Test
    void getCode_connectionRateExceeded() {
        assertEquals(0x9F, Mqtt5DisconnectReasonCode.CONNECTION_RATE_EXCEEDED.getCode());
    }

    @Test
    void getCode_maximumConnectTime() {
        assertEquals(0xA0, Mqtt5DisconnectReasonCode.MAXIMUM_CONNECT_TIME.getCode());
    }

    @Test
    void getCode_subscriptionIdentifiersNotSupported() {
        assertEquals(0xA1, Mqtt5DisconnectReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.getCode());
    }

    @Test
    void getCode_wildcardSubscriptionNotSupported() {
        assertEquals(0xA2, Mqtt5DisconnectReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED.getCode());
    }

    @ParameterizedTest
    @EnumSource(Mqtt5DisconnectReasonCode.class)
    void fromCode(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertEquals(reasonCode, Mqtt5DisconnectReasonCode.fromCode(reasonCode.getCode()));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5DisconnectReasonCode.fromCode(0x03));
        assertNull(Mqtt5DisconnectReasonCode.fromCode(0xFF));
        assertNull(Mqtt5DisconnectReasonCode.fromCode(-1));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class,
            names = {"NORMAL_DISCONNECTION", "DISCONNECT_WITH_WILL_MESSAGE"})
    void isError_false(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertFalse(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {
            "UNSPECIFIED_ERROR", "MALFORMED_PACKET", "PROTOCOL_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "SERVER_BUSY", "SERVER_SHUTTING_DOWN", "BAD_AUTHENTICATION_METHOD", "KEEP_ALIVE_TIMEOUT",
            "SESSION_TAKEN_OVER", "TOPIC_FILTER_INVALID", "TOPIC_NAME_INVALID", "RECEIVE_MAXIMUM_EXCEEDED",
            "TOPIC_ALIAS_INVALID", "PACKET_TOO_LARGE", "MESSAGE_RATE_TOO_HIGH", "QUOTA_EXCEEDED",
            "ADMINISTRATIVE_ACTION", "PAYLOAD_FORMAT_INVALID", "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED",
            "USE_ANOTHER_SERVER", "SERVER_MOVED", "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "CONNECTION_RATE_EXCEEDED",
            "MAXIMUM_CONNECT_TIME", "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED", "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void isError_true(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertTrue(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {
            "NORMAL_DISCONNECTION", "UNSPECIFIED_ERROR", "MALFORMED_PACKET", "PROTOCOL_ERROR",
            "IMPLEMENTATION_SPECIFIC_ERROR", "NOT_AUTHORIZED", "SERVER_BUSY", "SERVER_SHUTTING_DOWN",
            "BAD_AUTHENTICATION_METHOD", "KEEP_ALIVE_TIMEOUT", "SESSION_TAKEN_OVER", "TOPIC_FILTER_INVALID",
            "TOPIC_NAME_INVALID", "RECEIVE_MAXIMUM_EXCEEDED", "TOPIC_ALIAS_INVALID", "PACKET_TOO_LARGE",
            "MESSAGE_RATE_TOO_HIGH", "QUOTA_EXCEEDED", "ADMINISTRATIVE_ACTION", "PAYLOAD_FORMAT_INVALID",
            "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED", "USE_ANOTHER_SERVER", "SERVER_MOVED",
            "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "CONNECTION_RATE_EXCEEDED", "MAXIMUM_CONNECT_TIME",
            "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED", "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void canBeSentByServer_true(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {"DISCONNECT_WITH_WILL_MESSAGE"})
    void canBeSentByServer_false(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {
            "NORMAL_DISCONNECTION", "DISCONNECT_WITH_WILL_MESSAGE", "UNSPECIFIED_ERROR", "MALFORMED_PACKET",
            "PROTOCOL_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR", "BAD_AUTHENTICATION_METHOD", "TOPIC_NAME_INVALID",
            "RECEIVE_MAXIMUM_EXCEEDED", "TOPIC_ALIAS_INVALID", "PACKET_TOO_LARGE", "MESSAGE_RATE_TOO_HIGH",
            "QUOTA_EXCEEDED", "ADMINISTRATIVE_ACTION", "PAYLOAD_FORMAT_INVALID"
    })
    void canBeSentByClient_true(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {
            "NOT_AUTHORIZED", "SERVER_BUSY", "SERVER_SHUTTING_DOWN", "KEEP_ALIVE_TIMEOUT", "TOPIC_FILTER_INVALID",
            "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED", "USE_ANOTHER_SERVER", "SERVER_MOVED",
            "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "CONNECTION_RATE_EXCEEDED", "MAXIMUM_CONNECT_TIME",
            "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED", "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void canBeSentByClient_false(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {
            "NORMAL_DISCONNECTION", "DISCONNECT_WITH_WILL_MESSAGE", "UNSPECIFIED_ERROR",
            "IMPLEMENTATION_SPECIFIC_ERROR", "TOPIC_NAME_INVALID", "MESSAGE_RATE_TOO_HIGH", "QUOTA_EXCEEDED",
            "ADMINISTRATIVE_ACTION", "PAYLOAD_FORMAT_INVALID"
    })
    void canBeSetByUser_true(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSetByUser());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5DisconnectReasonCode.class, names = {
            "MALFORMED_PACKET", "PROTOCOL_ERROR", "NOT_AUTHORIZED", "SERVER_BUSY", "SERVER_SHUTTING_DOWN",
            "BAD_AUTHENTICATION_METHOD", "KEEP_ALIVE_TIMEOUT", "TOPIC_FILTER_INVALID", "RECEIVE_MAXIMUM_EXCEEDED",
            "TOPIC_ALIAS_INVALID", "PACKET_TOO_LARGE", "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED",
            "USE_ANOTHER_SERVER", "SERVER_MOVED", "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "CONNECTION_RATE_EXCEEDED",
            "MAXIMUM_CONNECT_TIME", "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED", "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void canBeSetByUser_false(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSetByUser());
    }
}
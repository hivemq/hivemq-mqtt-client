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

package com.hivemq.client2.mqtt.mqtt5.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt5SubAckReasonCodeTest {

    @ParameterizedTest
    @MethodSource("provideCodes")
    void getCode(final @NotNull Mqtt5SubAckReasonCode reasonCode, final int code) {
        assertEquals(code, reasonCode.getCode());
    }

    @ParameterizedTest
    @MethodSource("provideCodes")
    void fromCode(final @NotNull Mqtt5SubAckReasonCode reasonCode, final int code) {
        assertEquals(reasonCode, Mqtt5SubAckReasonCode.fromCode(code));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5SubAckReasonCode.fromCode(0xFF));
        assertNull(Mqtt5SubAckReasonCode.fromCode(-1));
    }

    private static @NotNull Stream<Arguments> provideCodes() {
        return Stream.of(
                Arguments.of(Mqtt5SubAckReasonCode.GRANTED_QOS_0, 0x00),
                Arguments.of(Mqtt5SubAckReasonCode.GRANTED_QOS_1, 0x01),
                Arguments.of(Mqtt5SubAckReasonCode.GRANTED_QOS_2, 0x02),
                Arguments.of(Mqtt5SubAckReasonCode.UNSPECIFIED_ERROR, 0x80),
                Arguments.of(Mqtt5SubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, 0x83),
                Arguments.of(Mqtt5SubAckReasonCode.NOT_AUTHORIZED, 0x87),
                Arguments.of(Mqtt5SubAckReasonCode.TOPIC_FILTER_INVALID, 0x8F),
                Arguments.of(Mqtt5SubAckReasonCode.PACKET_IDENTIFIER_IN_USE, 0x91),
                Arguments.of(Mqtt5SubAckReasonCode.QUOTA_EXCEEDED, 0x97),
                Arguments.of(Mqtt5SubAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED, 0x9E),
                Arguments.of(Mqtt5SubAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED, 0xA1),
                Arguments.of(Mqtt5SubAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED, 0x0A2));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5SubAckReasonCode.class, names = {
            "GRANTED_QOS_0", "GRANTED_QOS_1", "GRANTED_QOS_2"
    })
    void isError_false(final @NotNull Mqtt5SubAckReasonCode reasonCode) {
        assertFalse(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5SubAckReasonCode.class, names = {
            "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR", "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID",
            "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED", "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED",
            "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED", "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void isError_true(final @NotNull Mqtt5SubAckReasonCode reasonCode) {
        assertTrue(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5SubAckReasonCode.class, names = {
            "GRANTED_QOS_0", "GRANTED_QOS_1", "GRANTED_QOS_2", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID", "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED",
            "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED",
            "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void canBeSentByServer_true(final @NotNull Mqtt5SubAckReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5SubAckReasonCode.class, names = {
            "GRANTED_QOS_0", "GRANTED_QOS_1", "GRANTED_QOS_2", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID", "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED",
            "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED",
            "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void canBeSentByClient_false(final @NotNull Mqtt5SubAckReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5SubAckReasonCode.class, names = {
            "GRANTED_QOS_0", "GRANTED_QOS_1", "GRANTED_QOS_2", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID", "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED",
            "SHARED_SUBSCRIPTIONS_NOT_SUPPORTED", "SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED",
            "WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED"
    })
    void canBeSetByUser_false(final @NotNull Mqtt5SubAckReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSetByUser());
    }
}
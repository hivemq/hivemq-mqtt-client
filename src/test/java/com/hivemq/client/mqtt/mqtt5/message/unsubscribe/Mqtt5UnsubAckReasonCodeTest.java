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

package com.hivemq.client.mqtt.mqtt5.message.unsubscribe;

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
class Mqtt5UnsubAckReasonCodeTest {

    @ParameterizedTest
    @MethodSource("provideCodes")
    void getCode(final @NotNull Mqtt5UnsubAckReasonCode reasonCode, final int code) {
        assertEquals(code, reasonCode.getCode());
    }

    @ParameterizedTest
    @MethodSource("provideCodes")
    void fromCode(final @NotNull Mqtt5UnsubAckReasonCode reasonCode, final int code) {
        assertEquals(reasonCode, Mqtt5UnsubAckReasonCode.fromCode(code));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5UnsubAckReasonCode.fromCode(0xFF));
        assertNull(Mqtt5UnsubAckReasonCode.fromCode(-1));
    }

    private static @NotNull Stream<Arguments> provideCodes() {
        return Stream.of(
                Arguments.of(Mqtt5UnsubAckReasonCode.SUCCESS, 0x00),
                Arguments.of(Mqtt5UnsubAckReasonCode.NO_SUBSCRIPTIONS_EXISTED, 0x11),
                Arguments.of(Mqtt5UnsubAckReasonCode.UNSPECIFIED_ERROR, 0x80),
                Arguments.of(Mqtt5UnsubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, 0x83),
                Arguments.of(Mqtt5UnsubAckReasonCode.NOT_AUTHORIZED, 0x87),
                Arguments.of(Mqtt5UnsubAckReasonCode.TOPIC_FILTER_INVALID, 0x8F),
                Arguments.of(Mqtt5UnsubAckReasonCode.PACKET_IDENTIFIER_IN_USE, 0x91));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5UnsubAckReasonCode.class, names = {"SUCCESS", "NO_SUBSCRIPTIONS_EXISTED"})
    void isError_false(final @NotNull Mqtt5UnsubAckReasonCode reasonCode) {
        assertFalse(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5UnsubAckReasonCode.class, names = {
            "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR", "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID",
            "PACKET_IDENTIFIER_IN_USE"
    })
    void isError_true(final @NotNull Mqtt5UnsubAckReasonCode reasonCode) {
        assertTrue(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5UnsubAckReasonCode.class, names = {
            "SUCCESS", "NO_SUBSCRIPTIONS_EXISTED", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID", "PACKET_IDENTIFIER_IN_USE"
    })
    void canBeSentByServer_true(final @NotNull Mqtt5UnsubAckReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5UnsubAckReasonCode.class, names = {
            "SUCCESS", "NO_SUBSCRIPTIONS_EXISTED", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID", "PACKET_IDENTIFIER_IN_USE"
    })
    void canBeSentByClient_false(final @NotNull Mqtt5UnsubAckReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5UnsubAckReasonCode.class, names = {
            "SUCCESS", "NO_SUBSCRIPTIONS_EXISTED", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_FILTER_INVALID", "PACKET_IDENTIFIER_IN_USE"
    })
    void canBeSetByUser_false(final @NotNull Mqtt5UnsubAckReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSetByUser());
    }
}
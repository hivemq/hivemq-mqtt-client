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

package com.hivemq.client.mqtt.mqtt5.message.publish;

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
class Mqtt5PubRecReasonCodeTest {

    @ParameterizedTest
    @MethodSource("provideCodes")
    void getCode(final @NotNull Mqtt5PubRecReasonCode reasonCode, final int code) {
        assertEquals(code, reasonCode.getCode());
    }

    @ParameterizedTest
    @MethodSource("provideCodes")
    void fromCode(final @NotNull Mqtt5PubRecReasonCode reasonCode, final int code) {
        assertEquals(reasonCode, Mqtt5PubRecReasonCode.fromCode(code));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5PubRecReasonCode.fromCode(0xFF));
        assertNull(Mqtt5PubRecReasonCode.fromCode(-1));
    }

    private static @NotNull Stream<Arguments> provideCodes() {
        return Stream.of(
                Arguments.of(Mqtt5PubRecReasonCode.SUCCESS, 0x00),
                Arguments.of(Mqtt5PubRecReasonCode.NO_MATCHING_SUBSCRIBERS, 0x10),
                Arguments.of(Mqtt5PubRecReasonCode.UNSPECIFIED_ERROR, 0x80),
                Arguments.of(Mqtt5PubRecReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, 0x83),
                Arguments.of(Mqtt5PubRecReasonCode.NOT_AUTHORIZED, 0x87),
                Arguments.of(Mqtt5PubRecReasonCode.TOPIC_NAME_INVALID, 0x90),
                Arguments.of(Mqtt5PubRecReasonCode.PACKET_IDENTIFIER_IN_USE, 0x91),
                Arguments.of(Mqtt5PubRecReasonCode.QUOTA_EXCEEDED, 0x97),
                Arguments.of(Mqtt5PubRecReasonCode.PAYLOAD_FORMAT_INVALID, 0x99));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {"SUCCESS", "NO_MATCHING_SUBSCRIBERS"})
    void isError_false(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertFalse(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {
            "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR", "NOT_AUTHORIZED", "TOPIC_NAME_INVALID",
            "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID"
    })
    void isError_true(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertTrue(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {
            "SUCCESS", "NO_MATCHING_SUBSCRIBERS", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "NOT_AUTHORIZED", "TOPIC_NAME_INVALID", "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED",
            "PAYLOAD_FORMAT_INVALID"
    })
    void canBeSentByServer_true(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {
            "SUCCESS", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR", "NOT_AUTHORIZED", "TOPIC_NAME_INVALID",
            "PACKET_IDENTIFIER_IN_USE", "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID"
    })
    void canBeSentByClient_true(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {"NO_MATCHING_SUBSCRIBERS"})
    void canBeSentByClient_false(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {
            "SUCCESS", "UNSPECIFIED_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR", "NOT_AUTHORIZED", "TOPIC_NAME_INVALID",
            "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID"
    })
    void canBeSetByUser_true(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSetByUser());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRecReasonCode.class, names = {"NO_MATCHING_SUBSCRIBERS", "PACKET_IDENTIFIER_IN_USE"})
    void canBeSetByUser_false(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSetByUser());
    }
}
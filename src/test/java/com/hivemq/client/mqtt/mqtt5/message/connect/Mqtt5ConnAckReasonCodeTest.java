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

package com.hivemq.client.mqtt.mqtt5.message.connect;

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
class Mqtt5ConnAckReasonCodeTest {

    @ParameterizedTest
    @MethodSource("provideCodes")
    void getCode(final @NotNull Mqtt5ConnAckReasonCode reasonCode, final int code) {
        assertEquals(code, reasonCode.getCode());
    }

    @ParameterizedTest
    @MethodSource("provideCodes")
    void fromCode(final @NotNull Mqtt5ConnAckReasonCode reasonCode, final int code) {
        assertEquals(reasonCode, Mqtt5ConnAckReasonCode.fromCode(code));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5ConnAckReasonCode.fromCode(0xFF));
        assertNull(Mqtt5ConnAckReasonCode.fromCode(-1));
    }

    private static @NotNull Stream<Arguments> provideCodes() {
        return Stream.of(
                Arguments.of(Mqtt5ConnAckReasonCode.SUCCESS, 0x00),
                Arguments.of(Mqtt5ConnAckReasonCode.UNSPECIFIED_ERROR, 0x80),
                Arguments.of(Mqtt5ConnAckReasonCode.MALFORMED_PACKET, 0x81),
                Arguments.of(Mqtt5ConnAckReasonCode.PROTOCOL_ERROR, 0x82),
                Arguments.of(Mqtt5ConnAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, 0x83),
                Arguments.of(Mqtt5ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION, 0x84),
                Arguments.of(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, 0x85),
                Arguments.of(Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD, 0x86),
                Arguments.of(Mqtt5ConnAckReasonCode.NOT_AUTHORIZED, 0x87),
                Arguments.of(Mqtt5ConnAckReasonCode.SERVER_UNAVAILABLE, 0x88),
                Arguments.of(Mqtt5ConnAckReasonCode.SERVER_BUSY, 0x89),
                Arguments.of(Mqtt5ConnAckReasonCode.BANNED, 0x8A),
                Arguments.of(Mqtt5ConnAckReasonCode.BAD_AUTHENTICATION_METHOD, 0x8C),
                Arguments.of(Mqtt5ConnAckReasonCode.TOPIC_NAME_INVALID, 0x90),
                Arguments.of(Mqtt5ConnAckReasonCode.PACKET_TOO_LARGE, 0x95),
                Arguments.of(Mqtt5ConnAckReasonCode.QUOTA_EXCEEDED, 0x97),
                Arguments.of(Mqtt5ConnAckReasonCode.PAYLOAD_FORMAT_INVALID, 0x99),
                Arguments.of(Mqtt5ConnAckReasonCode.RETAIN_NOT_SUPPORTED, 0x9A),
                Arguments.of(Mqtt5ConnAckReasonCode.QOS_NOT_SUPPORTED, 0x9B),
                Arguments.of(Mqtt5ConnAckReasonCode.USE_ANOTHER_SERVER, 0x9C),
                Arguments.of(Mqtt5ConnAckReasonCode.SERVER_MOVED, 0x9D),
                Arguments.of(Mqtt5ConnAckReasonCode.CONNECTION_RATE_EXCEEDED, 0x9F));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5ConnAckReasonCode.class, names = {"SUCCESS"})
    void isError_false(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        assertFalse(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5ConnAckReasonCode.class, names = {
            "UNSPECIFIED_ERROR", "MALFORMED_PACKET", "PROTOCOL_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "UNSUPPORTED_PROTOCOL_VERSION", "CLIENT_IDENTIFIER_NOT_VALID", "BAD_USER_NAME_OR_PASSWORD",
            "NOT_AUTHORIZED", "SERVER_UNAVAILABLE", "SERVER_BUSY", "BANNED", "BAD_AUTHENTICATION_METHOD",
            "TOPIC_NAME_INVALID", "PACKET_TOO_LARGE", "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID",
            "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED", "USE_ANOTHER_SERVER", "SERVER_MOVED",
            "CONNECTION_RATE_EXCEEDED"
    })
    void isError_true(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        assertTrue(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5ConnAckReasonCode.class, names = {
            "SUCCESS", "UNSPECIFIED_ERROR", "MALFORMED_PACKET", "PROTOCOL_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "UNSUPPORTED_PROTOCOL_VERSION", "CLIENT_IDENTIFIER_NOT_VALID", "BAD_USER_NAME_OR_PASSWORD",
            "NOT_AUTHORIZED", "SERVER_UNAVAILABLE", "SERVER_BUSY", "BANNED", "BAD_AUTHENTICATION_METHOD",
            "TOPIC_NAME_INVALID", "PACKET_TOO_LARGE", "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID",
            "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED", "USE_ANOTHER_SERVER", "SERVER_MOVED",
            "CONNECTION_RATE_EXCEEDED"
    })
    void canBeSentByServer_true(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5ConnAckReasonCode.class, names = {
            "SUCCESS", "UNSPECIFIED_ERROR", "MALFORMED_PACKET", "PROTOCOL_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "UNSUPPORTED_PROTOCOL_VERSION", "CLIENT_IDENTIFIER_NOT_VALID", "BAD_USER_NAME_OR_PASSWORD",
            "NOT_AUTHORIZED", "SERVER_UNAVAILABLE", "SERVER_BUSY", "BANNED", "BAD_AUTHENTICATION_METHOD",
            "TOPIC_NAME_INVALID", "PACKET_TOO_LARGE", "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID",
            "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED", "USE_ANOTHER_SERVER", "SERVER_MOVED",
            "CONNECTION_RATE_EXCEEDED"
    })
    void canBeSentByClient_false(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5ConnAckReasonCode.class, names = {
            "SUCCESS", "UNSPECIFIED_ERROR", "MALFORMED_PACKET", "PROTOCOL_ERROR", "IMPLEMENTATION_SPECIFIC_ERROR",
            "UNSUPPORTED_PROTOCOL_VERSION", "CLIENT_IDENTIFIER_NOT_VALID", "BAD_USER_NAME_OR_PASSWORD",
            "NOT_AUTHORIZED", "SERVER_UNAVAILABLE", "SERVER_BUSY", "BANNED", "BAD_AUTHENTICATION_METHOD",
            "TOPIC_NAME_INVALID", "PACKET_TOO_LARGE", "QUOTA_EXCEEDED", "PAYLOAD_FORMAT_INVALID",
            "RETAIN_NOT_SUPPORTED", "QOS_NOT_SUPPORTED", "USE_ANOTHER_SERVER", "SERVER_MOVED",
            "CONNECTION_RATE_EXCEEDED"
    })
    void canBeSetByUser_false(final @NotNull Mqtt5ConnAckReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSetByUser());
    }
}
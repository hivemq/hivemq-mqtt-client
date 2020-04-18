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

package com.hivemq.client.mqtt.mqtt5.message.auth;

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
class Mqtt5AuthReasonCodeTest {

    @ParameterizedTest
    @MethodSource("provideCodes")
    void getCode(final @NotNull Mqtt5AuthReasonCode reasonCode, final int code) {
        assertEquals(code, reasonCode.getCode());
    }

    @ParameterizedTest
    @MethodSource("provideCodes")
    void fromCode(final @NotNull Mqtt5AuthReasonCode reasonCode, final int code) {
        assertEquals(reasonCode, Mqtt5AuthReasonCode.fromCode(code));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5AuthReasonCode.fromCode(0xFF));
        assertNull(Mqtt5AuthReasonCode.fromCode(-1));
    }

    private static @NotNull Stream<Arguments> provideCodes() {
        return Stream.of(
                Arguments.of(Mqtt5AuthReasonCode.SUCCESS, 0x00),
                Arguments.of(Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION, 0x18),
                Arguments.of(Mqtt5AuthReasonCode.REAUTHENTICATE, 0x19));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, names = {"SUCCESS", "CONTINUE_AUTHENTICATION", "REAUTHENTICATE"})
    void isError_false(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        assertFalse(reasonCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, names = {"SUCCESS", "CONTINUE_AUTHENTICATION"})
    void canBeSentByServer_true(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, names = {"REAUTHENTICATE"})
    void canBeSentByServer_false(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByServer());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, names = {"CONTINUE_AUTHENTICATION", "REAUTHENTICATE"})
    void canBeSentByClient_true(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        assertTrue(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, names = {"SUCCESS"})
    void canBeSentByClient_false(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSentByClient());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5AuthReasonCode.class, names = {"SUCCESS", "CONTINUE_AUTHENTICATION", "REAUTHENTICATE"})
    void canBeSetByUser_false(final @NotNull Mqtt5AuthReasonCode reasonCode) {
        assertFalse(reasonCode.canBeSetByUser());
    }
}
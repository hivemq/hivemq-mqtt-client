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

package com.hivemq.client.mqtt.mqtt3.message.connect;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt3ConnAckReturnCodeTest {

    @Test
    void getCode_success() {
        assertEquals(0x00, Mqtt3ConnAckReturnCode.SUCCESS.getCode());
    }

    @Test
    void getCode_unsupportedProtocolVersion() {
        assertEquals(0x01, Mqtt3ConnAckReturnCode.UNSUPPORTED_PROTOCOL_VERSION.getCode());
    }

    @Test
    void getCode_identifierRejected() {
        assertEquals(0x02, Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED.getCode());
    }

    @Test
    void getCode_serverUnavailable() {
        assertEquals(0x03, Mqtt3ConnAckReturnCode.SERVER_UNAVAILABLE.getCode());
    }

    @Test
    void getCode_badUserNameOrPassword() {
        assertEquals(0x04, Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD.getCode());
    }

    @Test
    void getCode_notAuthorized() {
        assertEquals(0x05, Mqtt3ConnAckReturnCode.NOT_AUTHORIZED.getCode());
    }

    @ParameterizedTest
    @EnumSource(Mqtt3ConnAckReturnCode.class)
    void fromCode(final @NotNull Mqtt3ConnAckReturnCode returnCode) {
        assertEquals(returnCode, Mqtt3ConnAckReturnCode.fromCode(returnCode.getCode()));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt3ConnAckReturnCode.fromCode(0x06));
        assertNull(Mqtt3ConnAckReturnCode.fromCode(0xFF));
        assertNull(Mqtt3ConnAckReturnCode.fromCode(-1));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt3ConnAckReturnCode.class, names = {"SUCCESS"})
    void isError_false(final @NotNull Mqtt3ConnAckReturnCode returnCode) {
        assertFalse(returnCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt3ConnAckReturnCode.class, names = {
            "UNSUPPORTED_PROTOCOL_VERSION", "IDENTIFIER_REJECTED", "SERVER_UNAVAILABLE", "BAD_USER_NAME_OR_PASSWORD",
            "NOT_AUTHORIZED"
    })
    void isError_true(final @NotNull Mqtt3ConnAckReturnCode returnCode) {
        assertTrue(returnCode.isError());
    }
}
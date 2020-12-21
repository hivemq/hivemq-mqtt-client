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

package com.hivemq.client2.mqtt.mqtt3.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt3SubAckReturnCodeTest {

    @Test
    void getCode_successMaximumQos0() {
        assertEquals(0x00, Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0.getCode());
    }

    @Test
    void getCode_successMaximumQos1() {
        assertEquals(0x01, Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1.getCode());
    }

    @Test
    void getCode_successMaximumQos2() {
        assertEquals(0x02, Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2.getCode());
    }

    @Test
    void getCode_failure() {
        assertEquals(0x80, Mqtt3SubAckReturnCode.FAILURE.getCode());
    }

    @ParameterizedTest
    @EnumSource(Mqtt3SubAckReturnCode.class)
    void fromCode(final @NotNull Mqtt3SubAckReturnCode returnCode) {
        assertEquals(returnCode, Mqtt3SubAckReturnCode.fromCode(returnCode.getCode()));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt3SubAckReturnCode.fromCode(0x03));
        assertNull(Mqtt3SubAckReturnCode.fromCode(0x7F));
        assertNull(Mqtt3SubAckReturnCode.fromCode(0x81));
        assertNull(Mqtt3SubAckReturnCode.fromCode(-1));
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt3SubAckReturnCode.class,
            names = {"SUCCESS_MAXIMUM_QOS_0", "SUCCESS_MAXIMUM_QOS_1", "SUCCESS_MAXIMUM_QOS_2"})
    void isError_false(final @NotNull Mqtt3SubAckReturnCode returnCode) {
        assertFalse(returnCode.isError());
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt3SubAckReturnCode.class, names = {"FAILURE"})
    void isError_true(final @NotNull Mqtt3SubAckReturnCode returnCode) {
        assertTrue(returnCode.isError());
    }
}

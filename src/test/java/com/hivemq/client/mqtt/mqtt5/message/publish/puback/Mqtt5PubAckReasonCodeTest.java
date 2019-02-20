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

package com.hivemq.client.mqtt.mqtt5.message.publish.puback;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Hoff
 */
class Mqtt5PubAckReasonCodeTest {

    @ParameterizedTest
    @MethodSource("pubAckReasonCodeProvider")
    void getCode(final @NotNull Mqtt5PubAckReasonCode reasonCode, final int expectedValue) {
        assertEquals(expectedValue, reasonCode.getCode());
    }

    private static Stream<Arguments> pubAckReasonCodeProvider() {
        return Stream.of(
                Arguments.of(Mqtt5PubAckReasonCode.SUCCESS, 0x00),
                Arguments.of(Mqtt5PubAckReasonCode.NO_MATCHING_SUBSCRIBERS, 0x10),
                Arguments.of(Mqtt5PubAckReasonCode.UNSPECIFIED_ERROR, 0x80),
                Arguments.of(Mqtt5PubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR, 0x83),
                Arguments.of(Mqtt5PubAckReasonCode.NOT_AUTHORIZED, 0x87),
                Arguments.of(Mqtt5PubAckReasonCode.TOPIC_NAME_INVALID, 0x90),
                Arguments.of(Mqtt5PubAckReasonCode.PACKET_IDENTIFIER_IN_USE, 0x91),
                Arguments.of(Mqtt5PubAckReasonCode.QUOTA_EXCEEDED, 0x97),
                Arguments.of(Mqtt5PubAckReasonCode.PAYLOAD_FORMAT_INVALID, 0x99));
    }
}
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

package com.hivemq.client2.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Silvio Giebl
 */
class Mqtt5PayloadFormatIndicatorTest {

    @Test
    void getCode_unspecified() {
        assertEquals(0x00, Mqtt5PayloadFormatIndicator.UNSPECIFIED.getCode());
    }

    @Test
    void getCode_utf8() {
        assertEquals(0x01, Mqtt5PayloadFormatIndicator.UTF_8.getCode());
    }

    @ParameterizedTest
    @EnumSource(Mqtt5PayloadFormatIndicator.class)
    void fromCode(final @NotNull Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        assertEquals(payloadFormatIndicator, Mqtt5PayloadFormatIndicator.fromCode(payloadFormatIndicator.getCode()));
    }

    @Test
    void fromCode_invalidCodes() {
        assertNull(Mqtt5PayloadFormatIndicator.fromCode(0x02));
        assertNull(Mqtt5PayloadFormatIndicator.fromCode(0xFF));
        assertNull(Mqtt5PayloadFormatIndicator.fromCode(-1));
    }
}
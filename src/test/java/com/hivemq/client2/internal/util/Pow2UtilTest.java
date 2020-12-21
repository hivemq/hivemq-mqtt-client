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

package com.hivemq.client2.internal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Silvio Giebl
 */
class Pow2UtilTest {

    @Test
    void roundToPowerOf2Bits() {
        assertEquals(0, Pow2Util.roundToPowerOf2Bits(1));
        for (int i = 2; i < 1000; i++) {
            final int powerOf2Bits = Pow2Util.roundToPowerOf2Bits(i);
            final int powerOf2 = 1 << powerOf2Bits;
            final int smallerPowerOf2 = 1 << (powerOf2Bits - 1);
            assertEquals(smallerPowerOf2, powerOf2 >> 1);
            assertTrue(powerOf2 >= i);
            assertTrue(smallerPowerOf2 < i);
        }
    }
}
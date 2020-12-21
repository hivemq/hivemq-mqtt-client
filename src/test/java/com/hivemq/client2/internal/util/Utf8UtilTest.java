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

import com.google.common.base.Utf8;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
class Utf8UtilTest {

    @Test
    void isWellFormed() {
        final Random random = new Random();
        final byte[] bytes = new byte[100];
        for (int i = 0; i < 10_000; i++) {
            random.nextBytes(bytes);
            final String string = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(Utf8.isWellFormed(bytes), Utf8Util.isWellFormed(bytes) == 0);
            assertEquals(
                    Utf8.isWellFormed(string.getBytes(StandardCharsets.UTF_8)),
                    Utf8Util.isWellFormed(string.getBytes(StandardCharsets.UTF_8)) == 0);
        }
    }

    @Test
    void encodedLength() {
        final Random random = new Random();
        final byte[] bytes = new byte[100];
        for (int i = 0; i < 1_000; i++) {
            random.nextBytes(bytes);
            final String string = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(Utf8.encodedLength(string), Utf8Util.encodedLength(string));
        }
    }
}
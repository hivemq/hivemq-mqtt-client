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

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ByteArrayUtilTest {

    @Test
    void equals() {
        final byte[] bytes = new byte[100];
        final Random random = new Random();
        random.nextBytes(bytes);
        final byte[] clone = bytes.clone();
        assertTrue(ByteArrayUtil.equals(bytes, 0, bytes.length, bytes, 0, bytes.length));
        assertTrue(ByteArrayUtil.equals(bytes, 0, bytes.length, clone, 0, clone.length));
        assertTrue(ByteArrayUtil.equals(bytes, 10, bytes.length, clone, 10, clone.length));
        assertTrue(ByteArrayUtil.equals(bytes, 0, 20, clone, 0, 20));
        assertTrue(ByteArrayUtil.equals(bytes, 10, 20, clone, 10, 20));
    }

    @Test
    void equals_differentOffset() {
        final byte[] bytes = new byte[100];
        final Random random = new Random();
        random.nextBytes(bytes);
        final byte[] clone = Arrays.copyOfRange(bytes, 50, 100);
        assertTrue(ByteArrayUtil.equals(bytes, 50, 100, clone, 0, 50));
        assertTrue(ByteArrayUtil.equals(bytes, 60, 90, clone, 10, 40));
        assertFalse(ByteArrayUtil.equals(bytes, 0, 50, clone, 0, 50));
        assertFalse(ByteArrayUtil.equals(bytes, 10, 40, clone, 10, 40));
    }

    @Test
    void equals_differentLength() {
        final byte[] bytes = new byte[100];
        final Random random = new Random();
        random.nextBytes(bytes);
        final byte[] clone = bytes.clone();
        assertFalse(ByteArrayUtil.equals(bytes, 0, bytes.length, bytes, 0, bytes.length - 1));
        assertFalse(ByteArrayUtil.equals(bytes, 0, bytes.length - 1, bytes, 0, bytes.length));
        assertFalse(ByteArrayUtil.equals(bytes, 0, bytes.length, clone, 0, clone.length - 1));
        assertFalse(ByteArrayUtil.equals(bytes, 0, bytes.length - 1, clone, 0, clone.length));
        assertFalse(ByteArrayUtil.equals(bytes, 10, bytes.length, clone, 10, clone.length - 1));
        assertFalse(ByteArrayUtil.equals(bytes, 10, bytes.length - 1, clone, 10, clone.length));
        assertFalse(ByteArrayUtil.equals(bytes, 0, 20, clone, 0, 20 - 1));
        assertFalse(ByteArrayUtil.equals(bytes, 0, 20 - 1, clone, 0, 20));
        assertFalse(ByteArrayUtil.equals(bytes, 10, 20, clone, 10, 20 - 1));
        assertFalse(ByteArrayUtil.equals(bytes, 10, 20 - 1, clone, 10, 20));
    }

    @Test
    void hashCode_sameAsArrays() {
        final byte[] bytes = new byte[100];
        final Random random = new Random();
        random.nextBytes(bytes);
        assertEquals(
                Arrays.hashCode(Arrays.copyOfRange(bytes, 0, bytes.length)),
                ByteArrayUtil.hashCode(bytes, 0, bytes.length));
        assertEquals(
                Arrays.hashCode(Arrays.copyOfRange(bytes, 10, bytes.length)),
                ByteArrayUtil.hashCode(bytes, 10, bytes.length));
        assertEquals(Arrays.hashCode(Arrays.copyOfRange(bytes, 0, 20)), ByteArrayUtil.hashCode(bytes, 0, 20));
        assertEquals(Arrays.hashCode(Arrays.copyOfRange(bytes, 10, 20)), ByteArrayUtil.hashCode(bytes, 10, 20));
    }

    @Test
    void indexOf() {
        final byte[] bytes = {0, 1, 2, 3, 4, 5};
        for (byte b = 0; b < 6; b++) {
            assertEquals(b, ByteArrayUtil.indexOf(bytes, 0, b));
        }
        assertEquals(bytes.length, ByteArrayUtil.indexOf(bytes, 0, (byte) 123));
    }

    @Test
    void indexOf_offset() {
        final byte[] bytes = {12, 12, 12, 0, 1, 2, 3, 4, 5};
        for (byte b = 0; b < 6; b++) {
            assertEquals(3 + b, ByteArrayUtil.indexOf(bytes, 3, b));
        }
        assertEquals(bytes.length, ByteArrayUtil.indexOf(bytes, 0, (byte) 123));
    }
}
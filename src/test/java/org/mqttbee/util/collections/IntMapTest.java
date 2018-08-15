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

package org.mqttbee.util.collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class IntMapTest {

    @ParameterizedTest
    @ValueSource(ints = {12, 1 << 10, 1 << 16, 1 << 28})
    void put_present(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        map.put(10, "test10");
        map.put(10, "test10_2");
        assertEquals("test10_2", map.get(10));
    }

    @ParameterizedTest
    @ValueSource(ints = {12, 1 << 10, 1 << 16, 1 << 28})
    void get_present(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        map.put(9, "test9");
        map.put(10, "test10");
        assertEquals("test10", map.get(10));
    }

    @ParameterizedTest
    @ValueSource(ints = {12, 1 << 10, 1 << 16, 1 << 28})
    void get_not_present(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        map.put(9, "test9");
        assertNull(map.get(10));
    }

    @ParameterizedTest
    @ValueSource(ints = {12, 1 << 10, 1 << 16, 1 << 28})
    void remove_present(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        map.put(9, "test9");
        map.put(10, "test10");
        map.remove(10);
        assertEquals("test9", map.get(9));
        assertNull(map.get(10));
    }

    @ParameterizedTest
    @ValueSource(ints = {12, 1 << 10, 1 << 16, 1 << 28})
    void remove_not_present(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        map.put(9, "test9");
        map.remove(10);
        assertEquals("test9", map.get(9));
        assertNull(map.get(10));
    }

    @Test
    void put_get_max_size() {
        final IntMap<String> map = IntMap.range(0, Integer.MAX_VALUE);
        map.put(Integer.MAX_VALUE, "max_value");
        assertEquals("max_value", map.get(Integer.MAX_VALUE));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 1 << 10, 1 << 16, 1 << 28})
    void put_boundaries(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.put(-1, "test-1"));
        map.put(0, "test0");
        map.put(size, "test" + size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.put(size + 1, "test" + (size + 1)));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 1 << 10, 1 << 16, 1 << 28})
    void put_boundaries_minKey(final int size) {
        final IntMap<String> map = IntMap.range(3, 3 + size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.put(2, "test2"));
        map.put(3, "test3");
        map.put(3 + size, "test" + (3 + size));
        assertThrows(IndexOutOfBoundsException.class, () -> map.put(3 + size + 1, "test" + (3 + size + 1)));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 1 << 10, 1 << 16, 1 << 28})
    void get_boundaries(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.get(-1));
        map.get(0);
        map.get(size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.get(size + 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 1 << 10, 1 << 16, 1 << 28})
    void get_boundaries_minKey(final int size) {
        final IntMap<String> map = IntMap.range(3, 3 + size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.get(2));
        map.get(3);
        map.get(3 + size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.get(3 + size + 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 1 << 10, 1 << 16, 1 << 28})
    void remove_boundaries(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.remove(-1));
        map.remove(0);
        map.remove(size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.remove(size + 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 1 << 10, 1 << 16, 1 << 28})
    void remove_boundaries_minKey(final int size) {
        final IntMap<String> map = IntMap.range(3, 3 + size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.remove(2));
        map.remove(3);
        map.remove(3 + size);
        assertThrows(IndexOutOfBoundsException.class, () -> map.remove(3 + size + 1));
    }

    @ParameterizedTest
    @CsvSource({"16, 4", "65536, 32", "65536, 64"})
    void put_remove_sequential(final int size, final int chunk) {
        final IntMap<String> map = IntMap.range(0, size - 1);
        for (int i = 0; i < chunk * 2; i++) {
            map.put(i, "test" + i);
        }
        for (int i = 0; i < chunk; i++) {
            assertEquals("test" + i, map.remove(i));
        }
        for (int i = chunk * 2; i < size; i += chunk) {
            for (int j = 0; j < chunk; j++) {
                map.put(i + j, "test" + (i + j));
            }
            for (int j = -chunk; j < 0; j++) {
                assertEquals("test" + (i + j), map.remove(i + j));
            }
        }
        for (int i = size - chunk; i < size; i++) {
            assertEquals("test" + i, map.remove(i));
        }
    }

    @Test
    void size() {
        final IntMap<String> map = IntMap.range(0, 256);
        final int[] keys = {0, 1, 3, 10, 100, 101, 102, 256};
        for (final int key : keys) {
            map.put(key, "test" + key);
        }
        assertEquals(keys.length, map.size());
    }

    @CsvSource({"0, 10", "1, 10", "5, 1000"})
    @ParameterizedTest
    void getMinKey_getMaxKey(final int minKey, final int maxKey) {
        final IntMap<String> map = IntMap.range(minKey, maxKey);
        assertEquals(minKey, map.getMinKey());
        assertEquals(maxKey, map.getMaxKey());
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 256})
    void clear(final int size) {
        final IntMap<String> map = IntMap.range(0, size);
        for (int i = 0; i < size; i += 3) {
            map.put(i, "test" + i);
        }
        map.clear();
        assertEquals(0, map.size());
        for (int i = 0; i < size; i += 3) {
            assertNull(map.get(i));
        }
    }

    @CsvSource({"false", "true"})
    @ParameterizedTest
    void visitor(final boolean cancel) {
        final IntMap<String> map = IntMap.range(0, 256);
        final int[] keys = {0, 1, 3, 10, 100, 101, 102, 256};
        for (final int key : keys) {
            map.put(key, "test" + key);
        }
        final AtomicInteger i = new AtomicInteger();
        map.accept((key, value) -> {
            final int k = keys[i.getAndIncrement()];
            assertEquals(k, key);
            assertEquals("test" + k, value);
            return (!cancel) || (k != 100);
        });
        assertEquals(cancel ? 5 : 8, i.get());
    }

    @ValueSource(ints = {11, 255, 256, 257, 1000})
    @ParameterizedTest
    void resize(final int newMaxKey) {
        final IntMap<String> map = IntMap.range(10, 256);
        for (int i = map.getMinKey(); i < map.getMaxKey(); i++) {
            map.put(i, "test" + i);
        }
        final IntMap<String> newMap = IntMap.resize(map, newMaxKey);
        assertEquals(10, newMap.getMinKey());
        assertEquals(newMaxKey, newMap.getMaxKey());
        for (int i = newMap.getMinKey(); i < Math.min(map.getMaxKey(), newMap.getMaxKey()); i++) {
            assertEquals("test" + i, newMap.get(i));
        }
    }

}
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** @author Silvio Giebl */
class IntMapTest {

    @Test
    void put_present() {
        final IntMap<String> map = new IntMap<>(0, 12);
        map.put(10, "test10");
        map.put(10, "test10_2");
        assertEquals("test10_2", map.get(10));
    }

    @Test
    void get_present() {
        final IntMap<String> map = new IntMap<>(0, 12);
        map.put(9, "test9");
        map.put(10, "test10");
        assertEquals("test10", map.get(10));
    }

    @Test
    void get_not_present() {
        final IntMap<String> map = new IntMap<>(0, 12);
        map.put(9, "test9");
        assertNull(map.get(10));
    }

    @Test
    void remove_present() {
        final IntMap<String> map = new IntMap<>(0, 12);
        map.put(9, "test9");
        map.put(10, "test10");
        map.remove(10);
        assertEquals("test9", map.get(9));
        assertNull(map.get(10));
    }

    @Test
    void remove_not_present() {
        final IntMap<String> map = new IntMap<>(0, 12);
        map.put(9, "test9");
        map.remove(10);
        assertEquals("test9", map.get(9));
        assertNull(map.get(10));
    }

    @Test
    void put_get_max_size() {
        final IntMap<String> map = new IntMap<>(0, Integer.MAX_VALUE);
        map.put(Integer.MAX_VALUE, "max_value");
        assertEquals("max_value", map.get(Integer.MAX_VALUE));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12})
    void put_boundaries(final int size) {
        final IntMap<String> map = new IntMap<>(0, size);
        assertThrows(IllegalArgumentException.class, () -> map.put(-1, "test-1"));
        map.put(0, "test0");
        map.put(15, "test15");
        assertThrows(IllegalArgumentException.class, () -> map.put(16, "test16"));
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 15})
    void put_boundaries_minKey(final int size) {
        final IntMap<String> map = new IntMap<>(3, size);
        assertThrows(IllegalArgumentException.class, () -> map.put(2, "test2"));
        map.put(3, "test3");
        map.put(18, "test18");
        assertThrows(IllegalArgumentException.class, () -> map.put(19, "test19"));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12})
    void get_boundaries(final int size) {
        final IntMap<String> map = new IntMap<>(0, size);
        assertThrows(IllegalArgumentException.class, () -> map.get(-1));
        map.get(0);
        map.get(15);
        assertThrows(IllegalArgumentException.class, () -> map.get(16));
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 15})
    void get_boundaries_minKey(final int size) {
        final IntMap<String> map = new IntMap<>(3, size);
        assertThrows(IllegalArgumentException.class, () -> map.get(2));
        map.get(3);
        map.get(18);
        assertThrows(IllegalArgumentException.class, () -> map.get(19));
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12})
    void remove_boundaries(final int size) {
        final IntMap<String> map = new IntMap<>(0, size);
        assertThrows(IllegalArgumentException.class, () -> map.remove(-1));
        map.remove(0);
        map.remove(15);
        assertThrows(IllegalArgumentException.class, () -> map.remove(16));
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 15})
    void remove_boundaries_minKey(final int size) {
        final IntMap<String> map = new IntMap<>(3, size);
        assertThrows(IllegalArgumentException.class, () -> map.remove(2));
        map.remove(3);
        map.remove(18);
        assertThrows(IllegalArgumentException.class, () -> map.remove(19));
    }

    @Test
    void put_remove_sequential() {
        final IntMap<String> map = new IntMap<>(1, 16);
        for (int i = 1; i <= 8; i++) {
            map.put(i, "test" + i);
        }
        for (int i = 1; i <= 4; i++) {
            assertEquals("test" + i, map.remove(i));
        }
        for (int i = 9; i <= 12; i++) {
            map.put(i, "test" + i);
        }
        for (int i = 5; i <= 12; i++) {
            assertEquals("test" + i, map.remove(i));
        }
    }
}

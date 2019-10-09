/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.util.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class IntMapTest {

    @Test
    void put_not_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.put(new Entry(2, "test2")));
        assertNull(map.put(new Entry(3, "test3")));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(new Entry(3, "test3"), map.get(3));
        assertEquals(2, map.size());
    }

    @Test
    void put_not_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.put(new Entry(2, "test2")));
        assertNull(map.put(new Entry(2 + 16, "test18")));
        assertNull(map.put(new Entry(2 + 32, "test34")));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(new Entry(2 + 16, "test18"), map.get(2 + 16));
        assertEquals(new Entry(2 + 32, "test34"), map.get(2 + 32));
        assertEquals(3, map.size());
    }

    @Test
    void put_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.put(new Entry(10, "test1")));
        assertEquals(new Entry(10, "test1"), map.put(new Entry(10, "test2")));
        assertEquals(new Entry(10, "test2"), map.get(10));
        assertEquals(1, map.size());
    }

    @Test
    void put_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.put(new Entry(10, "test1")));
        assertNull(map.put(new Entry(10 + 16, "test2")));
        assertNull(map.put(new Entry(10 + 32, "test3")));
        assertEquals(new Entry(10 + 32, "test3"), map.put(new Entry(10 + 32, "test4")));
        assertEquals(new Entry(10 + 16, "test2"), map.put(new Entry(10 + 16, "test5")));
        assertEquals(new Entry(10, "test1"), map.put(new Entry(10, "test6")));
        assertEquals(new Entry(10, "test6"), map.get(10));
        assertEquals(new Entry(10 + 16, "test5"), map.get(10 + 16));
        assertEquals(new Entry(10 + 32, "test4"), map.get(10 + 32));
        assertEquals(3, map.size());
    }

    @Test
    void putIfAbsent_not_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.putIfAbsent(new Entry(2, "test2")));
        assertNull(map.putIfAbsent(new Entry(3, "test3")));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(new Entry(3, "test3"), map.get(3));
        assertEquals(2, map.size());
    }

    @Test
    void putIfAbsent_not_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.putIfAbsent(new Entry(2, "test2")));
        assertNull(map.putIfAbsent(new Entry(2 + 16, "test18")));
        assertNull(map.putIfAbsent(new Entry(2 + 32, "test34")));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(new Entry(2 + 16, "test18"), map.get(2 + 16));
        assertEquals(new Entry(2 + 32, "test34"), map.get(2 + 32));
        assertEquals(3, map.size());
    }

    @Test
    void putIfAbsent_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.putIfAbsent(new Entry(10, "test1")));
        assertEquals(new Entry(10, "test1"), map.putIfAbsent(new Entry(10, "test2")));
        assertEquals(new Entry(10, "test1"), map.get(10));
        assertEquals(1, map.size());
    }

    @Test
    void putIfAbsent_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        assertNull(map.putIfAbsent(new Entry(10, "test1")));
        assertNull(map.putIfAbsent(new Entry(10 + 16, "test2")));
        assertNull(map.putIfAbsent(new Entry(10 + 32, "test3")));
        assertEquals(new Entry(10 + 32, "test3"), map.putIfAbsent(new Entry(10 + 32, "test4")));
        assertEquals(new Entry(10 + 16, "test2"), map.putIfAbsent(new Entry(10 + 16, "test5")));
        assertEquals(new Entry(10, "test1"), map.putIfAbsent(new Entry(10, "test6")));
        assertEquals(new Entry(10, "test1"), map.get(10));
        assertEquals(new Entry(10 + 16, "test2"), map.get(10 + 16));
        assertEquals(new Entry(10 + 32, "test3"), map.get(10 + 32));
        assertEquals(3, map.size());
    }

    @Test
    void get_not_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        assertNull(map.get(3));
    }

    @Test
    void get_not_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        assertNull(map.get(2 + 48));
        map.put(new Entry(2 + 16, "test18"));
        assertNull(map.get(2 + 48));
        map.put(new Entry(2 + 32, "test34"));
        assertNull(map.get(2 + 48));
    }

    @Test
    void get_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        assertEquals(new Entry(2, "test2"), map.get(2));
    }

    @Test
    void get_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        map.put(new Entry(2 + 16, "test18"));
        map.put(new Entry(2 + 32, "test34"));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(new Entry(2 + 16, "test18"), map.get(2 + 16));
        assertEquals(new Entry(2 + 32, "test34"), map.get(2 + 32));
    }

    @Test
    void remove_not_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        assertNull(map.remove(3));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(1, map.size());
    }

    @Test
    void remove_not_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        assertNull(map.remove(2 + 48));
        map.put(new Entry(2 + 16, "test18"));
        assertNull(map.remove(2 + 48));
        map.put(new Entry(2 + 32, "test34"));
        assertNull(map.remove(2 + 48));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(new Entry(2 + 16, "test18"), map.get(2 + 16));
        assertEquals(new Entry(2 + 32, "test34"), map.get(2 + 32));
        assertEquals(3, map.size());
    }

    @Test
    void remove_present() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        map.put(new Entry(3, "test3"));
        assertEquals(new Entry(3, "test3"), map.remove(3));
        assertEquals(new Entry(2, "test2"), map.get(2));
        assertEquals(1, map.size());
    }

    @Test
    void remove_present_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        map.put(new Entry(2, "test2"));
        map.put(new Entry(2 + 16, "test18"));
        map.put(new Entry(2 + 32, "test34"));
        assertEquals(new Entry(2, "test2"), map.remove(2));
        assertEquals(new Entry(2 + 16, "test18"), map.get(2 + 16));
        assertEquals(new Entry(2 + 32, "test34"), map.get(2 + 32));
        assertEquals(2, map.size());
    }

    @Test
    void size() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        final int[] keys = {0, 1, 3, 10, 100, 101, 102, 256};
        for (final int key : keys) {
            map.put(new Entry(key, "test" + key));
        }
        assertEquals(keys.length, map.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 256})
    void clear(final int size) {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        for (int i = 0; i < size; i += 3) {
            map.put(new Entry(i, "test" + i));
        }
        map.clear();
        assertEquals(0, map.size());
        for (int i = 0; i < size; i++) {
            assertNull(map.get(i));
        }
    }

    @Test
    void forEach() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        final int[] keys = {0, 1, 3, 10, 100, 101, 102, 256};
        final HashSet<Entry> set = new HashSet<>();
        for (final int key : keys) {
            final Entry entry = new Entry(key, "test" + key);
            map.put(entry);
            set.add(entry);
        }
        map.forEach(entry -> {
            assertEquals(new Entry(entry.id, "test" + entry.id), entry);
            set.remove(entry);
        });
        assertTrue(set.isEmpty());
    }

    @Test
    void forEach_hash_collision() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        final int[] keys = {0, 16, 1, 1 + 16, 1 + 32};
        final HashSet<Entry> set = new HashSet<>();
        for (final int key : keys) {
            final Entry entry = new Entry(key, "test" + key);
            map.put(entry);
            set.add(entry);
        }
        map.forEach(entry -> {
            assertEquals(new Entry(entry.id, "test" + entry.id), entry);
            set.remove(entry);
        });
        assertTrue(set.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"16, 4", "65536, 32", "65536, 64"})
    void put_remove_sequential(final int size, final int chunk) {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        for (int i = 0; i < chunk * 2; i++) {
            map.put(new Entry(i, "test" + i));
        }
        for (int i = 0; i < chunk; i++) {
            assertEquals(new Entry(i, "test" + i), map.get(i));
            assertEquals(new Entry(i, "test" + i), map.remove(i));
        }
        for (int i = chunk * 2; i < size; i += chunk) {
            for (int j = 0; j < chunk; j++) {
                map.put(new Entry(i + j, "test" + (i + j)));
            }
            for (int j = -chunk; j < 0; j++) {
                assertEquals(new Entry(i + j, "test" + (i + j)), map.get(i + j));
                assertEquals(new Entry(i + j, "test" + (i + j)), map.remove(i + j));
            }
        }
        for (int i = size - chunk; i < size; i++) {
            assertEquals(new Entry(i, "test" + i), map.get(i));
            assertEquals(new Entry(i, "test" + i), map.remove(i));
        }
        assertEquals(0, map.size());
    }

    @Test
    void put_remove_sparse() {
        final IntMap<Entry> map = new IntMap<>(new IntMap.Spec<>(e -> e.id));
        boolean reverse = false;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int index = i + j * 16;
                if (reverse) {
                    index = i + (9 - j) * 16;
                }
                assertNull(map.put(new Entry(index, "test" + index)));
            }
            reverse = !reverse;
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                final int index = i + j * 16;
                assertEquals(new Entry(index, "test" + index), map.get(index));
                assertEquals(new Entry(index, "test" + index), map.remove(index));
            }
        }
        assertEquals(0, map.size());
    }

    private static class Entry {

        final int id;
        final @NotNull String value;

        private Entry(final int id, final @NotNull String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Entry)) {
                return false;
            }
            final Entry entry = (Entry) o;
            return (id == entry.id) && value.equals(entry.value);
        }

        @Override
        public int hashCode() {
            return 31 * id + value.hashCode();
        }

        @Override
        public @NotNull String toString() {
            return "Entry{" + "id=" + id + ", value='" + value + '\'' + '}';
        }
    }
}
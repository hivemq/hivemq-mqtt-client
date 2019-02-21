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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class HandleListTest {

    @Test
    void handle_getElement() {
        final HandleList<String> list = new HandleList<>();
        final HandleList.Handle<String> test1 = list.add("test1");
        final HandleList.Handle<String> test2 = list.add("test2");
        assertEquals("test1", test1.getElement());
        assertEquals("test2", test2.getElement());
    }

    @Test
    void isEmpty_add_remove() {
        final HandleList<String> list = new HandleList<>();
        assertTrue(list.isEmpty());
        final HandleList.Handle<String> test1 = list.add("test1");
        assertFalse(list.isEmpty());
        final HandleList.Handle<String> test2 = list.add("test2");
        assertFalse(list.isEmpty());
        test1.remove();
        assertFalse(list.isEmpty());
        test2.remove();
        assertTrue(list.isEmpty());
    }

    @Test
    void iterator_same() {
        final HandleList<String> list = new HandleList<>();
        final Iterator<String> iterator1 = list.iterator();
        final Iterator<String> iterator2 = list.iterator();
        assertSame(iterator1, iterator2);
    }

    @Test
    void iterator_empty() {
        final HandleList<String> list = new HandleList<>();
        final Iterator<String> iterator = list.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @ValueSource(ints = {1, 2, 100})
    @ParameterizedTest
    void iterator(final int size) {
        final HandleList<String> list = new HandleList<>();
        for (int i = 0; i < size; i++) {
            list.add("test" + i);
        }
        int counter = size;
        for (final String s : list) {
            assertEquals("test" + --counter, s);
        }
        assertEquals(0, counter);
    }

    @ValueSource(ints = {1, 2, 100})
    @ParameterizedTest
    void iterator_remove(final int size) {
        final HandleList<String> list = new HandleList<>();
        for (int i = 0; i < size; i++) {
            list.add("test" + i);
        }
        final Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        assertTrue(list.isEmpty());
    }

}
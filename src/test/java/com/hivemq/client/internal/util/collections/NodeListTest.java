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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class NodeListTest {

    @Test
    void add_getFirst_getLast_getPrev_getNext() {
        final NodeList<Entry> list = new NodeList<>();
        assertNull(list.getFirst());
        assertNull(list.getLast());

        final Entry e1 = new Entry("test1");
        list.add(e1);
        assertSame(e1, list.getFirst());
        assertSame(e1, list.getLast());
        assertNull(e1.getPrev());
        assertNull(e1.getNext());

        final Entry e2 = new Entry("test2");
        list.add(e2);
        assertSame(e1, list.getFirst());
        assertSame(e2, list.getLast());
        assertNull(e1.getPrev());
        assertSame(e2, e1.getNext());
        assertSame(e1, e2.getPrev());
        assertNull(e2.getNext());
    }

    @Test
    void add_isEmpty_size() {
        final NodeList<Entry> list = new NodeList<>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());

        final Entry e1 = new Entry("test1");
        list.add(e1);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());

        final Entry e2 = new Entry("test2");
        list.add(e2);
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
    }

    @Test
    void remove_getFirst_getLast_getPrev_getNext() {
        final NodeList<Entry> list = new NodeList<>();
        final Entry e1 = new Entry("test1");
        list.add(e1);
        final Entry e2 = new Entry("test2");
        list.add(e2);
        final Entry e3 = new Entry("test3");
        list.add(e3);
        assertSame(e1, list.getFirst());
        assertSame(e3, list.getLast());
        assertNull(e1.getPrev());
        assertSame(e2, e1.getNext());
        assertSame(e1, e2.getPrev());
        assertSame(e3, e2.getNext());
        assertSame(e2, e3.getPrev());
        assertNull(e3.getNext());

        list.remove(e2);
        assertSame(e1, list.getFirst());
        assertSame(e3, list.getLast());
        assertNull(e1.getPrev());
        assertSame(e3, e1.getNext());
        assertSame(e1, e3.getPrev());
        assertNull(e3.getNext());

        list.remove(e3);
        assertSame(e1, list.getFirst());
        assertSame(e1, list.getLast());
        assertNull(e1.getPrev());
        assertNull(e1.getNext());

        list.remove(e1);
        assertNull(list.getFirst());
        assertNull(list.getLast());
    }

    @Test
    void remove_isEmpty_size() {
        final NodeList<Entry> list = new NodeList<>();
        final Entry e1 = new Entry("test1");
        list.add(e1);
        final Entry e2 = new Entry("test2");
        list.add(e2);
        final Entry e3 = new Entry("test3");
        list.add(e3);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());

        list.remove(e3);
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());

        list.remove(e2);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());

        list.remove(e1);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void replace_getFirst_getLast_getPrev_getNext() {
        final NodeList<Entry> list = new NodeList<>();
        final Entry e1 = new Entry("test1");
        list.add(e1);
        final Entry e2 = new Entry("test2");
        list.add(e2);
        final Entry e3 = new Entry("test3");
        list.add(e3);
        assertSame(e1, list.getFirst());
        assertSame(e3, list.getLast());
        assertNull(e1.getPrev());
        assertSame(e2, e1.getNext());
        assertSame(e1, e2.getPrev());
        assertSame(e3, e2.getNext());
        assertSame(e2, e3.getPrev());
        assertNull(e3.getNext());

        final Entry e4 = new Entry("test4");
        list.replace(e1, e4);
        assertSame(e4, list.getFirst());
        assertSame(e3, list.getLast());
        assertNull(e4.getPrev());
        assertSame(e2, e4.getNext());
        assertSame(e4, e2.getPrev());
        assertSame(e3, e2.getNext());
        assertSame(e2, e3.getPrev());
        assertNull(e3.getNext());

        final Entry e5 = new Entry("test5");
        list.replace(e2, e5);
        assertSame(e4, list.getFirst());
        assertSame(e3, list.getLast());
        assertNull(e4.getPrev());
        assertSame(e5, e4.getNext());
        assertSame(e4, e5.getPrev());
        assertSame(e3, e5.getNext());
        assertSame(e5, e3.getPrev());
        assertNull(e3.getNext());

        final Entry e6 = new Entry("test6");
        list.replace(e3, e6);
        assertSame(e4, list.getFirst());
        assertSame(e6, list.getLast());
        assertNull(e4.getPrev());
        assertSame(e5, e4.getNext());
        assertSame(e4, e5.getPrev());
        assertSame(e6, e5.getNext());
        assertSame(e5, e6.getPrev());
        assertNull(e6.getNext());
    }

    @Test
    void replace_isEmpty_size() {
        final NodeList<Entry> list = new NodeList<>();
        final Entry e1 = new Entry("test1");
        list.add(e1);
        final Entry e2 = new Entry("test2");
        list.add(e2);
        final Entry e3 = new Entry("test3");
        list.add(e3);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());

        final Entry e4 = new Entry("test4");
        list.replace(e1, e4);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());

        final Entry e5 = new Entry("test5");
        list.replace(e2, e5);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());

        final Entry e6 = new Entry("test6");
        list.replace(e3, e6);
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());
    }

    @Test
    void clear() {
        final NodeList<Entry> list = new NodeList<>();
        list.add(new Entry("test1"));
        list.add(new Entry("test2"));
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    private static class Entry extends NodeList.Node<Entry> {

        final @NotNull String value;

        private Entry(@NotNull final String value) {
            this.value = value;
        }
    }
}
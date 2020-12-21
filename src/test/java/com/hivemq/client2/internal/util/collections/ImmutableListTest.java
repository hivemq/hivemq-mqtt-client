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

package com.hivemq.client2.internal.util.collections;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ImmutableListTest {

    private static @NotNull Stream<ImmutableList<String>> numberedList() {
        // @formatter:off
        return Stream.of(
                ImmutableList.of(),
                ImmutableList.of("0"),
                ImmutableList.of("0", "1"),
                ImmutableList.of("0", "1", "2"),
                ImmutableList.of("x", "x", "x", "x").subList(2, 2),
                ImmutableList.of("x", "x", "x", "x").subList(1, 3).subList(1, 1),
                ImmutableList.of("x", "x", "0", "x", "x").subList(2, 3),
                ImmutableList.of("x", "x", "0", "x", "x").subList(1, 4).subList(1, 2),
                ImmutableList.of("x", "x", "0", "1", "2", "x", "x").subList(2, 5),
                ImmutableList.of("x", "x", "0", "1", "2", "x", "x").subList(1, 6).subList(1, 4));
        // @formatter:on
    }

    @Test
    void of_empty_isSingleton() {
        final ImmutableList<Object> of1 = ImmutableList.of();
        final ImmutableList<String> of2 = ImmutableList.of();
        final ImmutableList<Object> built1 = ImmutableList.builder().build();
        final ImmutableList<String> built2 = ImmutableList.<String>builder().build();

        assertSame(of1, of2);
        assertSame(of1, built1);
        assertSame(built1, built2);

        assertTrue(of1.isEmpty());
        assertTrue(built1.isEmpty());
        assertEquals(0, of1.size());
        assertEquals(0, built1.size());

        assertSame(ImmutableEmptyList.class, of1.getClass());
        assertSame(ImmutableEmptyList.class, built1.getClass());
    }

    @Test
    void of_single() {
        final ImmutableList<String> of1 = ImmutableList.of("test");
        final ImmutableList<String> of2 = ImmutableList.of("test");
        final ImmutableList<String> built1 = ImmutableList.<String>builder().add("test").build();
        final ImmutableList<String> built2 = ImmutableList.<String>builder().add("test").build();

        assertEquals(of1, of2);
        assertEquals(of1, built1);
        assertEquals(built1, built2);

        assertFalse(of1.isEmpty());
        assertFalse(built1.isEmpty());
        assertEquals(1, of1.size());
        assertEquals(1, built1.size());

        assertSame(ImmutableElement.class, of1.getClass());
        assertSame(ImmutableElement.class, built1.getClass());
    }

    @Test
    void of_multiple() {
        final ImmutableList<String> of1 = ImmutableList.of("1", "2");
        final ImmutableList<String> of2 = ImmutableList.of("1", "2");
        final ImmutableList<String> built1 = ImmutableList.<String>builder().add("1").add("2").build();
        final ImmutableList<String> built2 = ImmutableList.<String>builder().add("1").add("2").build();

        assertEquals(of1, of2);
        assertEquals(of1, built1);
        assertEquals(built1, built2);

        assertFalse(of1.isEmpty());
        assertFalse(built1.isEmpty());
        assertEquals(2, of1.size());
        assertEquals(2, built1.size());

        assertSame(ImmutableArray.class, of1.getClass());
        assertSame(ImmutableArray.class, built1.getClass());
    }

    @Test
    void copyOf_empty_isSingleton() {
        final ImmutableList<Object> array1 = ImmutableList.copyOf(new String[0]);
        final ImmutableList<Object> array2 = ImmutableList.copyOf(new String[0]);
        final ImmutableList<Object> collection1 = ImmutableList.copyOf(new LinkedList<>());
        final ImmutableList<Object> collection2 = ImmutableList.copyOf(new ArrayList<>());

        assertSame(array1, array2);
        assertSame(array1, collection1);
        assertSame(collection1, collection2);

        assertTrue(array1.isEmpty());
        assertTrue(collection1.isEmpty());
        assertEquals(0, array1.size());
        assertEquals(0, collection1.size());

        assertSame(ImmutableEmptyList.class, array1.getClass());
        assertSame(ImmutableEmptyList.class, collection1.getClass());
    }

    @Test
    void copyOf_single() {
        final ImmutableList<String> array1 = ImmutableList.copyOf(new String[]{"test"});
        final ImmutableList<String> array2 = ImmutableList.copyOf(new String[]{"test"});
        final ImmutableList<String> collection1 =
                ImmutableList.copyOf(new LinkedList<>(Collections.singletonList("test")));
        final ImmutableList<String> collection2 =
                ImmutableList.copyOf(new ArrayList<>(Collections.singletonList("test")));

        assertEquals(array1, array2);
        assertEquals(array1, collection1);
        assertEquals(collection1, collection2);

        assertFalse(array1.isEmpty());
        assertFalse(collection1.isEmpty());
        assertEquals(1, array1.size());
        assertEquals(1, collection1.size());

        assertSame(ImmutableElement.class, array1.getClass());
        assertSame(ImmutableElement.class, collection1.getClass());
    }

    @Test
    void copyOf_multiple() {
        final ImmutableList<String> array1 = ImmutableList.copyOf(new String[]{"1", "2"});
        final ImmutableList<String> array2 = ImmutableList.copyOf(new String[]{"1", "2"});
        final ImmutableList<String> collection1 = ImmutableList.copyOf(new LinkedList<>(Arrays.asList("1", "2")));
        final ImmutableList<String> collection2 = ImmutableList.copyOf(new ArrayList<>(Arrays.asList("1", "2")));

        assertEquals(array1, array2);
        assertEquals(array1, collection1);
        assertEquals(collection1, collection2);

        assertFalse(array1.isEmpty());
        assertFalse(collection1.isEmpty());
        assertEquals(2, array1.size());
        assertEquals(2, collection1.size());

        assertSame(ImmutableArray.class, array1.getClass());
        assertSame(ImmutableArray.class, collection1.getClass());
    }

    @Test
    void copyOf_immutable_isSame() {
        final ImmutableList<String> empty = ImmutableList.of();
        final ImmutableList<String> single = ImmutableList.of("1");
        final ImmutableList<String> multiple = ImmutableList.of("1", "2");

        assertSame(empty, ImmutableList.copyOf(empty));
        assertSame(single, ImmutableList.copyOf(single));
        assertSame(multiple, ImmutableList.copyOf(multiple));
    }

    @Test
    void builder_add() {
        final ImmutableList.Builder<String> list = ImmutableList.<String>builder().add("1").add("2");
        assertEquals(Arrays.asList("1", "2"), list.build());
    }

    @Test
    void builder_addAll() {
        final ArrayList<String> arrayList = new ArrayList<>();
        final LinkedList<String> linkedList = new LinkedList<>();
        final LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();

        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.addAll(ImmutableList.of("1", "2"));
        arrayList.add("3");
        arrayList.add("4");
        builder.addAll(arrayList);
        linkedList.add("5");
        linkedList.add("6");
        builder.addAll(linkedList);
        linkedHashSet.add("7");
        linkedHashSet.add("8");
        builder.addAll(linkedHashSet);

        assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"), builder.build());
    }

    @Test
    void builder_expectedSize() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder(3);
        builder.add("1");
        builder.addAll(ImmutableList.of("2", "3"));
        assertEquals(ImmutableList.of("1", "2", "3"), builder.build());
    }

    @Test
    void builder_ensureFree() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder(3);
        builder.add("1");
        builder.addAll(ImmutableList.of("2", "3"));
        builder.ensureFree(2);
        builder.add("4");
        builder.add("5");
        assertEquals(ImmutableList.of("1", "2", "3", "4", "5"), builder.build());
    }

    @Test
    void builder_reuse() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        final ImmutableList<String> empty = builder.build();
        final ImmutableList<String> single = builder.add("1").build();
        final ImmutableList<String> multiple = builder.add("2").build();
        assertEquals(ImmutableList.of(), empty);
        assertEquals(ImmutableList.of("1"), single);
        assertEquals(ImmutableList.of("1", "2"), multiple);
    }

    @Test
    void builder_getSize() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        assertEquals(0, builder.getSize());
        builder.add("1");
        assertEquals(1, builder.getSize());
        builder.addAll(ImmutableList.of("2"));
        assertEquals(2, builder.getSize());
        builder.addAll(ImmutableList.of("3", "4", "5"));
        assertEquals(5, builder.getSize());
    }

    @Test
    @SuppressWarnings("deprecation")
    void modifyMethods_throwUOE() {
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").add("2"));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").add(0, "2"));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").addAll(ImmutableList.of("2")));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").addAll(0, ImmutableList.of("2")));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").remove("1"));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").remove(1));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").removeAll(ImmutableList.of("1")));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").removeIf(s -> s.equals("1")));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").retainAll(ImmutableList.of("1")));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").replaceAll(s -> "2"));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").set(1, "2"));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").sort(String::compareTo));
        assertThrows(UnsupportedOperationException.class, () -> ImmutableList.of("1").clear());
        assertThrows(UnsupportedOperationException.class, () -> {
            final ImmutableList.ImmutableListIterator<String> iterator = ImmutableList.of("1").iterator();
            iterator.next();
            iterator.remove();
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            final ImmutableList.ImmutableListIterator<String> iterator = ImmutableList.of("1").listIterator();
            iterator.next();
            iterator.remove();
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            final ImmutableList.ImmutableListIterator<String> iterator = ImmutableList.of("1").listIterator();
            iterator.next();
            iterator.add("2");
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            final ImmutableList.ImmutableListIterator<String> iterator = ImmutableList.of("1").listIterator();
            iterator.next();
            iterator.set("2");
        });
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void get(final @NotNull ImmutableList<String> list) {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        for (int i = 0; i < list.size(); i++) {
            assertEquals("" + i, list.get(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void toArray(final @NotNull ImmutableList<String> list) {
        final Object[] array = new Object[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = "" + i;
        }
        assertArrayEquals(array, list.toArray());
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void toArray_typed(final @NotNull ImmutableList<String> list) {
        final String[] array = new String[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = "" + i;
        }
        assertArrayEquals(array, list.toArray(new String[0]));

        final String[] sameSizeArray = new String[list.size()];
        Arrays.fill(sameSizeArray, "x");
        final String[] sameSizeArrayResult = list.toArray(sameSizeArray);
        assertSame(sameSizeArray, sameSizeArrayResult);
        assertArrayEquals(array, sameSizeArrayResult);

        final String[] overSizeArray = new String[list.size() + 4];
        Arrays.fill(overSizeArray, "x");
        final String[] overSizeArrayResult = list.toArray(overSizeArray);
        assertSame(overSizeArray, overSizeArrayResult);
        for (int i = 0; i < list.size(); i++) {
            assertEquals("" + i, overSizeArrayResult[i]);
        }
        assertNull(overSizeArrayResult[list.size()]);
        assertNotNull(overSizeArrayResult[list.size() + 1]);
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void indexOf(final @NotNull ImmutableList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i, list.indexOf("" + i));
        }
        assertEquals(-1, list.indexOf("" + list.size()));
        assertEquals(-1, list.indexOf("x"));
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void lastIndexOf(final @NotNull ImmutableList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i, list.lastIndexOf("" + i));
        }
        assertEquals(-1, list.lastIndexOf("" + list.size()));
        assertEquals(-1, list.lastIndexOf("x"));
    }

    @Test
    void indexOf_lastIndexOf_multipleElements() {
        final ImmutableList<String> list = ImmutableList.of("1", "2", "3", "2", "1");
        assertEquals(0, list.indexOf("1"));
        assertEquals(1, list.indexOf("2"));
        assertEquals(2, list.indexOf("3"));
        assertEquals(4, list.lastIndexOf("1"));
        assertEquals(3, list.lastIndexOf("2"));
        assertEquals(2, list.indexOf("3"));
    }

    @Test
    void indexOf_lastIndexOf_null() {
        final ImmutableList<String> list = ImmutableList.of("1", "2", "3");
        assertEquals(-1, list.indexOf(null));
        assertEquals(-1, list.lastIndexOf(null));
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void contains(final @NotNull ImmutableList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            assertTrue(list.contains("" + i));
        }
        assertFalse(list.contains("" + list.size()));
        assertFalse(list.contains("x"));
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    @SuppressWarnings("CollectionAddedToSelf")
    void containsAll(final @NotNull ImmutableList<String> list) {
        assertTrue(list.containsAll(list));
        assertTrue(list.containsAll(ImmutableList.of()));
        assertTrue(list.containsAll(ImmutableList.builder().addAll(list).build()));
        assertFalse(list.containsAll(ImmutableList.of("x")));
        assertFalse(list.containsAll(ImmutableList.builder().addAll(list).add("x").build()));
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void iterator(final @NotNull ImmutableList<String> list) {
        final ImmutableList.ImmutableListIterator<String> iterator = list.iterator();
        for (int i = 0; i < list.size(); i++) {
            assertTrue(iterator.hasNext());
            assertEquals("" + i, iterator.next());
        }
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @ParameterizedTest()
    @MethodSource("numberedList")
    void listIterator(final @NotNull ImmutableList<String> list) {
        final ImmutableList.ImmutableListIterator<String> iterator = list.listIterator();
        assertFalse(iterator.hasPrevious());
        assertThrows(NoSuchElementException.class, iterator::previous);
        assertEquals(0, iterator.nextIndex());
        assertEquals(-1, iterator.previousIndex());
        for (int i = 0; i < list.size(); i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i, iterator.nextIndex());
            assertEquals(i - 1, iterator.previousIndex());
            assertEquals("" + i, iterator.next());
            assertEquals(i + 1, iterator.nextIndex());
            assertEquals(i, iterator.previousIndex());
            assertTrue(iterator.hasPrevious());
        }
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
        for (int i = list.size() - 1; i >= 0; i--) {
            assertTrue(iterator.hasPrevious());
            assertEquals(i + 1, iterator.nextIndex());
            assertEquals(i, iterator.previousIndex());
            assertEquals("" + i, iterator.previous());
            assertEquals(i, iterator.nextIndex());
            assertEquals(i - 1, iterator.previousIndex());
            assertTrue(iterator.hasNext());
        }
        assertFalse(iterator.hasPrevious());
        assertThrows(NoSuchElementException.class, iterator::previous);
    }

    @ParameterizedTest()
    @MethodSource("numberedList")
    void listIterator_index(final @NotNull ImmutableList<String> list) {
        final ImmutableList.ImmutableListIterator<String> iterator = list.listIterator(list.size());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
        assertEquals(list.size(), iterator.nextIndex());
        assertEquals(list.size() - 1, iterator.previousIndex());
        for (int i = list.size() - 1; i >= 0; i--) {
            assertTrue(iterator.hasPrevious());
            assertEquals(i + 1, iterator.nextIndex());
            assertEquals(i, iterator.previousIndex());
            assertEquals("" + i, iterator.previous());
            assertEquals(i, iterator.nextIndex());
            assertEquals(i - 1, iterator.previousIndex());
            assertTrue(iterator.hasNext());
        }
        assertFalse(iterator.hasPrevious());
        assertThrows(NoSuchElementException.class, iterator::previous);
        for (int i = 0; i < list.size(); i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i, iterator.nextIndex());
            assertEquals(i - 1, iterator.previousIndex());
            assertEquals("" + i, iterator.next());
            assertEquals(i + 1, iterator.nextIndex());
            assertEquals(i, iterator.previousIndex());
            assertTrue(iterator.hasPrevious());
        }
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @ParameterizedTest()
    @MethodSource("numberedList")
    void listIterator_forEachRemaining(final @NotNull ImmutableList<String> list) {
        final ImmutableList.ImmutableListIterator<String> iterator = list.listIterator();
        final AtomicInteger atomicInteger = new AtomicInteger();
        iterator.forEachRemaining(s -> assertEquals("" + atomicInteger.getAndIncrement(), s));
        assertEquals(list.size(), atomicInteger.get());
        iterator.forEachRemaining(s -> atomicInteger.getAndIncrement());
        assertEquals(list.size(), atomicInteger.get());
    }

    @ParameterizedTest()
    @MethodSource("numberedList")
    void listIterator_forEachRemaining_after_next(final @NotNull ImmutableList<String> list) {
        final ImmutableList.ImmutableListIterator<String> iterator = list.listIterator();
        final AtomicInteger atomicInteger = new AtomicInteger();
        if (iterator.hasNext()) {
            assertEquals("" + atomicInteger.getAndIncrement(), iterator.next());
        }
        iterator.forEachRemaining(s -> assertEquals("" + atomicInteger.getAndIncrement(), s));
        assertEquals(list.size(), atomicInteger.get());
        iterator.forEachRemaining(s -> atomicInteger.getAndIncrement());
        assertEquals(list.size(), atomicInteger.get());
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void stream(final @NotNull ImmutableList<String> list) {
        final AtomicInteger atomicInteger = new AtomicInteger();
        list.stream().map(s -> "test" + s).forEach(s -> assertEquals("test" + atomicInteger.getAndIncrement(), s));
        assertEquals(list.size(), atomicInteger.get());
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void forEach(final @NotNull ImmutableList<String> list) {
        final AtomicInteger atomicInteger = new AtomicInteger();
        list.forEach(s -> assertEquals("" + atomicInteger.getAndIncrement(), s));
        assertEquals(list.size(), atomicInteger.get());
    }

    @Test
    void subList_single_to_empty() {
        final ImmutableList<String> subList = ImmutableList.of("1").subList(0, 0);
        assertSame(ImmutableList.of(), subList);
        assertSame(ImmutableEmptyList.class, subList.getClass());
    }

    @Test
    void subList_multiple_to_empty() {
        final ImmutableList<String> subList = ImmutableList.of("1", "2", "3").subList(1, 1);
        assertSame(ImmutableList.of(), subList);
        assertSame(ImmutableEmptyList.class, subList.getClass());
    }

    @Test
    void subList_multiple_to_single() {
        final ImmutableList<String> subList = ImmutableList.of("1", "2", "3").subList(1, 2);
        assertEquals(ImmutableList.of("2"), subList);
        assertSame(ImmutableElement.class, subList.getClass());
    }

    @Test
    void subList_multiple_to_multiple() {
        final ImmutableList<String> subList = ImmutableList.of("1", "2", "3").subList(1, 3);
        assertEquals(ImmutableList.of("2", "3"), subList);
        assertTrue(subList instanceof ImmutableArray);
    }

    @Test
    void subList_same_to_same() {
        final ImmutableList<String> empty = ImmutableList.of();
        final ImmutableList<String> single = ImmutableList.of("1");
        final ImmutableList<String> multiple = ImmutableList.of("1", "2", "3");
        final ImmutableList<String> emptySubList = empty.subList(0, 0);
        final ImmutableList<String> singleSubList = single.subList(0, 1);
        final ImmutableList<String> multipleSubList = multiple.subList(0, 3);
        assertSame(empty, emptySubList);
        assertSame(single, singleSubList);
        assertSame(multiple, multipleSubList);
    }

    @Test
    void subList_subList_empty() {
        final ImmutableList<String> subList = ImmutableList.of("x", "1", "2", "3", "x").subList(1, 4).subList(1, 1);
        assertSame(ImmutableList.of(), subList);
        assertSame(ImmutableEmptyList.class, subList.getClass());
    }

    @Test
    void subList_subList_single() {
        final ImmutableList<String> subList = ImmutableList.of("x", "1", "2", "3", "x").subList(1, 4).subList(1, 2);
        assertEquals(ImmutableList.of("2"), subList);
        assertSame(ImmutableElement.class, subList.getClass());
    }

    @Test
    void subList_subList_multiple() {
        final ImmutableList<String> subList = ImmutableList.of("x", "1", "2", "3", "x").subList(1, 4).subList(1, 3);
        assertEquals(ImmutableList.of("2", "3"), subList);
        assertTrue(subList instanceof ImmutableArray);
    }

    @Test
    void subList_subList_same() {
        final ImmutableList<String> list = ImmutableList.of("x", "1", "2", "3", "x").subList(1, 4);
        final ImmutableList<String> subList = list.subList(0, 3);
        assertSame(list, subList);
    }

    @Test
    void trim_same() {
        final ImmutableList<String> empty = ImmutableList.of();
        final ImmutableList<String> single = ImmutableList.of("1");
        final ImmutableList<String> multiple = ImmutableList.of("1", "2");
        assertSame(empty, empty.trim());
        assertSame(single, single.trim());
        assertSame(multiple, multiple.trim());
    }

    @Test
    void trim_subList() {
        final ImmutableList<String> empty = ImmutableList.of("1", "2", "3").subList(1, 1);
        final ImmutableList<String> single = ImmutableList.of("1", "2", "3").subList(1, 2);
        final ImmutableList<String> multiple = ImmutableList.of("1", "2", "3").subList(1, 3);
        assertSame(empty, empty.trim());
        assertSame(single, single.trim());
        assertNotSame(multiple, multiple.trim());
    }

    @Test
    void equals_differentListImplementation() {
        final ArrayList<String> arrayList = new ArrayList<>();
        final LinkedList<String> linkedList = new LinkedList<>();
        assertEquals(ImmutableList.of(), arrayList);
        assertEquals(ImmutableList.of(), linkedList);

        arrayList.add("1");
        linkedList.add("1");
        assertEquals(ImmutableList.of("1"), arrayList);
        assertEquals(ImmutableList.of("1"), linkedList);

        arrayList.add("2");
        linkedList.add("2");
        assertEquals(ImmutableList.of("1", "2"), arrayList);
        assertEquals(ImmutableList.of("1", "2"), linkedList);
    }
}
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

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ImmutableIntListTest {

    private static @NotNull Stream<ImmutableIntList> numberedList() {
        // @formatter:off
        return Stream.of(
                ImmutableIntList.of(),
                ImmutableIntList.of(0),
                ImmutableIntList.of(0, 1),
                ImmutableIntList.of(0, 1, 2));
        // @formatter:on
    }

    @Test
    void of_empty_isSingleton() {
        final ImmutableIntList of1 = ImmutableIntList.of();
        final ImmutableIntList of2 = ImmutableIntList.of();
        final ImmutableIntList built1 = ImmutableIntList.builder().build();
        final ImmutableIntList built2 = ImmutableIntList.builder().build();

        assertSame(of1, of2);
        assertSame(of1, built1);
        assertSame(built1, built2);

        assertTrue(of1.isEmpty());
        assertTrue(built1.isEmpty());
        assertEquals(0, of1.size());
        assertEquals(0, built1.size());

        assertSame(ImmutableEmptyIntList.class, of1.getClass());
        assertSame(ImmutableEmptyIntList.class, built1.getClass());
    }

    @Test
    void of_single() {
        final ImmutableIntList of1 = ImmutableIntList.of(0);
        final ImmutableIntList of2 = ImmutableIntList.of(0);
        final ImmutableIntList built1 = ImmutableIntList.builder().add(0).build();
        final ImmutableIntList built2 = ImmutableIntList.builder().add(0).build();

        assertEquals(of1, of2);
        assertEquals(of1, built1);
        assertEquals(built1, built2);

        assertFalse(of1.isEmpty());
        assertFalse(built1.isEmpty());
        assertEquals(1, of1.size());
        assertEquals(1, built1.size());

        assertSame(ImmutableIntElement.class, of1.getClass());
        assertSame(ImmutableIntElement.class, built1.getClass());
    }

    @Test
    void of_multiple() {
        final ImmutableIntList of1 = ImmutableIntList.of(1, 2);
        final ImmutableIntList of2 = ImmutableIntList.of(1, 2);
        final ImmutableIntList built1 = ImmutableIntList.builder().add(1).add(2).build();
        final ImmutableIntList built2 = ImmutableIntList.builder().add(1).add(2).build();

        assertEquals(of1, of2);
        assertEquals(of1, built1);
        assertEquals(built1, built2);

        assertFalse(of1.isEmpty());
        assertFalse(built1.isEmpty());
        assertEquals(2, of1.size());
        assertEquals(2, built1.size());

        assertSame(ImmutableIntArray.class, of1.getClass());
        assertSame(ImmutableIntArray.class, built1.getClass());
    }

    @Test
    void copyOf_empty_isSingleton() {
        final ImmutableIntList array1 = ImmutableIntList.copyOf(new int[0]);
        final ImmutableIntList array2 = ImmutableIntList.copyOf(new int[0]);

        assertSame(array1, array2);

        assertTrue(array1.isEmpty());
        assertEquals(0, array1.size());

        assertSame(ImmutableEmptyIntList.class, array1.getClass());
    }

    @Test
    void copyOf_single() {
        final ImmutableIntList array1 = ImmutableIntList.copyOf(new int[]{1});
        final ImmutableIntList array2 = ImmutableIntList.copyOf(new int[]{1});

        assertEquals(array1, array2);

        assertFalse(array1.isEmpty());
        assertEquals(1, array1.size());

        assertSame(ImmutableIntElement.class, array1.getClass());
    }

    @Test
    void copyOf_multiple() {
        final ImmutableIntList array1 = ImmutableIntList.copyOf(new int[]{1, 2});
        final ImmutableIntList array2 = ImmutableIntList.copyOf(new int[]{1, 2});

        assertEquals(array1, array2);

        assertFalse(array1.isEmpty());
        assertEquals(2, array1.size());

        assertSame(ImmutableIntArray.class, array1.getClass());
    }

    @Test
    void builder_add() {
        final ImmutableIntList.Builder list = ImmutableIntList.builder().add(1).add(2);
        assertEquals(ImmutableIntList.of(1, 2), list.build());
    }

    @Test
    void builder_expectedSize() {
        final ImmutableIntList.Builder builder = ImmutableIntList.builder(3);
        builder.add(1).add(2).add(3);
        assertEquals(ImmutableIntList.of(1, 2, 3), builder.build());
    }

    @Test
    void builder_reuse() {
        final ImmutableIntList.Builder builder = ImmutableIntList.builder();
        final ImmutableIntList empty = builder.build();
        final ImmutableIntList single = builder.add(1).build();
        final ImmutableIntList multiple = builder.add(2).build();
        assertEquals(ImmutableIntList.of(), empty);
        assertEquals(ImmutableIntList.of(1), single);
        assertEquals(ImmutableIntList.of(1, 2), multiple);
    }

    @ParameterizedTest
    @MethodSource("numberedList")
    void get(final @NotNull ImmutableIntList list) {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i, list.get(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
    }
}
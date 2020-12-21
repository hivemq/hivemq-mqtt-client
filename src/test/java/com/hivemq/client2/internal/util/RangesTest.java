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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Silvio Giebl
 */
class RangesTest {

    @CsvSource({"0, 10", "1, 10", "9, 10"})
    @ParameterizedTest
    void getId_sequential(final int minId, final int maxId) {
        final Ranges ranges = new Ranges(minId, maxId);
        for (int i = minId; i <= maxId; i++) {
            assertEquals(i, ranges.getId());
        }
        assertEquals(-1, ranges.getId());
    }

    @Test
    void getId_lowest() {
        final Ranges ranges = new Ranges(0, 10);
        assertEquals(0, ranges.getId());
        assertEquals(1, ranges.getId());
        assertEquals(2, ranges.getId());
        ranges.returnId(1);
        assertEquals(1, ranges.getId());
        assertEquals(3, ranges.getId());
    }

    @Test
    void returnId_combineIntervals() {
        final Ranges ranges = new Ranges(0, 10);
        for (int i = 0; i <= 5; i++) {
            assertEquals(i, ranges.getId());
        }
        for (int i = 0; i <= 5; i++) {
            ranges.returnId(i);
        }
    }

    @ValueSource(ints = {0, 1, 10})
    @ParameterizedTest
    void returnId_alreadyPresent(final int id) {
        final Ranges ranges = new Ranges(0, 10);
        assertThrows(IllegalStateException.class, () -> ranges.returnId(id));
    }

    @ValueSource(ints = {11, 12})
    @ParameterizedTest
    void returnId_greaterThanMaxId(final int id) {
        final Ranges ranges = new Ranges(0, 10);
        assertThrows(IllegalStateException.class, () -> ranges.returnId(id));
    }

    @ValueSource(ints = {5, 15})
    @ParameterizedTest
    void resize(final int maxId) {
        final Ranges ranges = new Ranges(0, 10);
        assertEquals(0, ranges.resize(maxId));
        for (int i = 0; i <= maxId; i++) {
            assertEquals(i, ranges.getId());
        }
        assertEquals(-1, ranges.getId());
    }

    @CsvSource({"2, 2", "3, 3", "4, 4", "5, 4"})
    @ParameterizedTest
    void resize_notReturned(final int gap, final int count) {
        final Ranges ranges = new Ranges(0, 10);
        for (int i = 0; i <= 10; i++) {
            assertEquals(i, ranges.getId());
        }
        for (int i = 0; i <= 10; i += gap) {
            ranges.returnId(i);
        }
        assertEquals(count, ranges.resize(5));
    }

}
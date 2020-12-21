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

import com.hivemq.client2.internal.util.collections.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ChecksTest {

    @Test
    void notNull() {
        final Object o = new Object();
        assertSame(o, Checks.notNull(o, "test-name"));
    }

    @Test
    void notNull_null() {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> Checks.notNull(null, "test-name"));
        assertEquals("test-name must not be null.", exception.getMessage());
    }

    @Test
    void notEmpty() {
        final String string = "string";
        assertSame(string, Checks.notEmpty(string, "test-name"));
    }

    @Test
    void notEmpty_empty() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Checks.notEmpty("", "test-name"));
        assertEquals("test-name must be at least one character long.", exception.getMessage());
    }

    @Test
    void notEmpty_null() {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> Checks.notEmpty(null, "test-name"));
        assertEquals("test-name must not be null.", exception.getMessage());
    }

    @Test
    void notImplemented() {
        final Interface o = new Impl();
        final Impl impl = Checks.notImplemented(o, Impl.class, "test-name");
        assertSame(o, impl);
    }

    @Test
    void notImplemented_otherImpl() {
        final Interface o = new OtherImpl();
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Checks.notImplemented(o, Impl.class, "test-name"));
        assertEquals(
                "test-name must not be implemented by the user, but was implemented by com.hivemq.client2.internal.util.ChecksTest$OtherImpl.",
                exception.getMessage());
    }

    @Test
    void notImplemented_null() {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> Checks.notImplemented(null, Impl.class, "test-name"));
        assertEquals("test-name must not be null.", exception.getMessage());
    }

    @Test
    void notImplementedOrNull() {
        final Interface o = new Impl();
        final Impl impl = Checks.notImplementedOrNull(o, Impl.class, "test-name");
        assertSame(o, impl);
    }

    @Test
    void notImplementedOrNull_otherImpl() {
        final Interface o = new OtherImpl();
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Checks.notImplementedOrNull(o, Impl.class, "test-name"));
        assertEquals(
                "test-name must not be implemented by the user, but was implemented by com.hivemq.client2.internal.util.ChecksTest$OtherImpl.",
                exception.getMessage());
    }

    @Test
    void notImplementedOrNull_null() {
        assertNull(Checks.notImplementedOrNull(null, Impl.class, "test-name"));
    }

    @Test
    void elementsNotNull() {
        final Object[] o = new Object[]{new Object(), new Object()};
        assertSame(o, Checks.elementsNotNull(o, "test-name"));
    }

    @Test
    void elementsNotNull_elementNull() {
        final Object[] o = new Object[]{new Object(), null};
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> Checks.elementsNotNull(o, "test-name"));
        assertEquals("test-name must not contain a null element, found at index 1.", exception.getMessage());
    }

    @Test
    void elementsNotNull_null() {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> Checks.elementsNotNull(null, "test-name"));
        assertEquals("test-name must not be null.", exception.getMessage());
    }

    @Test
    void elementNotNull() {
        final Object o = new Object();
        assertSame(o, Checks.elementNotNull(o, "test-name", 123));
    }

    @Test
    void elementNotNull_null() {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> Checks.elementNotNull(null, "test-name", 123));
        assertEquals("test-name must not contain a null element, found at index 123.", exception.getMessage());
    }

    @Test
    void elementsNotImplemented() {
        final ImmutableList<Interface> o = ImmutableList.of(new Impl(), new Impl());
        final ImmutableList<Impl> impl = Checks.elementsNotImplemented(o, Impl.class, "test-name");
        assertSame(o, impl);
    }

    @Test
    void elementsNotImplemented_otherImpl() {
        final ImmutableList<Interface> o = ImmutableList.of(new Impl(), new OtherImpl());
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Checks.elementsNotImplemented(o, Impl.class, "test-name"));
        assertEquals(
                "test-name must not be implemented by the user, but was implemented by com.hivemq.client2.internal.util.ChecksTest$OtherImpl.",
                exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE})
    void unsignedShort_true(final int value) {
        assertEquals(value, Checks.unsignedShort(value, "test-name"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + 1})
    void unsignedShort_false(final int value) {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Checks.unsignedShort(value, "test-name"));
        assertEquals("test-name must not exceed the value range of unsigned short [0, " +
                UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + "], but was " + value + ".", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE})
    void unsignedShortNotZero_true(final int value) {
        assertEquals(value, Checks.unsignedShortNotZero(value, "test-name"));
    }

    @Test
    void unsignedShortNotZero_zero() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Checks.unsignedShortNotZero(0, "test-name"));
        assertEquals("test-name must not be zero.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + 1})
    void unsignedShortNotZero_false(final int value) {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Checks.unsignedShortNotZero(value, "test-name"));
        assertEquals("test-name must not exceed the value range of unsigned short [0, " +
                UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + "], but was " + value + ".", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE})
    void unsignedInt_true(final long value) {
        assertEquals(value, Checks.unsignedInt(value, "test-name"));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE + 1})
    void unsignedInt_false(final long value) {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Checks.unsignedInt(value, "test-name"));
        assertEquals("test-name must not exceed the value range of unsigned int [0, " +
                UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE + "], but was " + value + ".", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 9})
    void index_true(final int value) {
        assertEquals(value, Checks.index(value, 10));
    }

    @Test
    void index_negative() {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.index(-1, 10));
        assertEquals("Index must not be smaller than 0, but was -1.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 11})
    void index_tooBig(final int value) {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.index(value, 10));
        assertEquals("Index must not be greater than or equal to the size (10), but was " + value + ".",
                exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void cursorIndex_true(final int value) {
        assertEquals(value, Checks.cursorIndex(value, 10));
    }

    @Test
    void cursorIndex_negative() {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.cursorIndex(-1, 10));
        assertEquals("Cursor index must not be smaller than 0, but was -1.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 12})
    void cursorIndex_tooBig(final int value) {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.cursorIndex(value, 10));
        assertEquals("Cursor index must not be greater than the size (10), but was " + value + ".",
                exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"0, 10", "2, 8"})
    void indexRange_true(final int start, final int end) {
        Checks.indexRange(start, end, 10);
    }

    @Test
    void indexRange_negative() {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.indexRange(-1, 9, 10));
        assertEquals("Start index must not be smaller than 0, but was -1.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 12})
    void indexRange_tooBig(final int end) {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.indexRange(0, end, 10));
        assertEquals("End index must not be greater than or equal to the size (10), but was " + end + ".",
                exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"5, 4", "11, 9"})
    void indexRange_startBiggerThanEnd(final int start, final int end) {
        final IndexOutOfBoundsException exception =
                assertThrows(IndexOutOfBoundsException.class, () -> Checks.indexRange(start, end, 10));
        assertEquals("Start index must not be greater than the end index, but " + start + " > " + end + ".",
                exception.getMessage());
    }

    @Test
    void state_true() {
        Checks.state(true, "test-message");
    }

    @Test
    void state_false() {
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> Checks.state(false, "test-message"));
        assertEquals("test-message", exception.getMessage());
    }

    @Test
    void stateNotNull() {
        final Object o = new Object();
        assertSame(o, Checks.stateNotNull(o, "test-name"));
    }

    @Test
    void stateNotNull_null() {
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> Checks.stateNotNull(null, "test-name"));
        assertEquals("test-name must not be null. This must not happen and is a bug.", exception.getMessage());
    }

    private interface Interface {}

    private static class Impl implements Interface {}

    private static class OtherImpl implements Interface {}
}
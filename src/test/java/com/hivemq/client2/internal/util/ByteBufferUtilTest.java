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
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ByteBufferUtilTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void allocate(final boolean direct) {
        final ByteBuffer empty = ByteBufferUtil.allocate(0, direct);
        assertEquals(direct, empty.isDirect());
        assertEquals(0, empty.remaining());
        assertEquals(0, empty.capacity());
        final ByteBuffer buffer = ByteBufferUtil.allocate(10, direct);
        assertEquals(direct, buffer.isDirect());
        assertEquals(10, buffer.remaining());
        assertEquals(10, buffer.capacity());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void allocate_negative_throws(final boolean direct) {
        assertThrows(IllegalArgumentException.class, () -> ByteBufferUtil.allocate(-1, direct));
    }

    @Test
    void wrap_notNull() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBufferUtil.wrap(bytes);
        assertNotNull(buffer);
        assertEquals(ByteBuffer.wrap(bytes), buffer);
    }

    @Test
    void wrap_null_null() {
        //noinspection ConstantConditions
        assertNull(ByteBufferUtil.wrap(null));
    }

    @Test
    void slice_notNull() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final ByteBuffer sliced = ByteBufferUtil.slice(buffer);
        assertNotNull(sliced);
        assertEquals(buffer.slice(), sliced);
    }

    @Test
    void slice_null_null() {
        assertNull(ByteBufferUtil.slice(null));
    }

    @Test
    void optionalReadOnly_notNull() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final Optional<ByteBuffer> bufferOptional = ByteBufferUtil.optionalReadOnly(buffer);
        assertNotNull(bufferOptional);
        assertTrue(bufferOptional.isPresent());
        assertTrue(bufferOptional.get().isReadOnly());
        assertEquals(buffer, bufferOptional.get());
    }

    @Test
    void optionalReadOnly_null() {
        final Optional<ByteBuffer> bufferOptional = ByteBufferUtil.optionalReadOnly(null);
        assertNotNull(bufferOptional);
        assertFalse(bufferOptional.isPresent());
    }

    @Test
    void getBytes_heapBufferNoPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final byte[] gotBytes = ByteBufferUtil.getBytes(buffer);
        assertSame(buffer.array(), gotBytes);
        assertEquals(0, buffer.position());
    }

    @Test
    void getBytes_heapBufferPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(1);
        final byte[] gotBytes = ByteBufferUtil.getBytes(buffer);
        assertNotSame(buffer.array(), gotBytes);
        assertArrayEquals(Arrays.copyOfRange(bytes, 1, bytes.length), gotBytes);
        assertEquals(1, buffer.position());
    }

    @Test
    void getBytes_directBufferNoPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        final byte[] gotBytes = ByteBufferUtil.getBytes(buffer);
        assertArrayEquals(bytes, gotBytes);
        assertEquals(0, buffer.position());
    }

    @Test
    void getBytes_directBufferPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        buffer.position(1);
        final byte[] gotBytes = ByteBufferUtil.getBytes(buffer);
        assertArrayEquals(Arrays.copyOfRange(bytes, 1, bytes.length), gotBytes);
        assertEquals(1, buffer.position());
    }

    @Test
    void copyBytes_heapBufferNoPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final byte[] copiedBytes = ByteBufferUtil.copyBytes(buffer);
        assertNotSame(buffer.array(), copiedBytes);
        assertEquals(0, buffer.position());
    }

    @Test
    void copyBytes_heapBufferPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(1);
        final byte[] copiedBytes = ByteBufferUtil.copyBytes(buffer);
        assertNotSame(buffer.array(), copiedBytes);
        assertArrayEquals(Arrays.copyOfRange(bytes, 1, bytes.length), copiedBytes);
        assertEquals(1, buffer.position());
    }

    @Test
    void copyBytes_directBufferNoPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        final byte[] copiedBytes = ByteBufferUtil.copyBytes(buffer);
        assertArrayEquals(bytes, copiedBytes);
        assertEquals(0, buffer.position());
    }

    @Test
    void copyBytes_directBufferPosition() {
        final byte[] bytes = {0, 1, 2, 3, 4};
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        buffer.position(1);
        final byte[] copiedBytes = ByteBufferUtil.copyBytes(buffer);
        assertArrayEquals(Arrays.copyOfRange(bytes, 1, bytes.length), copiedBytes);
        assertEquals(1, buffer.position());
    }

    @Test
    void copyBytes_null() {
        final byte[] bytes1 = ByteBufferUtil.copyBytes(null);
        final byte[] bytes2 = ByteBufferUtil.copyBytes(null);
        assertEquals(0, bytes1.length);
        assertSame(bytes1, bytes2);
    }
}
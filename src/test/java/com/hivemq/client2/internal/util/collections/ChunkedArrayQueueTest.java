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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ChunkedArrayQueueTest {

    @Test
    void poll_not_present() {
        final ChunkedArrayQueue<String> queue = new ChunkedArrayQueue<>(8);
        assertNull(queue.poll());
        queue.offer("test");
        assertEquals("test", queue.poll());
        assertNull(queue.poll());
    }

    @CsvSource({"0, 1", "0, 4", "2, 4", "0, 8", "3, 8", "12, 20"})
    @ParameterizedTest
    void producer_consumer(final int minFill, final int iteration) {
        final ChunkedArrayQueue<String> queue = new ChunkedArrayQueue<>(8);
        int produced = 0;
        int consumed = 0;
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        while (produced < minFill) {
            queue.offer("test" + produced++);
        }
        assertEquals(minFill, queue.size());
        assertEquals(minFill == 0, queue.isEmpty());
        for (int i = iteration; i <= iteration * 100; i += iteration) {
            while (produced < i + minFill) {
                queue.offer("test" + produced++);
            }
            assertEquals(iteration + minFill, queue.size());
            assertFalse(queue.isEmpty());
            while (consumed < i) {
                assertEquals("test" + consumed, queue.peek());
                assertEquals("test" + consumed++, queue.poll());
            }
            assertEquals(minFill, queue.size());
            assertEquals(minFill == 0, queue.isEmpty());
        }
        while (consumed < iteration * 100 + minFill) {
            assertEquals("test" + consumed, queue.peek());
            assertEquals("test" + consumed++, queue.poll());
        }
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

}
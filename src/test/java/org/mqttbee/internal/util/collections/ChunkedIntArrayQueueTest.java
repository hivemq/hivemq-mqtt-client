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

package org.mqttbee.internal.util.collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class ChunkedIntArrayQueueTest {

    @CsvSource({"0, 1", "0, 4", "2, 4", "0, 8", "3, 8", "12, 20"})
    @ParameterizedTest
    void producer_consumer(final int minFill, final int iteration) {
        final ChunkedIntArrayQueue queue = new ChunkedIntArrayQueue(8);
        int produced = 0;
        int consumed = 0;
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        while (produced < minFill) {
            queue.offer(produced++);
        }
        assertEquals(minFill, queue.size());
        assertEquals(minFill == 0, queue.isEmpty());
        for (int i = iteration; i <= iteration * 100; i += iteration) {
            while (produced < i + minFill) {
                queue.offer(produced++);
            }
            assertEquals(iteration + minFill, queue.size());
            assertFalse(queue.isEmpty());
            while (consumed < i) {
                assertEquals(consumed, queue.peek(-1));
                assertEquals(consumed++, queue.poll(-1));
            }
            assertEquals(minFill, queue.size());
            assertEquals(minFill == 0, queue.isEmpty());
        }
        while (consumed < iteration * 100 + minFill) {
            assertEquals(consumed, queue.peek(-1));
            assertEquals(consumed++, queue.poll(-1));
        }
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @ValueSource(ints = {0, 1, 2, 7, 8, 9})
    @ParameterizedTest
    void removeFirst_present(final int removeValue) {
        final ChunkedIntArrayQueue queue = new ChunkedIntArrayQueue(8);
        for (int i = 0; i < 10; i++) {
            queue.offer(i);
        }
        assertTrue(queue.removeFirst(removeValue));
        for (int i = 0; i < 10; i++) {
            if (i != removeValue) {
                assertEquals(i, queue.poll(-1));
            }
        }
    }

    @ValueSource(ints = {-1, 10})
    @ParameterizedTest
    void removeFirst_notPresent(final int removeValue) {
        final ChunkedIntArrayQueue queue = new ChunkedIntArrayQueue(8);
        for (int i = 0; i < 10; i++) {
            queue.offer(i);
        }
        assertFalse(queue.removeFirst(removeValue));
        for (int i = 0; i < 10; i++) {
            assertEquals(i, queue.poll(-1));
        }
    }

}
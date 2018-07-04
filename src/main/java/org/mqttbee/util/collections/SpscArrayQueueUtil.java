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

package org.mqttbee.util.collections;

import java.util.Queue;
import org.jctools.queues.SpscArrayQueue;
import org.jctools.queues.SpscChunkedArrayQueue;
import org.mqttbee.annotations.NotNull;

/** @author Silvio Giebl */
public class SpscArrayQueueUtil {

    @NotNull
    public static <E> Queue<E> create(int capacity, int chunkSize) {
        chunkSize = roundToPowerOf2(chunkSize);
        chunkSize = Math.max(8, chunkSize);
        if (capacity <= chunkSize) {
            return new SpscArrayQueue<>(capacity);
        }
        capacity =
                roundToPowerOf2(
                        capacity); // capacity is at least 9, so next power of two is at least 16
        return new SpscChunkedArrayQueue<>(chunkSize, capacity);
    }

    private static int roundToPowerOf2(final int value) {
        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }
}

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

import org.jctools.queues.SpscChunkedArrayQueue;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class SpscChunkedArrayQueueUtil {

    @NotNull
    public static <E> SpscChunkedArrayQueue<E> create(int capacity, int chunkSize) {
        capacity = Math.max(16, capacity);
        chunkSize = Math.min(chunkSize, capacity / 2);
        return new SpscChunkedArrayQueue<>(chunkSize, capacity);
    }

}

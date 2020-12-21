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

package com.hivemq.client2.rx.reactivestreams;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * A {@link Subscriber} which consumes also a single item of type {@link S} by {@link #onSingle(Object)} besides items
 * of type {@link T} by {@link #onNext(Object)}.
 *
 * @author Silvio Giebl
 * @see Subscriber
 */
public interface WithSingleSubscriber<T, S> extends Subscriber<T> {

    /**
     * Single item sent by the {@link PublisherWithSingle}.
     *
     * @param s the single item.
     */
    void onSingle(@NotNull S s);
}

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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * A {@link Publisher} which emits a flow of items of type {@link T} and a single item of type {@link S}.
 *
 * @param <T> the type of the stream of items.
 * @param <S> the type of the single item.
 * @author Silvio Giebl
 * @see Publisher
 */
public interface PublisherWithSingle<T, S> extends Publisher<T> {

    /**
     * {@link Publisher#subscribe(Subscriber) Subscribes} to this {@link PublisherWithSingle}.
     * <p>
     * In addition to signalling the stream of items via {@link WithSingleSubscriber#onNext(Object) onNext}, the single
     * item is signalled via {@link WithSingleSubscriber#onSingle(Object) onSingle}.
     *
     * @param subscriber the {@link WithSingleSubscriber} that will consume signals from this {@link
     *                   PublisherWithSingle}.
     * @see Publisher#subscribe(Subscriber)
     */
    void subscribeBoth(@NotNull WithSingleSubscriber<? super T, ? super S> subscriber);
}

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

package com.hivemq.client2.rx.reactor;

import com.hivemq.client2.rx.reactivestreams.PublisherWithSingle;
import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;

/**
 * A {@link CoreWithSingleSubscriber} aware {@link PublisherWithSingle}.
 * <p>
 * {@inheritDoc}
 *
 * @author Silvio Giebl
 * @see PublisherWithSingle
 * @see CorePublisher
 * @since 1.2
 */
public interface CorePublisherWithSingle<T, S> extends PublisherWithSingle<T, S>, CorePublisher<T> {

    /**
     * {@link PublisherWithSingle#subscribeBoth(WithSingleSubscriber) Subscribes} to this {@link
     * CorePublisherWithSingle}.
     * <p>
     * In addition to behave as expected by {@link Publisher#subscribe(Subscriber)} in a controlled manner, it supports
     * direct subscribe-time {@link reactor.util.context.Context Context} passing.
     *
     * @param subscriber the {@link Subscriber} interested into the published sequence.
     * @see PublisherWithSingle#subscribeBoth(WithSingleSubscriber)
     * @see CorePublisher#subscribe(CoreSubscriber)
     */
    void subscribeBoth(@NotNull CoreWithSingleSubscriber<? super T, ? super S> subscriber);
}

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

package com.hivemq.client.internal.rx.operators;

import com.hivemq.client.rx.FlowableWithSingle;
import com.hivemq.client.rx.reactivestreams.PublisherWithSingle;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class FlowableWithSingleFrom<F, S> extends FlowableWithSingle<F, S> {

    private final @NotNull PublisherWithSingle<? extends F, ? extends S> source;

    public FlowableWithSingleFrom(final @NotNull PublisherWithSingle<? extends F, ? extends S> source) {
        this.source = source;
    }

    @Override
    public void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
        source.subscribe(subscriber);
    }

    @Override
    public void subscribeBothActual(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        source.subscribeBoth(subscriber);
    }
}

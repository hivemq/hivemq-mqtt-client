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

package com.hivemq.client2.internal.rx.reactor.operators;

import com.hivemq.client2.rx.reactivestreams.PublisherWithSingle;
import com.hivemq.client2.rx.reactor.CoreWithSingleSubscriber;
import com.hivemq.client2.rx.reactor.FluxWithSingle;
import org.jetbrains.annotations.NotNull;
import reactor.core.CoreSubscriber;

/**
 * @author Silvio Giebl
 */
public class FluxWithSingleFrom<F, S> extends FluxWithSingle<F, S> {

    private final @NotNull PublisherWithSingle<? extends F, ? extends S> source;

    public FluxWithSingleFrom(final @NotNull PublisherWithSingle<? extends F, ? extends S> source) {
        this.source = source;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super F> subscriber) {
        source.subscribe(subscriber);
    }

    @Override
    public void subscribeBoth(final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {
        source.subscribeBoth(subscriber);
    }
}

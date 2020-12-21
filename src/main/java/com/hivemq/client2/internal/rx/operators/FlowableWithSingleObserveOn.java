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

package com.hivemq.client2.internal.rx.operators;

import com.hivemq.client2.rx.FlowableWithSingle;
import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.rxjava3.core.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class FlowableWithSingleObserveOn<F, S> extends FlowableWithSingleOperator<F, S, F, S> {

    private final @NotNull Scheduler scheduler;
    private final boolean delayError;
    private final int bufferSize;

    public FlowableWithSingleObserveOn(
            final @NotNull FlowableWithSingle<F, S> source,
            final @NotNull Scheduler scheduler,
            final boolean delayError,
            final int bufferSize) {

        super(source);
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.bufferSize = bufferSize;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
        source.observeOn(scheduler, delayError, bufferSize).subscribe(subscriber);
    }

    @Override
    protected void subscribeBothActual(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        FlowableWithSingleCombine.split(
                new FlowableWithSingleCombine<>(source).observeOn(scheduler, delayError, bufferSize), subscriber);
    }
}

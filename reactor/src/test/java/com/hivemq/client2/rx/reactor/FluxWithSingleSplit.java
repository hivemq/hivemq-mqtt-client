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

import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author Silvio Giebl
 */
public class FluxWithSingleSplit<U, F, S> extends FluxWithSingle<F, S> {

    private final @NotNull Flux<U> source;
    private final @NotNull Class<F> flowableClass;
    private final @NotNull Class<S> singleClass;

    public FluxWithSingleSplit(
            final @NotNull Flux<U> source, final @NotNull Class<F> flowableClass, final @NotNull Class<S> singleClass) {

        this.source = source;
        this.flowableClass = flowableClass;
        this.singleClass = singleClass;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super F> subscriber) {
        source.subscribe(new SplitSubscriber<>(subscriber, flowableClass, singleClass));
    }

    @Override
    public void subscribeBoth(final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {
        source.subscribe(new SplitSubscriber<>(subscriber, flowableClass, singleClass));
    }

    private static class SplitSubscriber<U, F, S> implements Subscriber<U>, Subscription {

        private final @NotNull Subscriber<? super F> subscriber;
        private final @NotNull Class<F> flowableClass;
        private final @NotNull Class<S> singleClass;
        private @Nullable Subscription subscription;

        SplitSubscriber(
                final @NotNull Subscriber<? super F> subscriber,
                final @NotNull Class<F> flowableClass,
                final @NotNull Class<S> singleClass) {

            this.subscriber = subscriber;
            this.flowableClass = flowableClass;
            this.singleClass = singleClass;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onNext(final @NotNull U u) {
            assert subscription != null;
            if (singleClass.isInstance(u)) {
                if (subscriber instanceof WithSingleSubscriber) {
                    //noinspection unchecked
                    ((WithSingleSubscriber<F, S>) subscriber).onSingle(singleClass.cast(u));
                }
                subscription.request(1);
            } else if (flowableClass.isInstance(u)) {
                subscriber.onNext(flowableClass.cast(u));
            } else {
                subscription.request(1);
            }
        }

        @Override
        public void onError(final @NotNull Throwable error) {
            subscriber.onError(error);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }

        @Override
        public void request(final long n) {
            assert subscription != null;
            subscription.request(n);
        }

        @Override
        public void cancel() {
            assert subscription != null;
            subscription.cancel();
        }
    }
}

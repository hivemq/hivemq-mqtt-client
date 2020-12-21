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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author Silvio Giebl
 */
public class FluxWithSingleItem<F, S> extends FluxWithSingle<F, S> {

    private final @NotNull Flux<F> source;
    private final @NotNull S single;
    private final int index;

    public FluxWithSingleItem(final @NotNull Flux<F> source, final @NotNull S single, final int index) {
        this.source = source;
        this.single = single;
        this.index = index;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super F> subscriber) {
        source.subscribe(subscriber);
    }

    @Override
    public void subscribeBoth(final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {
        source.subscribe(new SingleItemSubscriber<>(subscriber, single, index));
    }

    private static class SingleItemSubscriber<F, S> implements CoreSubscriber<F>, Subscription {

        private final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber;
        private final @NotNull S single;
        private int index;
        private int currentIndex;
        private @Nullable Subscription subscription;

        SingleItemSubscriber(
                final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber,
                final @NotNull S single,
                final int index) {

            this.subscriber = subscriber;
            this.single = single;
            this.index = index;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onNext(final @NotNull F f) {
            subscriber.onNext(f);
            if (index == ++currentIndex) {
                index = -1;
                subscriber.onSingle(single);
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
            if (index == 0) {
                index = -1;
                subscriber.onSingle(single);
            }
            subscription.request(n);
        }

        @Override
        public void cancel() {
            assert subscription != null;
            subscription.cancel();
        }
    }
}

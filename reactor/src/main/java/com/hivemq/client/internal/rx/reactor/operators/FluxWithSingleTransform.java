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

package com.hivemq.client.internal.rx.reactor.operators;

import com.hivemq.client.rx.reactor.CoreWithSingleSubscriber;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class FluxWithSingleTransform<F, FT, S> extends FluxWithSingleOperator<F, S, FT, S> {

    private final @NotNull Function<? super Flux<F>, ? extends Publisher<FT>> transformer;

    public FluxWithSingleTransform(
            final @NotNull FluxWithSingle<F, S> source,
            final @NotNull Function<? super Flux<F>, ? extends Publisher<FT>> transformer) {

        super(source);
        this.transformer = transformer;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super FT> subscriber) {
        source.transform(transformer).subscribe(subscriber);
    }

    @Override
    public void subscribeBoth(final @NotNull CoreWithSingleSubscriber<? super FT, ? super S> subscriber) {
        final SerializeSubscriber<FT, S> serializeSubscriber = new SerializeSubscriber<>(subscriber);
        source.doOnSingle(serializeSubscriber).transform(transformer).subscribe(serializeSubscriber);
    }

    private static class SerializeSubscriber<F, S> implements CoreSubscriber<F>, Consumer<S>, Subscription {

        private final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber;
        private @Nullable Subscription subscription;
        private final @NotNull ConcurrentLinkedQueue<F> queue = new ConcurrentLinkedQueue<>();
        private @Nullable S single;
        private @Nullable Object done;
        private final @NotNull AtomicInteger wip = new AtomicInteger();

        SerializeSubscriber(final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void accept(final @NotNull S s) {
            single = s;
            drain();
        }

        @Override
        public void onNext(final @NotNull F f) {
            queue.offer(f);
            drain();
        }

        @Override
        public void onComplete() {
            // normally no drain necessary as onComplete happens after onSingle and all onNext
            // but the composed Flowable could complete before the FlowableWithSingle completes/errors
            done = new Object();
            drain();
        }

        @Override
        public void onError(final @NotNull Throwable error) {
            // normally no drain necessary as onError happens after onSingle and all onNext
            // but the composed Flowable could error before the FlowableWithSingle completes/errors
            done = error;
            drain();
        }

        private void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }
            int missed = 1;
            do {
                final S single = this.single;
                final Object done = this.done;
                if (single != null) {
                    this.single = null;
                    subscriber.onSingle(single);
                }
                while (true) {
                    final F f = queue.poll();
                    if (f == null) {
                        break;
                    }
                    subscriber.onNext(f);
                }
                if (done != null) {
                    this.done = null;
                    if (done instanceof Throwable) {
                        subscriber.onError((Throwable) done);
                    } else {
                        subscriber.onComplete();
                    }
                }
                missed = wip.addAndGet(-missed);
            } while (missed != 0);
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

/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.rx.operators;

import com.hivemq.client.internal.rx.WithSingleConditionalSubscriber;
import com.hivemq.client.rx.FlowableWithSingle;
import com.hivemq.client.rx.FlowableWithSingleSubscriber;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.Flowable;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.internal.util.BackpressureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
class FlowableWithSingleCombine<F, S> extends Flowable<Object> {

    private final @NotNull FlowableWithSingle<F, S> source;

    FlowableWithSingleCombine(final @NotNull FlowableWithSingle<F, S> source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Object> subscriber) {
        source.subscribeBoth(new CombineSubscriber<>(subscriber));
    }

    private static class CombineSubscriber<F, S> implements FlowableWithSingleSubscriber<F, S>, Subscription {

        private final @NotNull Subscriber<? super Object> subscriber;
        private @Nullable Subscription subscription;
        private final @NotNull AtomicLong requested = new AtomicLong();
        private final @NotNull AtomicReference<@Nullable Object> queued = new AtomicReference<>();

        CombineSubscriber(final @NotNull Subscriber<? super Object> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            next(new SingleElement(s));
        }

        @Override
        public void onNext(final @NotNull F f) {
            next(f);
        }

        private void next(final @NotNull Object next) {
            if (requested.get() == 0) {
                queued.set(next);
                if ((requested.get() != 0) && (queued.getAndSet(null)) != null) {
                    BackpressureHelper.produced(requested, 1);
                    subscriber.onNext(next);
                }
            } else {
                BackpressureHelper.produced(requested, 1);
                subscriber.onNext(next);
            }
        }

        @Override
        public void onError(final @NotNull Throwable throwable) {
            final Object next = queued.get();
            if ((next == null) || !queued.compareAndSet(next, new TerminalElement(next, throwable))) {
                subscriber.onError(throwable);
            }
        }

        @Override
        public void onComplete() {
            final Object next = queued.get();
            if ((next == null) || !queued.compareAndSet(next, new TerminalElement(next, null))) {
                subscriber.onComplete();
            }
        }

        @Override
        public void request(long n) {
            assert subscription != null;
            if (n > 0) {
                if (requested.get() == 0) {
                    final Object next = queued.getAndSet(null);
                    if (next != null) {
                        if (next instanceof TerminalElement) {
                            final TerminalElement terminalElement = (TerminalElement) next;
                            subscriber.onNext(terminalElement.element);
                            if (terminalElement.error == null) {
                                subscriber.onComplete();
                            } else {
                                subscriber.onError(terminalElement.error);
                            }
                            return;
                        } else {
                            subscriber.onNext(next);
                            n--;
                        }
                    }
                }
                if (n > 0) {
                    BackpressureHelper.add(requested, n);
                    subscription.request(n);
                }
            }
        }

        @Override
        public void cancel() {
            assert subscription != null;
            subscription.cancel();
        }
    }

    static <F, S> void split(
            final @NotNull Flowable<Object> source,
            final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {

        if (subscriber instanceof WithSingleConditionalSubscriber) {
            //noinspection unchecked
            source.subscribe(new SplitSubscriber.Conditional<>(
                    (WithSingleConditionalSubscriber<? super F, ? super S>) subscriber));
        } else {
            source.subscribe(new SplitSubscriber.Default<>(subscriber));
        }
    }

    private static abstract class SplitSubscriber<F, S, T extends WithSingleSubscriber<? super F, ? super S>>
            implements ConditionalSubscriber<Object>, Subscription {

        final @NotNull T subscriber;
        private @Nullable Subscription subscription;

        SplitSubscriber(final @NotNull T subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onNext(final @NotNull Object o) {
            if (!tryOnNext(o)) {
                assert subscription != null;
                subscription.request(1);
            }
        }

        @Override
        public boolean tryOnNext(final @NotNull Object o) {
            if (o instanceof SingleElement) {
                //noinspection unchecked
                subscriber.onSingle((S) ((SingleElement) o).element);
                return false;
            }
            //noinspection unchecked
            return tryOnNextActual((F) o);
        }

        abstract boolean tryOnNextActual(final @NotNull F f);

        @Override
        public void onError(final @NotNull Throwable throwable) {
            subscriber.onError(throwable);
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

        private static class Default<F, S> extends SplitSubscriber<F, S, WithSingleSubscriber<? super F, ? super S>> {

            Default(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
                super(subscriber);
            }

            @Override
            boolean tryOnNextActual(final @NotNull F f) {
                subscriber.onNext(f);
                return true;
            }
        }

        private static class Conditional<F, S>
                extends SplitSubscriber<F, S, WithSingleConditionalSubscriber<? super F, ? super S>> {

            Conditional(final @NotNull WithSingleConditionalSubscriber<? super F, ? super S> subscriber) {
                super(subscriber);
            }

            @Override
            boolean tryOnNextActual(final @NotNull F f) {
                return subscriber.tryOnNext(f);
            }
        }
    }

    private static class SingleElement {

        final @NotNull Object element;

        SingleElement(final @NotNull Object element) {
            this.element = element;
        }
    }

    private static class TerminalElement {

        final @NotNull Object element;
        final @Nullable Throwable error;

        TerminalElement(final @NotNull Object element, final @Nullable Throwable error) {
            this.element = element;
            this.error = error;
        }
    }
}

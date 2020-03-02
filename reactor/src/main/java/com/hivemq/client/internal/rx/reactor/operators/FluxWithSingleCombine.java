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

package com.hivemq.client.internal.rx.reactor.operators;

import com.hivemq.client.internal.rx.reactor.CoreWithSingleConditionalSubscriber;
import com.hivemq.client.rx.reactor.CoreWithSingleSubscriber;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Silvio Giebl
 */
class FluxWithSingleCombine<F, S> extends Flux<Object> {

    private final @NotNull FluxWithSingle<F, S> source;

    FluxWithSingleCombine(final @NotNull FluxWithSingle<F, S> source) {
        this.source = source;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super Object> subscriber) {
        source.subscribeBoth(new CombineSubscriber<>(subscriber));
    }

    private static class CombineSubscriber<F, S> implements CoreWithSingleSubscriber<F, S>, Subscription {

        @SuppressWarnings("rawtypes")
        static final @NotNull AtomicLongFieldUpdater<CombineSubscriber> REQUESTED =
                AtomicLongFieldUpdater.newUpdater(CombineSubscriber.class, "requested");
        @SuppressWarnings("rawtypes")
        static final @NotNull AtomicReferenceFieldUpdater<CombineSubscriber, Object> QUEUED =
                AtomicReferenceFieldUpdater.newUpdater(CombineSubscriber.class, Object.class, "queued");

        private final @NotNull CoreSubscriber<? super Object> subscriber;
        private @Nullable Subscription subscription;
        volatile long requested;
        volatile @Nullable Object queued;

        CombineSubscriber(final @NotNull CoreSubscriber<? super Object> subscriber) {
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
            if (REQUESTED.get(this) == 0) {
                QUEUED.set(this, next);
                if ((REQUESTED.get(this) != 0) && (QUEUED.getAndSet(this, null)) != null) {
                    Operators.produced(REQUESTED, this, 1);
                    subscriber.onNext(next);
                }
            } else {
                Operators.produced(REQUESTED, this, 1);
                subscriber.onNext(next);
            }
        }

        @Override
        public void onError(final @NotNull Throwable throwable) {
            final Object next = QUEUED.get(this);
            if ((next == null) || !QUEUED.compareAndSet(this, next, new TerminalElement(next, throwable))) {
                subscriber.onError(throwable);
            }
        }

        @Override
        public void onComplete() {
            final Object next = QUEUED.get(this);
            if ((next == null) || !QUEUED.compareAndSet(this, next, new TerminalElement(next, null))) {
                subscriber.onComplete();
            }
        }

        @Override
        public void request(long n) {
            assert subscription != null;
            if (n > 0) {
                if (REQUESTED.get(this) == 0) {
                    final Object next = QUEUED.getAndSet(this, null);
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
                    Operators.addCap(REQUESTED, this, n);
                    subscription.request(n);
                }
            }
        }

        @Override
        public void cancel() {
            assert subscription != null;
            subscription.cancel();
        }

        @Override
        public @NotNull Context currentContext() {
            return subscriber.currentContext();
        }
    }

    static <F, S> void split(
            final @NotNull Flux<Object> source,
            final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {

        if (subscriber instanceof CoreWithSingleConditionalSubscriber) {
            //noinspection unchecked
            source.subscribe(new SplitSubscriber.Conditional<>(
                    (CoreWithSingleConditionalSubscriber<? super F, ? super S>) subscriber));
        } else {
            source.subscribe(new SplitSubscriber.Default<>(subscriber));
        }
    }

    private static abstract class SplitSubscriber<F, S, T extends CoreWithSingleSubscriber<? super F, ? super S>>
            implements Fuseable.ConditionalSubscriber<Object>, Subscription {

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

        @Override
        public @NotNull Context currentContext() {
            return subscriber.currentContext();
        }

        private static class Default<F, S>
                extends FluxWithSingleCombine.SplitSubscriber<F, S, CoreWithSingleSubscriber<? super F, ? super S>> {

            Default(final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {
                super(subscriber);
            }

            @Override
            boolean tryOnNextActual(final @NotNull F f) {
                subscriber.onNext(f);
                return true;
            }
        }

        private static class Conditional<F, S> extends
                FluxWithSingleCombine.SplitSubscriber<F, S, CoreWithSingleConditionalSubscriber<? super F, ? super S>> {

            Conditional(final @NotNull CoreWithSingleConditionalSubscriber<? super F, ? super S> subscriber) {
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

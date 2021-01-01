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

import com.hivemq.client.internal.rx.WithSingleConditionalSubscriber;
import com.hivemq.client.rx.FlowableWithSingle;
import com.hivemq.client.rx.FlowableWithSingleSubscriber;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author Silvio Giebl
 */
public class FlowableWithSingleDo<F, S> extends FlowableWithSingleOperator<F, S, F, S> {

    private final @NotNull Consumer<? super S> singleConsumer;

    public FlowableWithSingleDo(
            final @NotNull FlowableWithSingle<F, S> source, final @NotNull Consumer<? super S> singleConsumer) {

        super(source);
        this.singleConsumer = singleConsumer;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            //noinspection unchecked
            final ConditionalSubscriber<? super F> conditional = (ConditionalSubscriber<? super F>) subscriber;
            source.subscribeBoth(new DoSubscriber.Conditional<>(conditional, singleConsumer));
        } else {
            source.subscribeBoth(new DoSubscriber<>(subscriber, singleConsumer));
        }
    }

    @Override
    protected void subscribeBothActual(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        if (subscriber instanceof WithSingleConditionalSubscriber) {
            //noinspection unchecked
            final WithSingleConditionalSubscriber<? super F, ? super S> conditional =
                    (WithSingleConditionalSubscriber<? super F, ? super S>) subscriber;
            source.subscribeBoth(new WithSingleDoSubscriber.Conditional<>(conditional, singleConsumer));
        } else {
            source.subscribeBoth(new WithSingleDoSubscriber<>(subscriber, singleConsumer));
        }
    }

    private static class DoSubscriber<F, S, T extends Subscriber<? super F>>
            implements FlowableWithSingleSubscriber<F, S>, Subscription {

        final @NotNull T subscriber;
        private final @NotNull Consumer<? super S> singleConsumer;
        private @Nullable Subscription subscription;

        DoSubscriber(final @NotNull T subscriber, final @NotNull Consumer<? super S> singleConsumer) {
            this.subscriber = subscriber;
            this.singleConsumer = singleConsumer;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            try {
                singleConsumer.accept(s);
            } catch (final Throwable throwable) {
                fail(throwable);
                return;
            }
            onSingleActual(s);
        }

        void onSingleActual(final @NotNull S s) {}

        private void fail(final @NotNull Throwable throwable) {
            assert subscription != null;
            Exceptions.throwIfFatal(throwable);
            subscription.cancel();
            onError(throwable);
        }

        @Override
        public void onNext(final @NotNull F f) {
            subscriber.onNext(f);
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

        private static class Conditional<F, S, T extends ConditionalSubscriber<? super F>> extends DoSubscriber<F, S, T>
                implements WithSingleConditionalSubscriber<F, S> {

            Conditional(final @NotNull T subscriber, final @NotNull Consumer<? super S> singleConsumer) {
                super(subscriber, singleConsumer);
            }

            @Override
            public boolean tryOnNext(final @NotNull F f) {
                return subscriber.tryOnNext(f);
            }
        }
    }

    private static class WithSingleDoSubscriber<F, S>
            extends DoSubscriber<F, S, WithSingleSubscriber<? super F, ? super S>> {

        WithSingleDoSubscriber(
                final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber,
                final @NotNull Consumer<? super S> singleConsumer) {

            super(subscriber, singleConsumer);
        }

        @Override
        void onSingleActual(final @NotNull S s) {
            subscriber.onSingle(s);
        }

        private static class Conditional<F, S>
                extends DoSubscriber.Conditional<F, S, WithSingleConditionalSubscriber<? super F, ? super S>> {

            Conditional(
                    final @NotNull WithSingleConditionalSubscriber<? super F, ? super S> subscriber,
                    final @NotNull Consumer<? super S> singleConsumer) {

                super(subscriber, singleConsumer);
            }

            @Override
            void onSingleActual(final @NotNull S s) {
                subscriber.onSingle(s);
            }
        }
    }
}

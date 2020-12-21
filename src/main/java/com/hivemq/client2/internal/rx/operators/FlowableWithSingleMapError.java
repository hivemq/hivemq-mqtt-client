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

import com.hivemq.client2.internal.rx.WithSingleConditionalSubscriber;
import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.rx.FlowableWithSingle;
import com.hivemq.client2.rx.FlowableWithSingleSubscriber;
import com.hivemq.client2.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.fuseable.ConditionalSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author Silvio Giebl
 */
public class FlowableWithSingleMapError<F, S> extends FlowableWithSingleOperator<F, S, F, S> {

    private final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper;

    public FlowableWithSingleMapError(
            final @NotNull FlowableWithSingle<F, S> source,
            final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper) {

        super(source);
        this.errorMapper = errorMapper;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            //noinspection unchecked
            final ConditionalSubscriber<? super F> conditional = (ConditionalSubscriber<? super F>) subscriber;
            source.subscribe(new MapErrorSubscriber.Conditional<>(conditional, errorMapper));
        } else {
            source.subscribe(new MapErrorSubscriber<>(subscriber, errorMapper));
        }
    }

    @Override
    protected void subscribeBothActual(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        if (subscriber instanceof WithSingleConditionalSubscriber) {
            //noinspection unchecked
            final WithSingleConditionalSubscriber<? super F, ? super S> conditional =
                    (WithSingleConditionalSubscriber<? super F, ? super S>) subscriber;
            source.subscribeBoth(new WithSingleMapErrorSubscriber.Conditional<>(conditional, errorMapper));
        } else {
            source.subscribeBoth(new WithSingleMapErrorSubscriber<>(subscriber, errorMapper));
        }
    }

    private static class MapErrorSubscriber<F, T extends Subscriber<? super F>>
            implements FlowableSubscriber<F>, Subscription {

        final @NotNull T subscriber;
        private final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper;
        private @Nullable Subscription subscription;

        MapErrorSubscriber(
                final @NotNull T subscriber,
                final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper) {

            this.subscriber = subscriber;
            this.errorMapper = errorMapper;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onNext(final @NotNull F f) {
            subscriber.onNext(f);
        }

        @Override
        public void onError(final @NotNull Throwable error) {
            Throwable em;
            try {
                em = Checks.notNull(errorMapper.apply(error), "Mapped exception");
            } catch (final Throwable throwable) {
                Exceptions.throwIfFatal(throwable);
                em = new CompositeException(error, throwable);
            }
            subscriber.onError(em);
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

        private static class Conditional<F, T extends ConditionalSubscriber<? super F>> extends MapErrorSubscriber<F, T>
                implements ConditionalSubscriber<F> {

            Conditional(
                    final @NotNull T subscriber,
                    final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper) {

                super(subscriber, errorMapper);
            }

            @Override
            public boolean tryOnNext(final @NotNull F f) {
                return subscriber.tryOnNext(f);
            }
        }
    }

    private static class WithSingleMapErrorSubscriber<F, S>
            extends MapErrorSubscriber<F, WithSingleSubscriber<? super F, ? super S>>
            implements FlowableWithSingleSubscriber<F, S> {

        WithSingleMapErrorSubscriber(
                final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber,
                final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper) {

            super(subscriber, errorMapper);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            subscriber.onSingle(s);
        }

        private static class Conditional<F, S>
                extends MapErrorSubscriber.Conditional<F, WithSingleConditionalSubscriber<? super F, ? super S>>
                implements WithSingleConditionalSubscriber<F, S> {

            Conditional(
                    final @NotNull WithSingleConditionalSubscriber<? super F, ? super S> subscriber,
                    final @NotNull Function<? super Throwable, ? extends Throwable> errorMapper) {

                super(subscriber, errorMapper);
            }

            @Override
            public void onSingle(final @NotNull S s) {
                subscriber.onSingle(s);
            }
        }
    }
}

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
public class FlowableWithSingleMap<F, S, FM, SM> extends FlowableWithSingleOperator<F, S, FM, SM> {

    public static <F, S, FM, SM> @NotNull FlowableWithSingleMap<F, S, FM, SM> mapBoth(
            final @NotNull FlowableWithSingle<F, S> source,
            final @Nullable Function<? super F, ? extends FM> flowableMapper,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        return new FlowableWithSingleMap<>(source, flowableMapper, singleMapper);
    }

    public static <F, S, SM> @NotNull FlowableWithSingleMap<F, S, F, SM> mapSingle(
            final @NotNull FlowableWithSingle<F, S> source,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        return new FlowableWithSingleMap<>(source, null, singleMapper);
    }

    private final @Nullable Function<? super F, ? extends FM> flowableMapper;
    private final @NotNull Function<? super S, ? extends SM> singleMapper;

    private FlowableWithSingleMap(
            final @NotNull FlowableWithSingle<F, S> source,
            final @Nullable Function<? super F, ? extends FM> flowableMapper,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        super(source);
        this.flowableMapper = flowableMapper;
        this.singleMapper = singleMapper;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super FM> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            //noinspection unchecked
            final ConditionalSubscriber<? super FM> conditional = (ConditionalSubscriber<? super FM>) subscriber;
            source.subscribeBoth(new MapSubscriber.Conditional<>(conditional, flowableMapper, singleMapper));
        } else {
            source.subscribeBoth(new MapSubscriber<>(subscriber, flowableMapper, singleMapper));
        }
    }

    @Override
    protected void subscribeBothActual(final @NotNull WithSingleSubscriber<? super FM, ? super SM> subscriber) {
        if (subscriber instanceof WithSingleConditionalSubscriber) {
            //noinspection unchecked
            final WithSingleConditionalSubscriber<? super FM, ? super SM> conditional =
                    (WithSingleConditionalSubscriber<? super FM, ? super SM>) subscriber;
            source.subscribeBoth(new WithSingleMapSubscriber.Conditional<>(conditional, flowableMapper, singleMapper));
        } else {
            source.subscribeBoth(new WithSingleMapSubscriber<>(subscriber, flowableMapper, singleMapper));
        }
    }

    private static class MapSubscriber<F, S, FM, SM, T extends Subscriber<? super FM>>
            implements FlowableWithSingleSubscriber<F, S>, Subscription {

        final @NotNull T subscriber;
        final @Nullable Function<? super F, ? extends FM> flowableMapper;
        private final @NotNull Function<? super S, ? extends SM> singleMapper;
        private @Nullable Subscription subscription;

        MapSubscriber(
                final @NotNull T subscriber,
                final @Nullable Function<? super F, ? extends FM> flowableMapper,
                final @NotNull Function<? super S, ? extends SM> singleMapper) {

            this.subscriber = subscriber;
            this.flowableMapper = flowableMapper;
            this.singleMapper = singleMapper;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            final SM sm;
            try {
                sm = Checks.notNull(singleMapper.apply(s), "Mapped single value");
            } catch (final Throwable throwable) {
                fail(throwable);
                return;
            }
            onSingleMapped(sm);
        }

        void onSingleMapped(final @NotNull SM sm) {}

        @Override
        public void onNext(final @NotNull F f) {
            if (flowableMapper == null) {
                //noinspection unchecked
                subscriber.onNext((FM) f);
            } else {
                final FM fm;
                try {
                    fm = Checks.notNull(flowableMapper.apply(f), "Mapped value");
                } catch (final Throwable throwable) {
                    fail(throwable);
                    return;
                }
                subscriber.onNext(fm);
            }
        }

        void fail(final @NotNull Throwable throwable) {
            assert subscription != null;
            Exceptions.throwIfFatal(throwable);
            subscription.cancel();
            onError(throwable);
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

        private static class Conditional<F, S, FM, SM, T extends ConditionalSubscriber<? super FM>>
                extends MapSubscriber<F, S, FM, SM, T> implements WithSingleConditionalSubscriber<F, S> {

            Conditional(
                    final @NotNull T subscriber,
                    final @Nullable Function<? super F, ? extends FM> flowableMapper,
                    final @NotNull Function<? super S, ? extends SM> singleMapper) {

                super(subscriber, flowableMapper, singleMapper);
            }

            @Override
            public boolean tryOnNext(final @NotNull F f) {
                if (flowableMapper == null) {
                    //noinspection unchecked
                    return subscriber.tryOnNext((FM) f);
                } else {
                    final FM fm;
                    try {
                        fm = Checks.notNull(flowableMapper.apply(f), "Mapped value");
                    } catch (final Throwable throwable) {
                        fail(throwable);
                        return false;
                    }
                    return subscriber.tryOnNext(fm);
                }
            }
        }
    }

    private static class WithSingleMapSubscriber<F, S, FM, SM>
            extends MapSubscriber<F, S, FM, SM, WithSingleSubscriber<? super FM, ? super SM>> {

        WithSingleMapSubscriber(
                final @NotNull WithSingleSubscriber<? super FM, ? super SM> subscriber,
                final @Nullable Function<? super F, ? extends FM> flowableMapper,
                final @NotNull Function<? super S, ? extends SM> singleMapper) {

            super(subscriber, flowableMapper, singleMapper);
        }

        @Override
        void onSingleMapped(final @NotNull SM sm) {
            subscriber.onSingle(sm);
        }

        private static class Conditional<F, S, FM, SM> extends
                MapSubscriber.Conditional<F, S, FM, SM, WithSingleConditionalSubscriber<? super FM, ? super SM>> {

            Conditional(
                    final @NotNull WithSingleConditionalSubscriber<? super FM, ? super SM> subscriber,
                    final @Nullable Function<? super F, ? extends FM> flowableMapper,
                    final @NotNull Function<? super S, ? extends SM> singleMapper) {

                super(subscriber, flowableMapper, singleMapper);
            }

            @Override
            void onSingleMapped(final @NotNull SM sm) {
                subscriber.onSingle(sm);
            }
        }
    }
}

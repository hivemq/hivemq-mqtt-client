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

import com.hivemq.client.internal.rx.reactor.CoreWithSingleConditionalSubscriber;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.rx.reactor.CoreWithSingleSubscriber;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.util.context.Context;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class FluxWithSingleMap<F, S, FM, SM> extends FluxWithSingleOperator<F, S, FM, SM> {

    private final @Nullable Function<? super F, ? extends FM> fluxMapper;
    private final @NotNull Function<? super S, ? extends SM> singleMapper;

    public FluxWithSingleMap(
            final @NotNull FluxWithSingle<F, S> source,
            final @Nullable Function<? super F, ? extends FM> fluxMapper,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        super(source);
        this.fluxMapper = fluxMapper;
        this.singleMapper = singleMapper;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super FM> subscriber) {
        if (subscriber instanceof Fuseable.ConditionalSubscriber) {
            //noinspection unchecked
            final Fuseable.ConditionalSubscriber<? super FM> conditional =
                    (Fuseable.ConditionalSubscriber<? super FM>) subscriber;
            source.subscribeBoth(new MapSubscriber.Conditional<>(conditional, fluxMapper, singleMapper));
        } else {
            source.subscribeBoth(new MapSubscriber<>(subscriber, fluxMapper, singleMapper));
        }
    }

    @Override
    public void subscribeBoth(final @NotNull CoreWithSingleSubscriber<? super FM, ? super SM> subscriber) {
        if (subscriber instanceof CoreWithSingleConditionalSubscriber) {
            //noinspection unchecked
            final CoreWithSingleConditionalSubscriber<? super FM, ? super SM> conditional =
                    (CoreWithSingleConditionalSubscriber<? super FM, ? super SM>) subscriber;
            source.subscribeBoth(new WithSingleMapSubscriber.Conditional<>(conditional, fluxMapper, singleMapper));
        } else {
            source.subscribeBoth(new WithSingleMapSubscriber<>(subscriber, fluxMapper, singleMapper));
        }
    }

    private static class MapSubscriber<F, S, FM, SM, T extends CoreSubscriber<? super FM>>
            implements CoreWithSingleSubscriber<F, S>, Subscription {

        final @NotNull T subscriber;
        final @Nullable Function<? super F, ? extends FM> fluxMapper;
        private final @NotNull Function<? super S, ? extends SM> singleMapper;
        private @Nullable Subscription subscription;

        MapSubscriber(
                final @NotNull T subscriber,
                final @Nullable Function<? super F, ? extends FM> fluxMapper,
                final @NotNull Function<? super S, ? extends SM> singleMapper) {

            this.subscriber = subscriber;
            this.fluxMapper = fluxMapper;
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
            if (fluxMapper == null) {
                //noinspection unchecked
                subscriber.onNext((FM) f);
            } else {
                final FM fm;
                try {
                    fm = Checks.notNull(fluxMapper.apply(f), "Mapped value");
                } catch (final Throwable throwable) {
                    fail(throwable);
                    return;
                }
                subscriber.onNext(fm);
            }
        }

        final void fail(final @NotNull Throwable throwable) {
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

        @Override
        public @NotNull Context currentContext() {
            return subscriber.currentContext();
        }

        private static class Conditional<F, S, FM, SM, T extends Fuseable.ConditionalSubscriber<? super FM>>
                extends MapSubscriber<F, S, FM, SM, T> implements CoreWithSingleConditionalSubscriber<F, S> {

            Conditional(
                    final @NotNull T subscriber,
                    final @Nullable Function<? super F, ? extends FM> fluxMapper,
                    final @NotNull Function<? super S, ? extends SM> singleMapper) {

                super(subscriber, fluxMapper, singleMapper);
            }

            @Override
            public boolean tryOnNext(final @NotNull F f) {
                if (fluxMapper == null) {
                    //noinspection unchecked
                    return subscriber.tryOnNext((FM) f);
                } else {
                    final FM fm;
                    try {
                        fm = Checks.notNull(fluxMapper.apply(f), "Mapped value");
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
            extends MapSubscriber<F, S, FM, SM, CoreWithSingleSubscriber<? super FM, ? super SM>> {

        WithSingleMapSubscriber(
                final @NotNull CoreWithSingleSubscriber<? super FM, ? super SM> subscriber,
                final @Nullable Function<? super F, ? extends FM> fluxMapper,
                final @NotNull Function<? super S, ? extends SM> singleMapper) {

            super(subscriber, fluxMapper, singleMapper);
        }

        @Override
        void onSingleMapped(final @NotNull SM sm) {
            subscriber.onSingle(sm);
        }

        private static class Conditional<F, S, FM, SM> extends
                MapSubscriber.Conditional<F, S, FM, SM, CoreWithSingleConditionalSubscriber<? super FM, ? super SM>> {

            Conditional(
                    final @NotNull CoreWithSingleConditionalSubscriber<? super FM, ? super SM> subscriber,
                    final @Nullable Function<? super F, ? extends FM> fluxMapper,
                    final @NotNull Function<? super S, ? extends SM> singleMapper) {

                super(subscriber, fluxMapper, singleMapper);
            }

            @Override
            void onSingleMapped(final @NotNull SM sm) {
                subscriber.onSingle(sm);
            }
        }
    }
}

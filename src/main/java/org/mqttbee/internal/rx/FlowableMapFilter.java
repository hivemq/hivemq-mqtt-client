/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.internal.rx;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;

/**
 * A {@link Flowable} operator which combines the functionality of {@link Flowable#map(Function)} and {@link
 * Flowable#filter(Predicate)}.
 * <p>
 * An element is filtered out if the mapper function returns <code>null</code>.
 * <dl>
 * <dt><b>Backpressure:</b></dt>
 * <dd>The operator doesn't interfere with backpressure which is determined by the source {@code Publisher}'s
 * backpressure behavior.</dd>
 * <dt><b>Scheduler:</b></dt>
 * <dd>The operator does not operate by default on a particular {@link io.reactivex.Scheduler Scheduler}.</dd>
 * </dl>
 *
 * @author Silvio Giebl
 */
public class FlowableMapFilter<T, U> extends Flowable<T> {

    public static <T, U> @NotNull FlowableTransformer<U, T> mapFilter(
            final @NotNull Function<? super U, ? extends T> mapper) {

        return source -> new FlowableMapFilter<>(source, mapper);
    }

    public static <T, U> @NotNull Flowable<T> mapFilter(
            final @NotNull Flowable<U> source, final @NotNull Function<? super U, ? extends T> mapper) {

        return new FlowableMapFilter<>(source, mapper);
    }

    private final @NotNull Flowable<U> source;
    private final @NotNull Function<? super U, ? extends T> mapper;

    private FlowableMapFilter(
            final @NotNull Flowable<U> source, final @NotNull Function<? super U, ? extends T> mapper) {

        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super T> s) {
        if (s instanceof ConditionalSubscriber) {
            @SuppressWarnings("unchecked") final ConditionalSubscriber<? super T> cs =
                    (ConditionalSubscriber<? super T>) s;
            source.subscribe(new MapFilterConditionalSubscriber<>(cs, mapper));
        } else {
            source.subscribe(new MapFilterSubscriber<>(s, mapper));
        }
    }

    private static abstract class AbstractMapFilterSubscriber<T, U, S extends Subscriber<? super T>>
            extends FuseableSubscriber<U, T, S> implements ConditionalSubscriber<U> {

        private final @NotNull Function<? super U, ? extends T> mapper;
        private boolean done;

        private AbstractMapFilterSubscriber(
                final @NotNull S subscriber, final @NotNull Function<? super U, ? extends T> mapper) {

            super(subscriber);
            this.mapper = mapper;
        }

        @Override
        public void onNext(final @NotNull U u) {
            assert subscription != null;
            if (!tryOnNext(u)) {
                subscription.request(1);
            }
        }

        @Override
        public boolean tryOnNext(final @NotNull U u) {
            assert subscription != null;
            if (done) {
                return true;
            }
            if (sourceMode != NONE) {
                return tryOnNextActual(null);
            }
            try {
                final T t = mapper.apply(u);
                return (t != null) && tryOnNextActual(t);

            } catch (final Throwable e) {
                Exceptions.throwIfFatal(e);
                subscription.cancel();
                onError(e);
                return false;
            }
        }

        abstract boolean tryOnNextActual(@Nullable T t);

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            subscriber.onComplete();
        }

        @Override
        public void onError(final @NotNull Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            subscriber.onError(t);
        }

        @Override
        public @Nullable T poll() throws Exception {
            assert queueSubscription != null;
            for (; ; ) {
                final U u = queueSubscription.poll();
                if (u == null) {
                    return null;
                }

                final T t = mapper.apply(u);
                if (t != null) {
                    return t;
                }

                if (sourceMode == ASYNC) {
                    queueSubscription.request(1);
                }
            }
        }
    }

    private static final class MapFilterSubscriber<T, U>
            extends AbstractMapFilterSubscriber<T, U, Subscriber<? super T>> {

        MapFilterSubscriber(
                final @NotNull Subscriber<? super T> subscriber,
                final @NotNull Function<? super U, ? extends T> mapper) {

            super(subscriber, mapper);
        }

        @Override
        boolean tryOnNextActual(final @Nullable T t) {
            subscriber.onNext(t);
            return true;
        }
    }

    private static final class MapFilterConditionalSubscriber<T, U>
            extends AbstractMapFilterSubscriber<T, U, ConditionalSubscriber<? super T>> {

        MapFilterConditionalSubscriber(
                final @NotNull ConditionalSubscriber<? super T> subscriber,
                final @NotNull Function<? super U, ? extends T> mapper) {

            super(subscriber, mapper);
        }

        @Override
        boolean tryOnNextActual(final @Nullable T t) {
            return subscriber.tryOnNext(t);
        }
    }
}

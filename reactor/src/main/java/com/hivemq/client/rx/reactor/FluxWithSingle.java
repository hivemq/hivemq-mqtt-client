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

package com.hivemq.client.rx.reactor;

import com.hivemq.client.internal.rx.reactor.CoreWithSingleStrictSubscriber;
import com.hivemq.client.internal.rx.reactor.operators.FluxWithSingleFrom;
import com.hivemq.client.internal.rx.reactor.operators.FluxWithSingleMap;
import com.hivemq.client.internal.rx.reactor.operators.FluxWithSinglePublishOn;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.rx.reactivestreams.PublisherWithSingle;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link Flux} which emits a flow of items of type <code>F</code> and a single item of type <code>S</code>.
 *
 * @param <F> the type of the flow of items.
 * @param <S> the type of the single item.
 * @author Silvio Giebl
 * @since 1.2
 */
public abstract class FluxWithSingle<F, S> extends Flux<F> implements CorePublisherWithSingle<F, S> {

    public static <F, S> @NotNull FluxWithSingle<F, S> from(
            final @NotNull PublisherWithSingle<? extends F, ? extends S> source) {

        if (source instanceof FluxWithSingle) {
            //noinspection unchecked
            return (FluxWithSingle<F, S>) source;
        }
        return new FluxWithSingleFrom<>(source);
    }

    public final @NotNull FluxWithSingle<F, S> publishBothOn(final @NotNull Scheduler scheduler) {
        return publishBothOn(scheduler, Queues.SMALL_BUFFER_SIZE);
    }

    public final @NotNull FluxWithSingle<F, S> publishBothOn(final @NotNull Scheduler scheduler, final int prefetch) {
        return publishBothOn(scheduler, true, prefetch);
    }

    public final @NotNull FluxWithSingle<F, S> publishBothOn(
            final @NotNull Scheduler scheduler, final boolean delayError, final int prefetch) {

        Checks.notNull(scheduler, "Scheduler");
        return new FluxWithSinglePublishOn<>(this, scheduler, delayError, prefetch);
    }

    public final <SM> @NotNull FluxWithSingle<F, SM> mapSingle(
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        Checks.notNull(singleMapper, "Single mapper");
        return FluxWithSingleMap.mapSingle(this, singleMapper);
    }

    public final <FM, SM> @NotNull FluxWithSingle<FM, SM> mapBoth(
            final @NotNull Function<? super F, ? extends FM> fluxMapper,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        Checks.notNull(fluxMapper, "Flux mapper");
        Checks.notNull(singleMapper, "Single mapper");
        return FluxWithSingleMap.mapBoth(this, fluxMapper, singleMapper);
    }

    public final @NotNull FluxWithSingle<F, S> doOnSingle(final @NotNull Consumer<? super S> singleConsumer) {
        Checks.notNull(singleConsumer, "Single consumer");
        return FluxWithSingleMap.mapSingle(this, s -> {
            singleConsumer.accept(s);
            return s;
        });
    }

    @Override
    public final void subscribeBoth(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        if (subscriber instanceof CoreWithSingleSubscriber) {
            //noinspection unchecked
            subscribeBoth((CoreWithSingleSubscriber<? super F, ? super S>) subscriber);
        } else {
            Checks.notNull(subscriber, "Subscriber");
            subscribeBoth(new CoreWithSingleStrictSubscriber<>(subscriber));
        }
    }

    public final @NotNull CompletableFuture<S> subscribeSingleFuture() {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe();
        return future;
    }

    public final @NotNull CompletableFuture<S> subscribeSingleFuture(final @NotNull Consumer<? super F> consumer) {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe(consumer);
        return future;
    }

    public final @NotNull CompletableFuture<S> subscribeSingleFuture(
            final @NotNull Consumer<? super F> consumer, final @NotNull Consumer<? super Throwable> errorConsumer) {

        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe(consumer, errorConsumer);
        return future;
    }

    public final @NotNull CompletableFuture<S> subscribeSingleFuture(
            final @NotNull Consumer<? super F> consumer, final @NotNull Consumer<? super Throwable> errorConsumer,
            final @NotNull Runnable completeConsumer) {

        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe(consumer, errorConsumer, completeConsumer);
        return future;
    }

    public final @NotNull CompletableFuture<S> subscribeSingleFuture(
            final @NotNull Consumer<? super F> consumer, final @NotNull Consumer<? super Throwable> errorConsumer,
            final @NotNull Runnable completeConsumer, final @NotNull Context initialContext) {

        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe(consumer, errorConsumer, completeConsumer, initialContext);
        return future;
    }

    public final @NotNull CompletableFuture<S> subscribeSingleFuture(final @NotNull Subscriber<? super F> subscriber) {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe(subscriber);
        return future;
    }

    private static class SingleFutureSubscriber<F, S> extends Flux<F>
            implements CoreWithSingleSubscriber<F, S>, Subscription {

        private final @NotNull FluxWithSingle<F, S> source;
        private @Nullable Subscriber<? super F> subscriber;
        private final @NotNull AtomicReference<@Nullable Subscription> subscription = new AtomicReference<>();
        private final @NotNull AtomicReference<@Nullable CompletableFuture<S>> future =
                new AtomicReference<>(new CompletableFuture<S>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        future.set(null);
                        SingleFutureSubscriber.this.cancel();
                        return super.cancel(mayInterruptIfRunning);
                    }
                });

        SingleFutureSubscriber(final @NotNull FluxWithSingle<F, S> source) {
            this.source = source;
        }

        @NotNull CompletableFuture<S> getFutureBeforeSubscribe() {
            final CompletableFuture<S> future = this.future.get();
            assert future != null;
            return future;
        }

        @Override
        public void subscribe(final @NotNull CoreSubscriber<? super F> subscriber) {
            this.subscriber = subscriber;
            source.subscribeBoth(this);
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            assert subscriber != null;
            if (!this.subscription.compareAndSet(null, subscription)) {
                cancel(subscription);
            }
            subscriber.onSubscribe(this);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            final CompletableFuture<S> future = this.future.getAndSet(null);
            if (future != null) {
                future.complete(s);
            }
        }

        @Override
        public void onNext(final @NotNull F f) {
            assert subscriber != null;
            subscriber.onNext(f);
        }

        @Override
        public void onComplete() {
            assert subscriber != null;
            final CompletableFuture<S> future = this.future.getAndSet(null);
            if (future != null) {
                future.completeExceptionally(new NoSuchElementException());
            }
            subscriber.onComplete();
        }

        @Override
        public void onError(final @NotNull Throwable t) {
            assert subscriber != null;
            final CompletableFuture<S> future = this.future.getAndSet(null);
            if (future != null) {
                future.completeExceptionally(t);
            }
            subscriber.onError(t);
        }

        @Override
        public void request(final long n) {
            final Subscription subscription = this.subscription.get();
            assert subscription != null;
            if (subscription != this) {
                subscription.request(n);
            }
        }

        @Override
        public void cancel() {
            final Subscription subscription = this.subscription.getAndSet(this);
            if ((subscription != null) && (subscription != this)) {
                cancel(subscription);
            }
        }

        private void cancel(final @NotNull Subscription subscription) {
            subscription.cancel();
            final CompletableFuture<S> future = this.future.getAndSet(null);
            if (future != null) {
                future.cancel(false);
            }
        }
    }
}

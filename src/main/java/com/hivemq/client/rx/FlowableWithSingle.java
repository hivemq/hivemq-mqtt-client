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

package com.hivemq.client.rx;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.internal.rx.WithSingleStrictSubscriber;
import com.hivemq.client.internal.rx.operators.FlowableWithSingleMap;
import com.hivemq.client.internal.rx.operators.FlowableWithSingleMapError;
import com.hivemq.client.internal.rx.operators.FlowableWithSingleObserveOn;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.rx.reactivestreams.PublisherWithSingle;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.BackpressureKind;
import io.reactivex.annotations.BackpressureSupport;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link Flowable} which emits a flow of items of type <code>F</code> and a single item of type <code>S</code>.
 *
 * @param <F> the type of the flow of items.
 * @param <S> the type of the single item.
 * @author Silvio Giebl
 */
public abstract class FlowableWithSingle<F, S> extends Flowable<F> implements PublisherWithSingle<F, S> {

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified {@link
     * Scheduler} asynchronously with a bounded buffer of {@link #bufferSize()} slots.
     *
     * @param scheduler see {@link #observeOn(Scheduler)}.
     * @return the source {@link FlowableWithSingle} modified so that its {@link Subscriber}s are notified on the
     *         specified {@link Scheduler}.
     * @see #observeOn(Scheduler)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public final @NotNull FlowableWithSingle<F, S> observeBothOn(final @NotNull Scheduler scheduler) {
        return observeBothOn(scheduler, false, bufferSize());
    }

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified {@link
     * Scheduler} asynchronously with a bounded buffer and optionally delays onError notifications.
     *
     * @param scheduler  see {@link #observeOn(Scheduler)}.
     * @param delayError see {@link #observeOn(Scheduler)}.
     * @return the source {@link FlowableWithSingle} modified so that its {@link Subscriber}s are notified on the
     *         specified {@link Scheduler}.
     * @see #observeOn(Scheduler)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public final @NotNull FlowableWithSingle<F, S> observeBothOn(
            final @NotNull Scheduler scheduler, final boolean delayError) {

        return observeBothOn(scheduler, delayError, bufferSize());
    }

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified {@link
     * Scheduler} asynchronously with a bounded buffer of configurable size and optionally delays onError
     * notifications.
     *
     * @param scheduler  see {@link #observeOn(Scheduler)}.
     * @param delayError see {@link #observeOn(Scheduler)}.
     * @param bufferSize see {@link #observeOn(Scheduler)}.
     * @return the source {@link FlowableWithSingle} modified so that its {@link Subscriber}s are notified on the
     *         specified {@link Scheduler}.
     * @see #observeOn(Scheduler)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public final @NotNull FlowableWithSingle<F, S> observeBothOn(
            final @NotNull Scheduler scheduler, final boolean delayError, final int bufferSize) {

        Checks.notNull(scheduler, "Scheduler");
        return new FlowableWithSingleObserveOn<>(this, scheduler, delayError, bufferSize);
    }

    /**
     * Modifies the upstream so that it applies a specified function to the single item of type <code>S</code> mapping
     * it to an item of type <code>SM</code>.
     *
     * @param singleMapper the mapper function to apply to the single item.
     * @param <SM>         the type of the mapped single item.
     * @return a {@link FlowableWithSingle} that applies the mapper function to the single item.
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final <SM> @NotNull FlowableWithSingle<F, SM> mapSingle(
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        Checks.notNull(singleMapper, "Single mapper");
        return FlowableWithSingleMap.mapSingle(this, singleMapper);
    }

    /**
     * Modifies the upstream so that it applies a specified function to the flow of items of type <code>F</code> mapping
     * them to items of type <code>FM</code> and a specified function to the single item of type <code>S</code> mapping
     * it to an item of type <code>SM</code>.
     *
     * @param flowableMapper the mapper function to apply to the flow items.
     * @param singleMapper   the mapper function to apply to the single item.
     * @param <FM>           the type of the mapped flow items.
     * @param <SM>           the type of the mapped single item.
     * @return a {@link FlowableWithSingle} that applies the mapper functions to the single item and the flow items.
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final <FM, SM> @NotNull FlowableWithSingle<FM, SM> mapBoth(
            final @NotNull Function<? super F, ? extends FM> flowableMapper,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        Checks.notNull(flowableMapper, "Flowable mapper");
        Checks.notNull(singleMapper, "Single mapper");
        return FlowableWithSingleMap.mapBoth(this, flowableMapper, singleMapper);
    }

    /**
     * Modifies the upstream so that it applies a specified function to an error which can map it to a different error.
     *
     * @param mapper the mapper function to apply to an error.
     * @return a {@link FlowableWithSingle} that applies the mapper function to an error.
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull FlowableWithSingle<F, S> mapError(
            final @NotNull Function<? super Throwable, ? extends Throwable> mapper) {

        Checks.notNull(mapper, "Mapper");
        return new FlowableWithSingleMapError<>(this, mapper);
    }

    /**
     * Modifies the upstream so that it calls a consumer on emission of the single item of type <code>S</code>.
     *
     * @param singleConsumer the consumer of the single item.
     * @return a {@link FlowableWithSingle} that calls the consumer with the single item.
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull FlowableWithSingle<F, S> doOnSingle(final @NotNull Consumer<? super S> singleConsumer) {
        Checks.notNull(singleConsumer, "Single consumer");
        return FlowableWithSingleMap.mapSingle(this, s -> {
            singleConsumer.accept(s);
            return s;
        });
    }

    @Override
    @BackpressureSupport(BackpressureKind.SPECIAL)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final void subscribeBoth(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        if (subscriber instanceof FlowableWithSingleSubscriber) {
            //noinspection unchecked
            subscribeBoth((FlowableWithSingleSubscriber<? super F, ? super S>) subscriber);
        } else {
            Checks.notNull(subscriber, "Subscriber");
            subscribeBothActual(new WithSingleStrictSubscriber<>(subscriber));
        }
    }

    /**
     * Special version of {@link #subscribeBoth(WithSingleSubscriber)} with a {@link FlowableWithSingleSubscriber}.
     *
     * @param subscriber the {@link FlowableWithSingleSubscriber}.
     * @see #subscribeBoth(WithSingleSubscriber)
     */
    @BackpressureSupport(BackpressureKind.SPECIAL)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final void subscribeBoth(final @NotNull FlowableWithSingleSubscriber<? super F, ? super S> subscriber) {
        Checks.notNull(subscriber, "Subscriber");
        subscribeBothActual(subscriber);
    }

    protected abstract void subscribeBothActual(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber);

    /**
     * {@link #subscribe() Subscribes} to this Flowable and returns a future for the single item.
     * <ul>
     *   <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.
     *   <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     *     FlowableWithSingle} completes but no single item was emitted.
     *   <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     *     errors before the single item was emitted.
     *   <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already completed
     *     normally or exceptionally.
     * </ul>
     *
     * @return a future for the single item.
     * @see #subscribe()
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture() {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe();
        return future;
    }

    /**
     * {@link #subscribe(Consumer) Subscribes} to this Flowable and returns a future for the single item.
     * <ul>
     *   <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.
     *   <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     *     FlowableWithSingle} completes but no single item was emitted.
     *   <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     *     errors before the single item was emitted.
     *   <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already completed
     *     normally or exceptionally.
     * </ul>
     *
     * @param onNext see {@link #subscribe(Consumer)}
     * @return a future for the single item.
     * @see #subscribe(Consumer)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture(final @NotNull Consumer<? super F> onNext) {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        //noinspection ResultOfMethodCallIgnored
        singleFutureSubscriber.subscribe(onNext);
        return future;
    }

    /**
     * {@link #subscribe(Consumer, Consumer) Subscribes} to this Flowable and returns a future for the single item.
     * <ul>
     *   <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.
     *   <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     *     FlowableWithSingle} completes but no single item was emitted.
     *   <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     *     errors before the single item was emitted.
     *   <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already completed
     *     normally or exceptionally.
     * </ul>
     *
     * @param onNext  see {@link #subscribe(Consumer, Consumer)}
     * @param onError see {@link #subscribe(Consumer, Consumer)}
     * @return a future for the single item.
     * @see #subscribe(Consumer, Consumer)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture(
            final @NotNull Consumer<? super F> onNext, final @NotNull Consumer<? super Throwable> onError) {

        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        //noinspection ResultOfMethodCallIgnored
        singleFutureSubscriber.subscribe(onNext, onError);
        return future;
    }

    /**
     * {@link #subscribe(Consumer, Consumer, Action) Subscribes} to this Flowable and returns a future for the single
     * item.
     * <ul>
     *   <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.
     *   <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     *     FlowableWithSingle} completes but no single item was emitted.
     *   <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     *     errors before the single item was emitted.
     *   <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already completed
     *     normally or exceptionally.
     * </ul>
     *
     * @param onNext     see {@link #subscribe(Consumer, Consumer, Action)}
     * @param onError    see {@link #subscribe(Consumer, Consumer, Action)}
     * @param onComplete see {@link #subscribe(Consumer, Consumer, Action)}
     * @return a future for the single item.
     * @see #subscribe(Consumer, Consumer, Action)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture(
            final @NotNull Consumer<? super F> onNext,
            final @NotNull Consumer<? super Throwable> onError,
            final @NotNull Action onComplete) {

        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        //noinspection ResultOfMethodCallIgnored
        singleFutureSubscriber.subscribe(onNext, onError, onComplete);
        return future;
    }

    /**
     * {@link #subscribe(Subscriber) Subscribes} to this Flowable and returns a future for the single item.
     * <ul>
     *   <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.
     *   <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     *     FlowableWithSingle} completes but no single item was emitted.
     *   <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     *     errors before the single item was emitted.
     *   <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already completed
     *     normally or exceptionally.
     * </ul>
     *
     * @param subscriber see {@link #subscribe(Subscriber)}
     * @return a future for the single item.
     * @see #subscribe(Subscriber)
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture(final @NotNull Subscriber<? super F> subscriber) {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe(subscriber);
        return future;
    }

    private static class SingleFutureSubscriber<F, S> extends Flowable<F>
            implements FlowableWithSingleSubscriber<F, S>, Subscription {

        private final @NotNull FlowableWithSingle<F, S> source;
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

        SingleFutureSubscriber(final @NotNull FlowableWithSingle<F, S> source) {
            this.source = source;
        }

        @NotNull CompletableFuture<S> getFutureBeforeSubscribe() {
            final CompletableFuture<S> future = this.future.get();
            assert future != null;
            return future;
        }

        @Override
        protected void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
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

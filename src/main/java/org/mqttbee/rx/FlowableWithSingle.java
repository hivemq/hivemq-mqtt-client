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

package org.mqttbee.rx;

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
import org.mqttbee.rx.reactivestreams.PublisherWithSingle;
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
     * Splits the given source into a flow of items of type <code>F</code> and a single item of type <code>S</code>.
     *
     * @param source        the {@link Flowable} of a supertype to split.
     * @param flowableClass the class of the type of the flow of item.
     * @param singleClass   the class of the single item type.
     * @param <F>           the type of the flow of items, must not be a super-/subtype of <code>S</code>.
     * @param <S>           the type of the single item, must not be a super-/subtype of <code>F</code>.
     * @return the {@link FlowableWithSingle} emitting a flow of items of type <code>F</code> and a single item of type
     *         <code>S</code>.
     */
    public static <F, S> @NotNull FlowableWithSingle<F, S> split(
            final @NotNull Flowable<?> source, final @NotNull Class<F> flowableClass,
            final @NotNull Class<S> singleClass) {

        if (flowableClass.isAssignableFrom(singleClass) || singleClass.isAssignableFrom(flowableClass)) {
            throw new IllegalArgumentException("Flowable and single class must not be assignable to each other");
        }
        return new FlowableWithSingleSplit<>(source, flowableClass, singleClass);
    }

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified
     * Scheduler.
     *
     * @param scheduler see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public abstract @NotNull FlowableWithSingle<F, S> observeOnBoth(@NotNull Scheduler scheduler);

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified
     * Scheduler.
     *
     * @param scheduler  see {@link Flowable#observeOn(Scheduler)}.
     * @param delayError see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public abstract @NotNull FlowableWithSingle<F, S> observeOnBoth(@NotNull Scheduler scheduler, boolean delayError);

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified
     * Scheduler.
     *
     * @param scheduler  see {@link Flowable#observeOn(Scheduler)}.
     * @param delayError see {@link Flowable#observeOn(Scheduler)}.
     * @param bufferSize see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public abstract @NotNull FlowableWithSingle<F, S> observeOnBoth(
            @NotNull Scheduler scheduler, boolean delayError, int bufferSize);

    /**
     * Modifies the upstream so that it applies a specified function to the single item of type <code>S</code> mapping
     * it to an item of type <code>SM</code>.
     *
     * @param singleMapper the mapper function to apply to the single item.
     * @param <SM>         the type of the mapped single item.
     * @return a {@link FlowableWithSingle} that applies the mapper function to the single item.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract <SM> @NotNull FlowableWithSingle<F, SM> mapSingle(@NotNull Function<S, SM> singleMapper);

    /**
     * Modifies the upstream so that it applies a specified function to the flow of items of type <code>F</code> mapping
     * them to items of type <code>FM</code> and a specified function to the single item of type <code>S</code> mapping
     * it to an item of type <code>SM</code>.
     *
     * @param singleMapper   the mapper function to apply to the single item.
     * @param flowableMapper the mapper function to apply to the flow of items.
     * @param <SM>           the type of the mapped single item.
     * @param <FM>           the type of the mapped flow items.
     * @return a {@link FlowableWithSingle} that applies the mapper functions to the single item and the flow of items.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract <FM, SM> @NotNull FlowableWithSingle<FM, SM> mapBoth(
            @NotNull Function<F, FM> flowableMapper, @NotNull Function<S, SM> singleMapper);

    /**
     * Modifies the upstream so that it applies a specified function to an error which can map it to a different error.
     *
     * @param mapper the mapper function to apply to an error.
     * @return a {@link FlowableWithSingle} that applies the mapper function to an error.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract @NotNull FlowableWithSingle<F, S> mapError(@NotNull Function<Throwable, Throwable> mapper);

    /**
     * Modifies the upstream so that it calls a consumer on emission of the single item of type <code>S</code>.
     *
     * @param singleConsumer the consumer of the single item.
     * @return the modified {@link Flowable}.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract @NotNull FlowableWithSingle<F, S> doOnSingle(@NotNull Consumer<S> singleConsumer);

    /**
     * Does {@link #subscribe()} to this Flowable and returns a future for the single item.
     * <ul>
     * <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.</li>
     * <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     * FlowableWithSingle} completes but no single item was emitted.</li>
     * <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     * errors before the single item was emitted.</li>
     * <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already
     * completed normally or exceptionally.</li>
     * </ul>
     *
     * @return a future for the single item of type S.
     */
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture() {
        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        singleFutureSubscriber.subscribe();
        return future;
    }

    /**
     * Does {@link #subscribe()} to this Flowable and returns a future for the single item.
     * <ul>
     * <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.</li>
     * <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     * FlowableWithSingle} completes but no single item was emitted.</li>
     * <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     * errors before the single item was emitted.</li>
     * <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already
     * completed normally or exceptionally.</li>
     * </ul>
     *
     * @param onNext see {@link #subscribe(Consumer)}
     * @return a future for the single item of type S.
     */
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
     * Does {@link #subscribe()} to this Flowable and returns a future for the single item.
     * <ul>
     * <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.</li>
     * <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     * FlowableWithSingle} completes but no single item was emitted.</li>
     * <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     * errors before the single item was emitted.</li>
     * <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already
     * completed normally or exceptionally.</li>
     * </ul>
     *
     * @param onNext  see {@link #subscribe(Consumer, Consumer)}
     * @param onError see {@link #subscribe(Consumer, Consumer)}
     * @return a future for the single item of type S.
     */
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
     * Does {@link #subscribe()} to this Flowable and returns a future for the single item.
     * <ul>
     * <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.</li>
     * <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     * FlowableWithSingle} completes but no single item was emitted.</li>
     * <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     * errors before the single item was emitted.</li>
     * <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already
     * completed normally or exceptionally.</li>
     * </ul>
     *
     * @param onNext     see {@link #subscribe(Consumer, Consumer, Action)}
     * @param onError    see {@link #subscribe(Consumer, Consumer, Action)}
     * @param onComplete see {@link #subscribe(Consumer, Consumer, Action)}
     * @return a future for the single item of type S.
     */
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull CompletableFuture<S> subscribeSingleFuture(
            final @NotNull Consumer<? super F> onNext, final @NotNull Consumer<? super Throwable> onError,
            final @NotNull Action onComplete) {

        final SingleFutureSubscriber<F, S> singleFutureSubscriber = new SingleFutureSubscriber<>(this);
        final CompletableFuture<S> future = singleFutureSubscriber.getFutureBeforeSubscribe();
        //noinspection ResultOfMethodCallIgnored
        singleFutureSubscriber.subscribe(onNext, onError, onComplete);
        return future;
    }

    /**
     * Does {@link #subscribe()} to this Flowable and returns a future for the single item.
     * <ul>
     * <li>The future will complete with the single item if this {@link FlowableWithSingle} emits a single item.</li>
     * <li>The future will complete exceptionally with a {@link NoSuchElementException} if this {@link
     * FlowableWithSingle} completes but no single item was emitted.</li>
     * <li>The future will complete exceptionally with the exception emitted by this {@link FlowableWithSingle} if it
     * errors before the single item was emitted.</li>
     * <li>Cancelling the future will cancel this {@link FlowableWithSingle} also when the future already
     * completed normally or exceptionally.</li>
     * </ul>
     *
     * @param subscriber see {@link #subscribe(Subscriber)}
     * @return a future for the single item of type S.
     */
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

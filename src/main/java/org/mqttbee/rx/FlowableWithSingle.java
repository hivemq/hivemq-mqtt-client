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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.internal.util.ExceptionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.rx.reactivestreams.PublisherWithSingle;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

/**
 * A {@link Flowable} which emits a flow of items of type {@link F} and a single item of type {@link S}.
 *
 * @param <F> the type of the stream of items.
 * @param <S> the type of the single item.
 * @author Silvio Giebl
 */
public abstract class FlowableWithSingle<F, S> extends Flowable<F> implements PublisherWithSingle<F, S> {

    /**
     * Splits the given source into a flow of items of type F and a single item of type S.
     *
     * @param source        the {@link Flowable} of the supertype T to split.
     * @param flowableClass the class of the type of the flow of item.
     * @param singleClass   the class of the single item type.
     * @param <T>           the type of the source, which is a supertype of S and F.
     * @param <S>           the type of the single item.
     * @param <F>           the type of the flow of items.
     * @return the {@link FlowableWithSingle} emitting a flow of items of type F and a single item of type S.
     */
    public static <T, F, S> @NotNull FlowableWithSingle<F, S> split(
            final @NotNull Flowable<T> source, final @NotNull Class<F> flowableClass,
            final @NotNull Class<S> singleClass) {

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
     * Modifies the upstream so that it applies a specified function to the single item of type {@link S} mapping it to
     * an item of type {@link SM}.
     *
     * @param singleMapper the mapper function to apply to the single item.
     * @param <SM>         the type of the mapped single item.
     * @return a {@link FlowableWithSingle} that applies the mapper function to the single item.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract <SM> @NotNull FlowableWithSingle<F, SM> mapSingle(@NotNull Function<S, SM> singleMapper);

    /**
     * Modifies the upstream so that it applies a specified function to the single item of type {@link S} mapping it to
     * an item of type {@link SM} and a a specified function to the flow of items of type {@link F} mapping them to
     * items of type {@link FM}.
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
     * Modifies the upstream so that it calls a consumer on emission of the single item of type {@link S}.
     *
     * @param singleConsumer the consumer of the single item.
     * @return the modified {@link Flowable}.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract @NotNull FlowableWithSingle<F, S> doOnSingle(@NotNull Consumer<S> singleConsumer);

    /**
     * Does {@link #subscribe()} to this Flowable and awaits the single item of type S.
     *
     * @return the single item and a disposable allowing cancelling of the flow of items of type F.
     * @throws NoSuchElementException if this Flowable completes but no single item of type S was emitted.
     * @throws RuntimeException       if the Flowable errors before a single item of type S was emitted.
     */
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull SingleAndDisposable<S> subscribeBlockUntilSingle() {
        final BlockingSingleSubscriber<F, S> blockingSingleSubscriber = new BlockingSingleSubscriber<>(this);
        final Disposable disposable = blockingSingleSubscriber.subscribe();
        final S single = blockingSingleSubscriber.blockingGet();
        return new SingleAndDisposable<>(single, disposable);
    }

    /**
     * Does {@link #subscribe(Consumer)} to this Flowable and awaits the single item of type S.
     *
     * @param onNext see {@link #subscribe(Consumer)}
     * @return the single item and a disposable allowing cancelling of the flow of items of type F.
     * @throws NoSuchElementException if this Flowable completes but no single item of type S was emitted.
     * @throws RuntimeException       if the Flowable errors before a single item of type S was emitted.
     */
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull SingleAndDisposable<S> subscribeBlockUntilSingle(final @NotNull Consumer<? super F> onNext) {
        final BlockingSingleSubscriber<F, S> blockingSingleSubscriber = new BlockingSingleSubscriber<>(this);
        final Disposable disposable = blockingSingleSubscriber.subscribe(onNext);
        final S single = blockingSingleSubscriber.blockingGet();
        return new SingleAndDisposable<>(single, disposable);
    }

    /**
     * Does {@link #subscribe(Consumer, Consumer)} to this Flowable and awaits the single item of type S.
     *
     * @param onNext  see {@link #subscribe(Consumer, Consumer)}
     * @param onError see {@link #subscribe(Consumer, Consumer)}
     * @return the single item and a disposable allowing cancelling of the flow of items of type F.
     * @throws NoSuchElementException if this Flowable completes but no single item of type S was emitted.
     * @throws RuntimeException       if the Flowable errors before a single item of type S was emitted.
     */
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull SingleAndDisposable<S> subscribeBlockUntilSingle(
            final @NotNull Consumer<? super F> onNext, final @NotNull Consumer<? super Throwable> onError) {

        final BlockingSingleSubscriber<F, S> blockingSingleSubscriber = new BlockingSingleSubscriber<>(this);
        final Disposable disposable = blockingSingleSubscriber.subscribe(onNext, onError);
        final S single = blockingSingleSubscriber.blockingGet();
        return new SingleAndDisposable<>(single, disposable);
    }

    /**
     * Does {@link #subscribe(Consumer, Consumer, Action)} to this Flowable and awaits the single item of type S.
     *
     * @param onNext     see {@link #subscribe(Consumer, Consumer, Action)}
     * @param onError    see {@link #subscribe(Consumer, Consumer, Action)}
     * @param onComplete see {@link #subscribe(Consumer, Consumer, Action)}
     * @return the single item and a disposable allowing cancelling of the flow of items of type F.
     * @throws NoSuchElementException if this Flowable completes before a single item of type S was emitted.
     * @throws RuntimeException       if the Flowable errors before a single item of type S was emitted.
     */
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull SingleAndDisposable<S> subscribeBlockUntilSingle(
            final @NotNull Consumer<? super F> onNext, final @NotNull Consumer<? super Throwable> onError,
            final @NotNull Action onComplete) {

        final BlockingSingleSubscriber<F, S> blockingSingleSubscriber = new BlockingSingleSubscriber<>(this);
        final Disposable disposable = blockingSingleSubscriber.subscribe(onNext, onError, onComplete);
        final S single = blockingSingleSubscriber.blockingGet();
        return new SingleAndDisposable<>(single, disposable);
    }

    /**
     * Does {@link #subscribe(Subscriber)} to this Flowable and awaits the single item of type S.
     *
     * @param subscriber see {@link #subscribe(Subscriber)}
     * @return the single item.
     * @throws NoSuchElementException if this Flowable completes but no single item of type S was emitted.
     * @throws RuntimeException       if the Flowable errors before a single item of type S was emitted.
     */
    @BackpressureSupport(BackpressureKind.SPECIAL)
    @SchedulerSupport(SchedulerSupport.NONE)
    public final @NotNull S subscribeBlockUntilSingle(final @NotNull Subscriber<? super F> subscriber) {
        final BlockingSingleSubscriber<F, S> blockingSingleSubscriber = new BlockingSingleSubscriber<>(this);
        blockingSingleSubscriber.subscribe(subscriber);
        return blockingSingleSubscriber.blockingGet();
    }

    /**
     * Combines a single item of type {@link S} and a {@link Disposable} allowing cancelling of the flow of items.
     *
     * @param <S> the type of the single item.
     */
    public static class SingleAndDisposable<S> {

        private final @NotNull S single;
        private final @NotNull Disposable disposable;

        SingleAndDisposable(final @NotNull S single, final @NotNull Disposable disposable) {
            this.single = single;
            this.disposable = disposable;
        }

        public @NotNull S getSingle() {
            return single;
        }

        public @NotNull Disposable getDisposable() {
            return disposable;
        }
    }

    private static class BlockingSingleSubscriber<F, S> extends Flowable<F>
            implements FlowableWithSingleSubscriber<F, S>, Subscription {

        private final @NotNull CountDownLatch latch = new CountDownLatch(1);
        private final @NotNull FlowableWithSingle<F, S> source;
        private @Nullable Subscriber<? super F> subscriber;
        private @Nullable Subscription subscription;
        private @Nullable S single;
        private @Nullable Throwable error;

        private BlockingSingleSubscriber(final @NotNull FlowableWithSingle<F, S> source) {
            this.source = source;
        }

        @Override
        protected void subscribeActual(final @NotNull Subscriber<? super F> s) {
            subscriber = s;
            source.subscribeBoth(this);
        }

        @Override
        public void onSubscribe(final @NotNull Subscription s) {
            assert subscriber != null;
            subscription = s;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            single = s;
            latch.countDown();
        }

        @Override
        public void onNext(final @NotNull F f) {
            assert subscriber != null;
            subscriber.onNext(f);
        }

        @Override
        public void onComplete() {
            assert subscriber != null;
            latch.countDown();
            subscriber.onComplete();
        }

        @Override
        public void onError(final @NotNull Throwable t) {
            assert subscriber != null;
            error = t;
            latch.countDown();
            subscriber.onError(t);
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
            latch.countDown();
        }

        @NotNull S blockingGet() {
            if (latch.getCount() != 0) {
                try {
                    BlockingHelper.verifyNonBlocking();
                    latch.await();
                } catch (final InterruptedException e) {
                    throw ExceptionHelper.wrapOrThrow(e);
                }
            }
            if (error != null) {
                throw ExceptionHelper.wrapOrThrow(error);
            }
            if (single == null) {
                throw ExceptionHelper.wrapOrThrow(new NoSuchElementException());
            }
            return single;
        }
    }

}

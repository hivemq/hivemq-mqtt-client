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
import io.reactivex.functions.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.rx.FlowableMapFilter;
import org.mqttbee.rx.reactivestreams.PublisherWithSingle;
import org.mqttbee.rx.reactivestreams.WithSingleSubscriber;
import org.mqttbee.util.Checks;
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
public class FlowableWithSingle<F, S> extends Flowable<F> implements PublisherWithSingle<F, S> {

    private static final @NotNull Predicate<Object> FLOWABLE_FILTER = o -> !(o instanceof SingleElement);

    /**
     * Splits the given source into a flow of items of type <code>F</code> and a single item of type <code>S</code>.
     * <p>
     * Only a single item of type <code>S</code> will be emitted. Any further items of type <code>S</code> emitted from
     * the upstream are ignored. Items emitted from the upstream which are instances of <code>T</code> but neither of
     * <code>F</code> nor <code>S</code> are ignored.
     * <dl>
     * <dt><b>Backpressure:</b></dt>
     * <dd>The operator doesn't interfere with backpressure which is determined by the source {@code Publisher}'s
     * backpressure behavior.</dd>
     * <dt><b>Scheduler:</b></dt>
     * <dd>The operator does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
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
        return new FlowableWithSingle<>(source, flowableClass, singleClass);
    }

    private final @NotNull Flowable<?> source;
    private final @Nullable Consumer<? super S> singleConsumer;

    private FlowableWithSingle(
            final @NotNull Flowable<?> source, final @NotNull Class<F> flowableClass,
            final @NotNull Class<S> singleClass) {

        this(FlowableMapFilter.mapFilter(source, new FlowableWithSingleSplitter<>(flowableClass, singleClass)), null);
    }

    private FlowableWithSingle(final @NotNull Flowable<?> source) {
        this(source, null);
    }

    private FlowableWithSingle(final @NotNull Flowable<?> source, final @Nullable Consumer<? super S> singleConsumer) {
        this.source = source;
        this.singleConsumer = singleConsumer;
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
    public @NotNull FlowableWithSingle<F, S> observeOnBoth(final @NotNull Scheduler scheduler) {
        return new FlowableWithSingle<>(applySingleConsumer().observeOn(scheduler));
    }

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
    public @NotNull FlowableWithSingle<F, S> observeOnBoth(
            final @NotNull Scheduler scheduler, final boolean delayError) {

        return new FlowableWithSingle<>(applySingleConsumer().observeOn(scheduler, delayError));
    }

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
    public @NotNull FlowableWithSingle<F, S> observeOnBoth(
            final @NotNull Scheduler scheduler, final boolean delayError, final int bufferSize) {

        return new FlowableWithSingle<>(applySingleConsumer().observeOn(scheduler, delayError, bufferSize));
    }

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
    public <SM> @NotNull FlowableWithSingle<F, SM> mapSingle(
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        Checks.notNull(singleMapper, "Single mapper");
        return new FlowableWithSingle<>(applySingleConsumer().map(new MapperSingle<>(singleMapper)));
    }

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
    public <FM, SM> @NotNull FlowableWithSingle<FM, SM> mapBoth(
            final @NotNull Function<? super F, ? extends FM> flowableMapper,
            final @NotNull Function<? super S, ? extends SM> singleMapper) {

        Checks.notNull(singleMapper, "Single mapper");
        Checks.notNull(flowableMapper, "Flowable mapper");
        return new FlowableWithSingle<>(applySingleConsumer().map(new MapperBoth<>(flowableMapper, singleMapper)));
    }

    /**
     * Modifies the upstream so that it applies a specified function to an error which can map it to a different error.
     *
     * @param mapper the mapper function to apply to an error.
     * @return a {@link FlowableWithSingle} that applies the mapper function to an error.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public @NotNull FlowableWithSingle<F, S> mapError(
            final @NotNull Function<? super Throwable, ? extends Throwable> mapper) {

        Checks.notNull(mapper, "Mapper");
        final Function<Throwable, Flowable<?>> resumeMapper = throwable -> Flowable.error(mapper.apply(throwable));
        @SuppressWarnings("unchecked") final Flowable<Object> source = (Flowable<Object>) applySingleConsumer();
        return new FlowableWithSingle<>(source.onErrorResumeNext(resumeMapper));
    }

    /**
     * Modifies the upstream so that it calls a consumer on emission of the single item of type <code>S</code>.
     *
     * @param singleConsumer the consumer of the single item.
     * @return the modified {@link Flowable}.
     */
    @BackpressureSupport(BackpressureKind.PASS_THROUGH)
    @SchedulerSupport(SchedulerSupport.NONE)
    public @NotNull FlowableWithSingle<F, S> doOnSingle(final @NotNull Consumer<? super S> singleConsumer) {
        Checks.notNull(singleConsumer, "Single consumer");
        return new FlowableWithSingle<>(applySingleConsumer(), singleConsumer);
    }

    private @NotNull Flowable<?> applySingleConsumer() {
        return (singleConsumer == null) ? source : source.map(new ConsumerSingle<>(singleConsumer));
    }

    @Override
    @BackpressureSupport(BackpressureKind.SPECIAL)
    @SchedulerSupport(SchedulerSupport.NONE)
    public void subscribeBoth(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        doOnSingle(subscriber::onSingle).subscribe(subscriber);
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super F> s) {
        @SuppressWarnings("unchecked") final Flowable<F> flowable = (Flowable<F>) source.filter(
                (singleConsumer == null) ? FLOWABLE_FILTER : new ConsumerSingle<>(singleConsumer));
        flowable.subscribe(s);
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

    private static class SingleElement {

        final @NotNull Object element;

        SingleElement(final @NotNull Object element) {
            this.element = element;
        }
    }

    private static class FlowableWithSingleSplitter<F, S> implements Function<Object, Object> {

        private final @NotNull Class<F> flowableClass;
        private @Nullable Class<S> singleClass;

        private FlowableWithSingleSplitter(final @NotNull Class<F> flowableClass, final @NotNull Class<S> singleClass) {
            this.flowableClass = flowableClass;
            this.singleClass = singleClass;
        }

        @Override
        public @Nullable Object apply(final @NotNull Object o) {
            if (flowableClass.isInstance(o)) {
                return o;
            }
            if ((singleClass != null) && singleClass.isInstance(o)) {
                singleClass = null;
                return new SingleElement(o);
            }
            return null;
        }
    }

    private static class ConsumerSingle<S> implements Function<Object, Object>, Predicate<Object> {

        private @Nullable Consumer<S> singleConsumer;

        private ConsumerSingle(final @NotNull Consumer<S> singleConsumer) {
            this.singleConsumer = singleConsumer;
        }

        @Override
        public @NotNull Object apply(final @NotNull Object o) throws Exception {
            if (o instanceof SingleElement) {
                consumeSingle((SingleElement) o);
            }
            return o;
        }

        @Override
        public boolean test(final @NotNull Object o) throws Exception {
            if (o instanceof SingleElement) {
                consumeSingle((SingleElement) o);
                return false;
            }
            return true;
        }

        private void consumeSingle(final @NotNull SingleElement singleElement) throws Exception {
            if (singleConsumer == null) {
                throw new IllegalStateException("Single element must only be emitted at most once");
            }
            @SuppressWarnings("unchecked") final S s = (S) singleElement.element;
            singleConsumer.accept(s);
            singleConsumer = null;
        }
    }

    private static class MapperSingle<S> implements Function<Object, Object> {

        private @Nullable Function<S, ?> singleMapper;

        private MapperSingle(final @NotNull Function<S, ?> singleMapper) {
            this.singleMapper = singleMapper;
        }

        @Override
        public @NotNull Object apply(final @NotNull Object o) throws Exception {
            if (o instanceof SingleElement) {
                return mapSingle((SingleElement) o);
            }
            return o;
        }

        @NotNull SingleElement mapSingle(final @NotNull SingleElement singleElement) throws Exception {
            if (singleMapper == null) {
                throw new IllegalStateException("Single element must only be emitted at most once");
            }
            @SuppressWarnings("unchecked") final S s = (S) singleElement.element;
            final SingleElement mappedSingleElement = new SingleElement(singleMapper.apply(s));
            singleMapper = null;
            return mappedSingleElement;
        }
    }

    private static class MapperBoth<F, S> extends MapperSingle<S> {

        private final @NotNull Function<F, ?> flowableMapper;

        private MapperBoth(final @NotNull Function<F, ?> flowableMapper, final @NotNull Function<S, ?> singleMapper) {
            super(singleMapper);
            this.flowableMapper = flowableMapper;
        }

        @Override
        public @NotNull Object apply(final @NotNull Object o) throws Exception {
            if (o instanceof SingleElement) {
                return mapSingle((SingleElement) o);
            }
            @SuppressWarnings("unchecked") final F f = (F) o;
            return flowableMapper.apply(f);
        }
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

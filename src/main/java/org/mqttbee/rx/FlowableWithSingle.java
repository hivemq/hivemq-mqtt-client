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
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;
import io.reactivex.annotations.BackpressureKind;
import io.reactivex.annotations.BackpressureSupport;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiConsumer;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.internal.fuseable.QueueSubscription;
import io.reactivex.plugins.RxJavaPlugins;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A {@link Flowable} which emits a single item and a stream of items. It transforms an upstream {@link Flowable} of
 * type {@link T}, which is a supertype of {@link S} and {@link F}, to a single item of type {@link S} and a stream of
 * items of type {@link T}.
 * <p>
 * Only a single item of type S will be emitted. Any further items of type S emitted from the upstream are ignored.
 * Items emitted from the upstream which are instances of T but neither of S nor T are ignored.
 * <dl>
 * <dt><b>Backpressure:</b></dt>
 * <dd>The operator doesn't interfere with backpressure which is determined by the source {@code Publisher}'s
 * backpressure behavior.</dd>
 * <dt><b>Scheduler:</b></dt>
 * <dd>{@code cast} does not operate by default on a particular {@link Scheduler}.</dd>
 * </dl>
 *
 * @param <T> the type of the upstream, which is a supertype of S and F.
 * @param <S> the type of the single item.
 * @param <F> the type of the stream of items.
 * @author Silvio Giebl
 */
@BackpressureSupport(BackpressureKind.PASS_THROUGH)
@SchedulerSupport(SchedulerSupport.NONE)
public class FlowableWithSingle<T, S extends T, F extends T> extends Flowable<F> {

    private final Flowable<? super T> source;
    private final Class<S> singleClass;
    private final Class<F> flowableClass;
    private final BiConsumer<S, Subscription> singleConsumer;

    /**
     * Creates a new {@link FlowableWithSingle} transforming the given upstream source.
     *
     * @param source        the upstream source to transform.
     * @param singleClass   the class of the single item type.
     * @param flowableClass the class of the type of the item stream.
     */
    public FlowableWithSingle(
            @NotNull final Flowable<? super T> source, @NotNull final Class<S> singleClass,
            @NotNull final Class<F> flowableClass) {

        this(source, singleClass, flowableClass, null);
    }

    /**
     * Creates a new {@link FlowableWithSingle} transforming the given upstream source.
     *
     * @param source         the upstream source to transform.
     * @param singleClass    the class of the single item type.
     * @param flowableClass  the class of the type of the item stream.
     * @param singleConsumer the consumer of the single item.
     */
    private FlowableWithSingle(
            @NotNull final Flowable<? super T> source, @NotNull final Class<S> singleClass,
            @NotNull final Class<F> flowableClass, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

        this.source = source;
        this.singleClass = singleClass;
        this.flowableClass = flowableClass;
        this.singleConsumer = singleConsumer;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super F> s) {
        if (s instanceof ConditionalSubscriber) {
            @SuppressWarnings("unchecked") final ConditionalSubscriber<? super F> cs =
                    (ConditionalSubscriber<? super F>) s;
            source.subscribe(
                    new FlowableWithSingleConditionalSubscriber<>(cs, singleClass, flowableClass, singleConsumer));
        } else {
            source.subscribe(new FlowableWithSingleSubscriber<>(s, singleClass, flowableClass, singleConsumer));
        }
    }

    /**
     * Modifies the upstream to perform its emissions and notifications on a specified Scheduler.
     *
     * @param scheduler see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public FlowableWithSingle<T, S, F> observeOnWithSingle(@NotNull final Scheduler scheduler) {
        return new FlowableWithSingle<>(source.observeOn(scheduler), singleClass, flowableClass, singleConsumer);
    }

    /**
     * Modifies the upstream to perform its emissions and notifications on a specified Scheduler.
     *
     * @param scheduler  see {@link Flowable#observeOn(Scheduler)}.
     * @param delayError see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public FlowableWithSingle<T, S, F> observeOnWithSingle(
            @NotNull final Scheduler scheduler, final boolean delayError) {

        return new FlowableWithSingle<>(
                source.observeOn(scheduler, delayError), singleClass, flowableClass, singleConsumer);
    }

    /**
     * Modifies the upstream to perform its emissions and notifications on a specified Scheduler.
     *
     * @param scheduler  see {@link Flowable#observeOn(Scheduler)}.
     * @param delayError see {@link Flowable#observeOn(Scheduler)}.
     * @param bufferSize see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public FlowableWithSingle<T, S, F> observeOnWithSingle(
            @NotNull final Scheduler scheduler, final boolean delayError, final int bufferSize) {

        return new FlowableWithSingle<>(
                source.observeOn(scheduler, delayError, bufferSize), singleClass, flowableClass, singleConsumer);
    }

    /**
     * Modifies this {@link FlowableWithSingle} so that it invokes an action when the single item of type {@link S} is
     * emitted from the upstream.
     *
     * @param singleConsumer the consumer of the single item.
     * @return the modified {@link Flowable}.
     */
    @BackpressureSupport(BackpressureKind.NONE)
    @SchedulerSupport(SchedulerSupport.NONE)
    public Flowable<F> doOnSingle(@NotNull final BiConsumer<S, Subscription> singleConsumer) {
        return new FlowableWithSingle<>(source, singleClass, flowableClass, singleConsumer);
    }

    private static abstract class FlowableWithSingleAbstractSubscriber<T, S extends T, F extends T>
            implements FlowableSubscriber<T>, ConditionalSubscriber<T>, QueueSubscription<F> {

        final Subscriber<? super F> actual;
        private final Class<S> singleClass;
        private final Class<F> flowableClass;
        private BiConsumer<S, Subscription> singleConsumer;

        private Subscription s;
        private QueueSubscription<T> qs;
        private boolean done;
        private int sourceMode;

        private FlowableWithSingleAbstractSubscriber(
                @NotNull final Subscriber<? super F> actual, @NotNull final Class<S> singleClass,
                @NotNull final Class<F> flowableClass, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            this.actual = actual;
            this.singleClass = singleClass;
            this.flowableClass = flowableClass;
            this.singleConsumer = singleConsumer;
        }

        @Override
        public void onSubscribe(final Subscription s) {
            this.s = s;
            if (s instanceof QueueSubscription) {
                @SuppressWarnings("unchecked") final QueueSubscription<T> qs = (QueueSubscription<T>) s;
                this.qs = qs;
            }
            actual.onSubscribe(this);
        }

        @Override
        public void onNext(final T t) {
            if (!tryOnNext(t)) {
                s.request(1);
            }
        }

        @Override
        public boolean tryOnNext(final T t) {
            if (done) {
                return true;
            }

            if (sourceMode == ASYNC) {
                return tryOnNextActual(null);
            }

            if (singleClass.isInstance(t)) {
                if (singleConsumer != null) {
                    try {
                        @SuppressWarnings("unchecked") final S single = (S) t;
                        singleConsumer.accept(single, s);
                        singleConsumer = null;
                        return false;
                    } catch (final Throwable e) {
                        Exceptions.throwIfFatal(e);
                        s.cancel();
                        onError(e);
                        return true;
                    }
                } else {
                    return false;
                }
            } else if (flowableClass.isInstance(t)) {
                @SuppressWarnings("unchecked") final F f = (F) t;
                return tryOnNextActual(f);
            } else {
                return false;
            }
        }

        abstract boolean tryOnNextActual(F f);

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            actual.onComplete();
        }

        @Override
        public void onError(final Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            actual.onError(t);
        }

        @Override
        public void request(final long n) {
            s.request(n);
        }

        @Override
        public void cancel() {
            s.cancel();
        }

        @Override
        public int requestFusion(final int mode) {
            if (qs != null) {
                if ((mode & BOUNDARY) == 0) {
                    final int m = qs.requestFusion(mode);
                    if (m != NONE) {
                        sourceMode = m;
                    }
                    return m;
                }
            }
            return NONE;
        }

        @Override
        public F poll() throws Exception {
            for (; ; ) {
                final T t = qs.poll();
                if (t == null) {
                    return null;
                }
                if (singleClass.isInstance(t)) {
                    if (singleConsumer != null) {
                        @SuppressWarnings("unchecked") final S single = (S) t;
                        singleConsumer.accept(single, s);
                        singleConsumer = null;
                    }
                } else if (flowableClass.isInstance(t)) {
                    @SuppressWarnings("unchecked") final F f = (F) t;
                    return f;
                }

                if (sourceMode == ASYNC) {
                    qs.request(1);
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return qs.isEmpty();
        }

        @Override
        public void clear() {
            qs.clear();
        }

        @Override
        public boolean offer(final F value) {
            throw new UnsupportedOperationException("Should not be called!");
        }

        @Override
        public boolean offer(final F v1, final F v2) {
            throw new UnsupportedOperationException("Should not be called!");
        }

    }

    private static final class FlowableWithSingleSubscriber<T, S extends T, F extends T>
            extends FlowableWithSingleAbstractSubscriber<T, S, F> {

        private FlowableWithSingleSubscriber(
                @NotNull final Subscriber<? super F> actual, @NotNull final Class<S> singleClass,
                @NotNull final Class<F> flowableClass, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(actual, singleClass, flowableClass, singleConsumer);
        }

        @Override
        boolean tryOnNextActual(final F f) {
            actual.onNext(f);
            return true;
        }

    }

    private static final class FlowableWithSingleConditionalSubscriber<T, S extends T, F extends T>
            extends FlowableWithSingleAbstractSubscriber<T, S, F> {

        private final ConditionalSubscriber<? super F> conditionalActual;

        private FlowableWithSingleConditionalSubscriber(
                @NotNull final ConditionalSubscriber<? super F> actual, @NotNull final Class<S> singleClass,
                @NotNull final Class<F> flowableClass, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(actual, singleClass, flowableClass, singleConsumer);
            this.conditionalActual = actual;
        }

        @Override
        boolean tryOnNextActual(final F f) {
            return conditionalActual.tryOnNext(f);
        }

    }

}

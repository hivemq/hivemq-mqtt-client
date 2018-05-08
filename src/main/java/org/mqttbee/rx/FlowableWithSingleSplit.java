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
import io.reactivex.functions.Function;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.internal.fuseable.QueueSubscription;
import io.reactivex.plugins.RxJavaPlugins;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A {@link Flowable} operator which splits an upstream {@link Flowable} of type {@link T} into a single item of type
 * {@link S} and a flow of items of type {@link F}.
 * <p>
 * Only a single item of type S will be emitted. Any further items of type S emitted from the upstream are ignored.
 * Items emitted from the upstream which are instances of T but neither of S nor T are ignored.
 * <dl>
 * <dt><b>Backpressure:</b></dt>
 * <dd>The operator doesn't interfere with backpressure which is determined by the source {@code Publisher}'subscription
 * backpressure behavior.</dd>
 * <dt><b>Scheduler:</b></dt>
 * <dd>The operator does not operate by default on a particular {@link Scheduler}.</dd>
 * </dl>
 *
 * @param <T> the type of the upstream, which is a supertype of S and F.
 * @param <S> the type of the single item.
 * @param <F> the type of the stream of items.
 * @author Silvio Giebl
 */
@BackpressureSupport(BackpressureKind.PASS_THROUGH)
@SchedulerSupport(SchedulerSupport.NONE)
public class FlowableWithSingleSplit<T, S, F> extends FlowableWithSingle<S, F> {

    private final Flowable<T> source;
    private final Caster<S> singleCaster;
    private final Caster<F> flowableCaster;
    private final BiConsumer<S, Subscription> singleConsumer;

    /**
     * Creates a new {@link FlowableWithSingleSplit} transforming the given upstream source.
     *
     * @param source        the upstream source to transform.
     * @param singleClass   the class of the single item type.
     * @param flowableClass the class of the type of the item stream.
     */
    public FlowableWithSingleSplit(
            @NotNull final Flowable<T> source, @NotNull final Class<S> singleClass,
            @NotNull final Class<F> flowableClass) {

        this(source, Caster.of(singleClass), Caster.of(flowableClass), null);
    }

    /**
     * Creates a new {@link FlowableWithSingleSplit} transforming the given upstream source.
     *
     * @param source         the upstream source to transform.
     * @param singleCaster   the caster for the single item.
     * @param flowableCaster the caster for the flow items.
     * @param singleConsumer the consumer of the single item.
     */
    private FlowableWithSingleSplit(
            @NotNull final Flowable<T> source, @NotNull final Caster<S> singleCaster,
            @NotNull final Caster<F> flowableCaster, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

        this.source = source;
        this.singleCaster = singleCaster;
        this.flowableCaster = flowableCaster;
        this.singleConsumer = singleConsumer;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super F> s) {
        if (s instanceof ConditionalSubscriber) {
            @SuppressWarnings("unchecked") final ConditionalSubscriber<? super F> cs = (ConditionalSubscriber<? super F>) s;
            source.subscribe(
                    new FlowableWithSingleConditionalSubscriber<>(singleCaster, flowableCaster, cs, singleConsumer));
        } else {
            source.subscribe(new FlowableWithSingleSubscriber<>(singleCaster, flowableCaster, s, singleConsumer));
        }
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> observeOnWithSingle(@NotNull final Scheduler scheduler) {
        return new FlowableWithSingleSplit<>(source.observeOn(scheduler), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> observeOnWithSingle(
            @NotNull final Scheduler scheduler, final boolean delayError) {

        return new FlowableWithSingleSplit<>(
                source.observeOn(scheduler, delayError), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> observeOnWithSingle(
            @NotNull final Scheduler scheduler, final boolean delayError, final int bufferSize) {

        return new FlowableWithSingleSplit<>(
                source.observeOn(scheduler, delayError, bufferSize), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public <SM, FM> FlowableWithSingleSplit<T, SM, FM> mapBoth(
            @NotNull final Function<S, SM> singleMapper, @NotNull final Function<F, FM> flowableMapper) {

        return new FlowableWithSingleSplit<>(
                source, Caster.map(singleCaster, singleMapper), Caster.map(flowableCaster, flowableMapper), null);
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> mapError(@NotNull final Function<Throwable, Throwable> mapper) {
        final Function<Throwable, Flowable<T>> resumeMapper = throwable -> Flowable.error(mapper.apply(throwable));
        return new FlowableWithSingleSplit<>(
                source.onErrorResumeNext(resumeMapper), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public Flowable<F> doOnSingle(@NotNull final BiConsumer<S, Subscription> singleConsumer) {
        return new FlowableWithSingleSplit<>(source, singleCaster, flowableCaster, singleConsumer);
    }


    private static abstract class FlowableWithSingleAbstractSubscriber<T, S, F>
            implements FlowableSubscriber<T>, ConditionalSubscriber<T>, QueueSubscription<F> {

        private final Caster<S> singleCaster;
        private final Caster<F> flowableCaster;
        final Subscriber<? super F> flowableSubscriber;
        private BiConsumer<S, Subscription> singleConsumer;

        private Subscription subscription;
        private QueueSubscription<T> queueSubscription;
        private int sourceMode;
        private boolean done;

        private FlowableWithSingleAbstractSubscriber(
                @NotNull final Caster<S> singleCaster, @NotNull final Caster<F> flowableCaster,
                @NotNull final Subscriber<? super F> flowableSubscriber,
                @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            this.singleCaster = singleCaster;
            this.flowableCaster = flowableCaster;
            this.flowableSubscriber = flowableSubscriber;
            this.singleConsumer = singleConsumer;
        }

        @Override
        public void onSubscribe(final Subscription s) {
            this.subscription = s;
            if (s instanceof QueueSubscription) {
                @SuppressWarnings("unchecked") final QueueSubscription<T> qs = (QueueSubscription<T>) s;
                this.queueSubscription = qs;
            }
            flowableSubscriber.onSubscribe(this);
        }

        @Override
        public void onNext(final T t) {
            if (!tryOnNext(t)) {
                subscription.request(1);
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

            try {
                final F f = flowableCaster.cast(t);
                if (f != null) {
                    return tryOnNextActual(f);
                }
                final S single = singleCaster.cast(t);
                if (single != null) {
                    if (singleConsumer != null) {
                        singleConsumer.accept(single, subscription);
                        singleConsumer = null;
                    }
                }
                return false;
            } catch (final Throwable e) {
                Exceptions.throwIfFatal(e);
                subscription.cancel();
                onError(e);
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
            flowableSubscriber.onComplete();
        }

        @Override
        public void onError(final Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            flowableSubscriber.onError(t);
        }

        @Override
        public void request(final long n) {
            subscription.request(n);
        }

        @Override
        public void cancel() {
            subscription.cancel();
        }

        @Override
        public int requestFusion(final int mode) {
            if (queueSubscription != null) {
                if ((mode & BOUNDARY) == 0) {
                    final int m = queueSubscription.requestFusion(mode);
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
                final T t = queueSubscription.poll();
                if (t == null) {
                    return null;
                }

                final F f = flowableCaster.cast(t);
                if (f != null) {
                    return f;
                }
                final S single = singleCaster.cast(t);
                if (single != null) {
                    if (singleConsumer != null) {
                        singleConsumer.accept(single, subscription);
                        singleConsumer = null;
                    }
                }

                if (sourceMode == ASYNC) {
                    queueSubscription.request(1);
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return queueSubscription.isEmpty();
        }

        @Override
        public void clear() {
            queueSubscription.clear();
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


    private static final class FlowableWithSingleSubscriber<T, S, F>
            extends FlowableWithSingleAbstractSubscriber<T, S, F> {

        private FlowableWithSingleSubscriber(
                @NotNull final Caster<S> singleCaster, @NotNull final Caster<F> flowableCaster,
                @NotNull final Subscriber<? super F> flowableSubscriber,
                @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(singleCaster, flowableCaster, flowableSubscriber, singleConsumer);
        }

        @Override
        boolean tryOnNextActual(final F f) {
            flowableSubscriber.onNext(f);
            return true;
        }

    }


    private static final class FlowableWithSingleConditionalSubscriber<T, S, F>
            extends FlowableWithSingleAbstractSubscriber<T, S, F> {

        private final ConditionalSubscriber<? super F> conditionalActual;

        private FlowableWithSingleConditionalSubscriber(
                @NotNull final Caster<S> singleCaster, @NotNull final Caster<F> flowableCaster,
                @NotNull final ConditionalSubscriber<? super F> flowableSubscriber,
                @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(singleCaster, flowableCaster, flowableSubscriber, singleConsumer);
            this.conditionalActual = flowableSubscriber;
        }

        @Override
        boolean tryOnNextActual(final F f) {
            return conditionalActual.tryOnNext(f);
        }

    }


    private interface Caster<C> {

        @Nullable
        C cast(@NotNull final Object o) throws Exception;


        @SuppressWarnings("unchecked")
        static <C> Caster<C> of(@NotNull final Class<C> castedClass) {
            return o -> (castedClass.isInstance(o)) ? (C) o : null;
        }

        static <C, M> Caster<M> map(@NotNull final Caster<C> inner, @NotNull final Function<C, M> mapper) {
            return o -> {
                final C c = inner.cast(o);
                return (c == null) ? null : mapper.apply(c);
            };
        }

    }

}

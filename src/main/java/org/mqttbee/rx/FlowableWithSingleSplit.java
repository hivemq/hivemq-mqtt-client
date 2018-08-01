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

import com.google.common.base.Preconditions;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.BackpressureKind;
import io.reactivex.annotations.BackpressureSupport;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private final Function<T, S> singleCaster;
    private final Function<T, F> flowableCaster;
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

        this(source, caster(singleClass), caster(flowableClass), null);
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
            @NotNull final Flowable<T> source, @NotNull final Function<T, S> singleCaster,
            @NotNull final Function<T, F> flowableCaster, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

        this.source = source;
        this.singleCaster = singleCaster;
        this.flowableCaster = flowableCaster;
        this.singleConsumer = singleConsumer;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super F> s) {
        if (s instanceof ConditionalSubscriber) {
            @SuppressWarnings("unchecked") final ConditionalSubscriber<? super F> cs =
                    (ConditionalSubscriber<? super F>) s;
            source.subscribe(
                    new FlowableWithSingleConditionalSubscriber<>(singleCaster, flowableCaster, cs, singleConsumer));
        } else {
            source.subscribe(new FlowableWithSingleSubscriber<>(singleCaster, flowableCaster, s, singleConsumer));
        }
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> observeOnBoth(@NotNull final Scheduler scheduler) {
        return new FlowableWithSingleSplit<>(source.observeOn(scheduler), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> observeOnBoth(
            @NotNull final Scheduler scheduler, final boolean delayError) {

        return new FlowableWithSingleSplit<>(
                source.observeOn(scheduler, delayError), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> observeOnBoth(
            @NotNull final Scheduler scheduler, final boolean delayError, final int bufferSize) {

        return new FlowableWithSingleSplit<>(
                source.observeOn(scheduler, delayError, bufferSize), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public <SM, FM> FlowableWithSingleSplit<T, SM, FM> mapBoth(
            @NotNull final Function<S, SM> singleMapper, @NotNull final Function<F, FM> flowableMapper) {

        Preconditions.checkNotNull(singleMapper, "Single mapper must not be null.");
        Preconditions.checkNotNull(flowableMapper, "Flowable mapper must not be null.");
        return new FlowableWithSingleSplit<>(
                source, mapCaster(singleCaster, singleMapper), mapCaster(flowableCaster, flowableMapper), null);
    }

    @NotNull
    @Override
    public FlowableWithSingleSplit<T, S, F> mapError(@NotNull final Function<Throwable, Throwable> mapper) {
        Preconditions.checkNotNull(mapper, "Mapper must not be null.");
        final Function<Throwable, Flowable<T>> resumeMapper = throwable -> Flowable.error(mapper.apply(throwable));
        return new FlowableWithSingleSplit<>(
                source.onErrorResumeNext(resumeMapper), singleCaster, flowableCaster, singleConsumer);
    }

    @NotNull
    @Override
    public Flowable<F> doOnSingle(@NotNull final BiConsumer<S, Subscription> singleConsumer) {
        Preconditions.checkNotNull(singleConsumer, "Single consumer must not be null.");
        return new FlowableWithSingleSplit<>(source, singleCaster, flowableCaster, singleConsumer);
    }


    private static abstract class FlowableWithSingleAbstractSubscriber<T, S, F, U extends Subscriber<? super F>>
            extends FuseableSubscriber<T, F, U> implements ConditionalSubscriber<T> {

        private final Function<T, S> singleCaster;
        private final Function<T, F> flowableCaster;
        private BiConsumer<S, Subscription> singleConsumer;

        private FlowableWithSingleAbstractSubscriber(
                @NotNull final Function<T, S> singleCaster, @NotNull final Function<T, F> flowableCaster,
                @NotNull final U subscriber, @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(subscriber);
            this.singleCaster = singleCaster;
            this.flowableCaster = flowableCaster;
            this.singleConsumer = singleConsumer;
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
                final F f = flowableCaster.apply(t);
                if (f != null) {
                    return tryOnNextActual(f);
                }
                final S single = singleCaster.apply(t);
                if (single != null && singleConsumer != null) {
                    singleConsumer.accept(single, subscription);
                    singleConsumer = null;
                }
                return false;
            } catch (final Throwable e) {
                Exceptions.throwIfFatal(e);
                subscription.cancel();
                onError(e);
                return false;
            }
        }

        abstract boolean tryOnNextActual(@Nullable F f);

        @Override
        public F poll() throws Exception {
            for (; ; ) {
                final T t = queueSubscription.poll();
                if (t == null) {
                    return null;
                }

                final F f = flowableCaster.apply(t);
                if (f != null) {
                    return f;
                }
                final S single = singleCaster.apply(t);
                if (single != null && singleConsumer != null) {
                    singleConsumer.accept(single, subscription);
                    singleConsumer = null;
                }

                if (sourceMode == ASYNC) {
                    queueSubscription.request(1);
                }
            }
        }

    }


    private static final class FlowableWithSingleSubscriber<T, S, F>
            extends FlowableWithSingleAbstractSubscriber<T, S, F, Subscriber<? super F>> {

        private FlowableWithSingleSubscriber(
                @NotNull final Function<T, S> singleCaster, @NotNull final Function<T, F> flowableCaster,
                @NotNull final Subscriber<? super F> subscriber,
                @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(singleCaster, flowableCaster, subscriber, singleConsumer);
        }

        @Override
        boolean tryOnNextActual(@Nullable final F f) {
            subscriber.onNext(f);
            return true;
        }

    }


    private static final class FlowableWithSingleConditionalSubscriber<T, S, F>
            extends FlowableWithSingleAbstractSubscriber<T, S, F, ConditionalSubscriber<? super F>> {

        private FlowableWithSingleConditionalSubscriber(
                @NotNull final Function<T, S> singleCaster, @NotNull final Function<T, F> flowableCaster,
                @NotNull final ConditionalSubscriber<? super F> subscriber,
                @Nullable final BiConsumer<S, Subscription> singleConsumer) {

            super(singleCaster, flowableCaster, subscriber, singleConsumer);
        }

        @Override
        boolean tryOnNextActual(@Nullable final F f) {
            return subscriber.tryOnNext(f);
        }

    }


    @NotNull
    @SuppressWarnings("unchecked")
    private static <T, C> Function<T, C> caster(@NotNull final Class<C> castedClass) {
        return o -> (castedClass.isInstance(o)) ? (C) o : null;
    }

    @NotNull
    private static <T, C, M> Function<T, M> mapCaster(
            @NotNull final Function<T, C> inner, @NotNull final Function<C, M> mapper) {

        return o -> {
            final C c = inner.apply(o);
            return (c == null) ? null : mapper.apply(c);
        };
    }

}

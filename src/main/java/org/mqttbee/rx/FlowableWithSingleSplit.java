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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.rx.reactivestreams.WithSingleSubscriber;
import org.reactivestreams.Subscriber;

/**
 * A {@link Flowable} operator which splits an upstream {@link Flowable} of type {@link T} into a single item of type
 * {@link S} and a flow of items of type {@link F}.
 * <p>
 * Only a single item of type S will be emitted. Any further items of type S emitted from the upstream are ignored.
 * Items emitted from the upstream which are instances of T but neither of S nor F are ignored.
 * <dl>
 * <dt><b>Backpressure:</b></dt>
 * <dd>The operator doesn't interfere with backpressure which is determined by the source {@code Publisher}'s
 * backpressure behavior.</dd>
 * <dt><b>Scheduler:</b></dt>
 * <dd>The operator does not operate by default on a particular {@link Scheduler}.</dd>
 * </dl>
 *
 * @param <T> the type of the upstream, which is a supertype of S and F on creation.
 * @param <S> the type of the single item.
 * @param <F> the type of the flow of items.
 * @author Silvio Giebl
 */
@BackpressureSupport(BackpressureKind.PASS_THROUGH)
@SchedulerSupport(SchedulerSupport.NONE)
public class FlowableWithSingleSplit<T, F, S> extends FlowableWithSingle<F, S> {

    private final @NotNull Flowable<T> source;
    private final @NotNull Function<T, F> flowableCaster;
    private final @NotNull Function<T, S> singleCaster;
    private final @Nullable Consumer<S> singleConsumer;

    /**
     * Creates a new {@link FlowableWithSingleSplit} transforming the given upstream source.
     *
     * @param source        the upstream source to transform.
     * @param flowableClass the class of the type of the flow of item.
     * @param singleClass   the class of the single item type.
     */
    FlowableWithSingleSplit(
            final @NotNull Flowable<T> source, final @NotNull Class<F> flowableClass,
            final @NotNull Class<S> singleClass) {

        this(source, caster(flowableClass), caster(singleClass), null);
    }

    /**
     * Creates a new {@link FlowableWithSingleSplit} transforming the given upstream source.
     *
     * @param source         the upstream source to transform.
     * @param flowableCaster the caster for the flow items.
     * @param singleCaster   the caster for the single item.
     * @param singleConsumer the consumer of the single item.
     */
    private FlowableWithSingleSplit(
            final @NotNull Flowable<T> source, final @NotNull Function<T, F> flowableCaster,
            final @NotNull Function<T, S> singleCaster, final @Nullable Consumer<S> singleConsumer) {

        this.source = source;
        this.singleCaster = singleCaster;
        this.flowableCaster = flowableCaster;
        this.singleConsumer = singleConsumer;
    }

    private @NotNull Flowable<T> applySingleConsumer() {
        if (singleConsumer != null) {
            return source.map(new SingleMapper<>(singleCaster, singleConsumer));
        }
        return source;
    }

    @Override
    public @NotNull FlowableWithSingle<F, S> observeOnBoth(final @NotNull Scheduler scheduler) {
        return new FlowableWithSingleSplit<>(
                applySingleConsumer().observeOn(scheduler), flowableCaster, singleCaster, null);
    }

    @Override
    public @NotNull FlowableWithSingle<F, S> observeOnBoth(
            final @NotNull Scheduler scheduler, final boolean delayError) {

        return new FlowableWithSingleSplit<>(
                applySingleConsumer().observeOn(scheduler, delayError), flowableCaster, singleCaster, null);
    }

    @Override
    public @NotNull FlowableWithSingle<F, S> observeOnBoth(
            final @NotNull Scheduler scheduler, final boolean delayError, final int bufferSize) {

        return new FlowableWithSingleSplit<>(
                applySingleConsumer().observeOn(scheduler, delayError, bufferSize), flowableCaster, singleCaster, null);
    }

    @Override
    public @NotNull <SM> FlowableWithSingle<F, SM> mapSingle(final @NotNull Function<S, SM> singleMapper) {
        Preconditions.checkNotNull(singleMapper, "Single mapper must not be null.");
        return new FlowableWithSingleSplit<>(source, flowableCaster,
                mapSingleCaster(singleCaster, singleMapper, singleConsumer), null);
    }

    @Override
    public @NotNull <FM, SM> FlowableWithSingle<FM, SM> mapBoth(
            final @NotNull Function<F, FM> flowableMapper, final @NotNull Function<S, SM> singleMapper) {

        Preconditions.checkNotNull(singleMapper, "Single mapper must not be null.");
        Preconditions.checkNotNull(flowableMapper, "Flowable mapper must not be null.");
        return new FlowableWithSingleSplit<>(source, mapCaster(flowableCaster, flowableMapper),
                mapSingleCaster(singleCaster, singleMapper, singleConsumer), null);
    }

    @Override
    public @NotNull FlowableWithSingle<F, S> mapError(final @NotNull Function<Throwable, Throwable> mapper) {
        Preconditions.checkNotNull(mapper, "Mapper must not be null.");
        final Function<Throwable, Flowable<T>> resumeMapper = throwable -> Flowable.error(mapper.apply(throwable));
        return new FlowableWithSingleSplit<>(
                source.onErrorResumeNext(resumeMapper), flowableCaster, singleCaster, singleConsumer);
    }

    @Override
    public @NotNull FlowableWithSingle<F, S> doOnSingle(final @NotNull Consumer<S> singleConsumer) {
        Preconditions.checkNotNull(singleConsumer, "Single consumer must not be null.");
        return new FlowableWithSingleSplit<>(
                source, flowableCaster, singleCaster, combineConsumer(this.singleConsumer, singleConsumer));
    }

    @Override
    public void subscribeBoth(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        new FlowableWithSingleSplit<>(source, flowableCaster, singleCaster,
                combineConsumer(singleConsumer, subscriber::onSingle)).subscribe(subscriber);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super F> s) {
        source.filter(new CasterPredicate<>(flowableCaster, singleCaster, singleConsumer))
                .map(flowableCaster)
                .subscribe(s);
    }

    private static class CasterPredicate<T, F, S> implements Predicate<T> {

        private final @NotNull Function<T, F> flowableCaster;
        private @Nullable Function<T, S> singleCaster;
        private @Nullable Consumer<S> singleConsumer;

        private CasterPredicate(
                final @NotNull Function<T, F> flowableCaster, final @NotNull Function<T, S> singleCaster,
                final @Nullable Consumer<S> singleConsumer) {

            this.singleCaster = singleCaster;
            this.flowableCaster = flowableCaster;
            this.singleConsumer = singleConsumer;
        }

        @Override
        public boolean test(final @NotNull T t) throws Exception {
            if (flowableCaster.apply(t) != null) {
                return true;
            }
            if (singleCaster != null) {
                final S single = singleCaster.apply(t);
                if (single != null) {
                    if (singleConsumer != null) {
                        singleConsumer.accept(single);
                        singleConsumer = null;
                    }
                    singleCaster = null;
                }
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, C> @NotNull Function<T, C> caster(final @NotNull Class<C> castedClass) {
        return t -> (castedClass.isInstance(t)) ? (C) t : null;
    }

    private static <T, C, M> @NotNull Function<T, M> mapCaster(
            final @NotNull Function<T, C> caster, final @NotNull Function<C, M> mapper) {

        return t -> {
            final C c = caster.apply(t);
            return (c == null) ? null : mapper.apply(c);
        };
    }

    private static <T, S, M> @NotNull Function<T, M> mapSingleCaster(
            final @NotNull Function<T, S> caster, final @NotNull Function<S, M> mapper,
            final @Nullable Consumer<S> singleConsumer) {

        if (singleConsumer == null) {
            return mapCaster(caster, mapper);
        }
        return new SingleCasterMapper<>(caster, mapper, singleConsumer);
    }

    private static class SingleCasterMapper<T, S, M> implements Function<T, M> {

        private final @NotNull Function<T, S> caster;
        private final @NotNull Function<S, M> mapper;
        private @Nullable Consumer<S> singleConsumer;

        private SingleCasterMapper(
                final @NotNull Function<T, S> caster, final @NotNull Function<S, M> mapper,
                final @NotNull Consumer<S> singleConsumer) {

            this.caster = caster;
            this.mapper = mapper;
            this.singleConsumer = singleConsumer;
        }

        @Override
        public @NotNull M apply(final @NotNull T t) throws Exception {
            final S single = caster.apply(t);
            if (singleConsumer != null) {
                singleConsumer.accept(single);
                singleConsumer = null;
            }
            return mapper.apply(single);
        }
    }

    private static class SingleMapper<T, S> implements Function<T, T> {

        private @Nullable Function<T, S> singleCaster;
        private @Nullable Consumer<S> singleConsumer;

        private SingleMapper(final @NotNull Function<T, S> singleCaster, final @NotNull Consumer<S> singleConsumer) {
            this.singleCaster = singleCaster;
            this.singleConsumer = singleConsumer;
        }

        @Override
        public @NotNull T apply(final @NotNull T t) throws Exception {
            if (singleCaster != null) {
                final S single = singleCaster.apply(t);
                if (single != null) {
                    if (singleConsumer != null) {
                        singleConsumer.accept(single);
                        singleConsumer = null;
                    }
                    singleCaster = null;
                }
            }
            return t;
        }
    }

    private static <T> @NotNull Consumer<T> combineConsumer(
            final @Nullable Consumer<T> c1, final @NotNull Consumer<T> c2) {

        if (c1 == null) {
            return c2;
        }
        return t -> {
            c1.accept(t);
            c2.accept(t);
        };
    }
}

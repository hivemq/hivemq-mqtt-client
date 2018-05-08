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
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import org.mqttbee.annotations.NotNull;
import org.reactivestreams.Subscription;

/**
 * A {@link Flowable} which emits a single item of type {@link S} and a flow of items of type {@link F}.
 *
 * @param <S> the type of the single item.
 * @param <F> the type of the stream of items.
 * @author Silvio Giebl
 */
public abstract class FlowableWithSingle<S, F> extends Flowable<F> {

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified
     * Scheduler.
     *
     * @param scheduler see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @NotNull
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public abstract FlowableWithSingle<S, F> observeOnWithSingle(@NotNull final Scheduler scheduler);

    /**
     * Modifies the upstream to perform its emissions and notifications including the single item on a specified
     * Scheduler.
     *
     * @param scheduler  see {@link Flowable#observeOn(Scheduler)}.
     * @param delayError see {@link Flowable#observeOn(Scheduler)}.
     * @return a {@link FlowableWithSingle} notified from the upstream on the specified Scheduler.
     * @see Flowable#observeOn(Scheduler)
     */
    @NotNull
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public abstract FlowableWithSingle<S, F> observeOnWithSingle(
            @NotNull final Scheduler scheduler, final boolean delayError);

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
    @NotNull
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public abstract FlowableWithSingle<S, F> observeOnWithSingle(
            @NotNull final Scheduler scheduler, final boolean delayError, final int bufferSize);

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
    @NotNull
    @BackpressureSupport(BackpressureKind.NONE)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract <SM, FM> FlowableWithSingle<SM, FM> mapBoth(
            @NotNull final Function<S, SM> singleMapper, @NotNull final Function<F, FM> flowableMapper);

    /**
     * Modifies the upstream so that it applies a specified function to an error which can map it to a different error.
     *
     * @param mapper the mapper function to apply to an error.
     * @return a {@link FlowableWithSingle} that applies the mapper function to an error.
     */
    @NotNull
    @BackpressureSupport(BackpressureKind.NONE)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract FlowableWithSingle<S, F> mapError(@NotNull final Function<Throwable, Throwable> mapper);

    /**
     * Modifies the upstream so that it calls a consumer on emission of the single item of type {@link S}.
     *
     * @param singleConsumer the consumer of the single item.
     * @return the modified {@link Flowable}.
     */
    @NotNull
    @BackpressureSupport(BackpressureKind.NONE)
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract Flowable<F> doOnSingle(@NotNull final BiConsumer<S, Subscription> singleConsumer);

}

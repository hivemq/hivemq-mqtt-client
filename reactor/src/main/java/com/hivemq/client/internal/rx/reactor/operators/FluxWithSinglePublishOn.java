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

package com.hivemq.client.internal.rx.reactor.operators;

import com.hivemq.client.internal.rx.reactor.CoreWithSingleConditionalSubscriber;
import com.hivemq.client.rx.reactor.CoreWithSingleSubscriber;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Fuseable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public class FluxWithSinglePublishOn<F, S> extends FluxWithSingleOperator<F, S, F, S> {

    private final @NotNull Scheduler scheduler;
    private final boolean delayError;
    private final int prefetch;

    public FluxWithSinglePublishOn(
            final @NotNull FluxWithSingle<F, S> source, final @NotNull Scheduler scheduler, final boolean delayError,
            final int prefetch) {

        super(source);
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.prefetch = prefetch;
    }

    @Override
    public void subscribe(final @NotNull CoreSubscriber<? super F> subscriber) {
        source.publishOn(scheduler, delayError, prefetch).subscribe(subscriber);
    }

    @Override
    public void subscribeBoth(final @NotNull CoreWithSingleSubscriber<? super F, ? super S> subscriber) {
        final WorkerScheduler workerScheduler = new WorkerScheduler(scheduler);
        new Adapter<>(source, subscriber, workerScheduler).publishOn(workerScheduler, delayError, prefetch)
                .subscribe(subscriber);
    }

    private static class Adapter<F, S> extends Flux<F> {

        private final @NotNull FluxWithSingle<F, S> source;
        private final @NotNull CoreWithSingleSubscriber<? super F, ? super S> withSingleSubscriber;
        private final @NotNull WorkerScheduler scheduler;

        private Adapter(
                final @NotNull FluxWithSingle<F, S> source,
                final @NotNull CoreWithSingleSubscriber<? super F, ? super S> withSingleSubscriber,
                final @NotNull WorkerScheduler scheduler) {

            this.source = source;
            this.withSingleSubscriber = withSingleSubscriber;
            this.scheduler = scheduler;
        }

        @Override
        public void subscribe(final @NotNull CoreSubscriber<? super F> subscriber) {
            if (subscriber instanceof Fuseable.ConditionalSubscriber) {
                //noinspection unchecked
                final Fuseable.ConditionalSubscriber<? super F> conditional =
                        (Fuseable.ConditionalSubscriber<? super F>) subscriber;
                source.subscribeBoth(new AdapterSubscriber.Conditional<>(conditional, withSingleSubscriber, scheduler));
            } else {
                source.subscribeBoth(new AdapterSubscriber<>(subscriber, withSingleSubscriber, scheduler));
            }
        }
    }

    private static class AdapterSubscriber<F, S, T extends CoreSubscriber<? super F>>
            implements CoreWithSingleSubscriber<F, S>, Subscription {

        final @NotNull T subscriber;
        private final @NotNull CoreWithSingleSubscriber<? super F, ? super S> withSingleSubscriber;
        private final @NotNull WorkerScheduler scheduler;
        private @Nullable Subscription subscription;

        private AdapterSubscriber(
                final @NotNull T subscriber,
                final @NotNull CoreWithSingleSubscriber<? super F, ? super S> withSingleSubscriber,
                final @NotNull WorkerScheduler scheduler) {

            this.subscriber = subscriber;
            this.withSingleSubscriber = withSingleSubscriber;
            this.scheduler = scheduler;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
        }

        @Override
        public void onSingle(final @NotNull S s) {
            scheduler.worker.schedule(() -> withSingleSubscriber.onSingle(s));
        }

        @Override
        public void onNext(final @NotNull F f) {
            subscriber.onNext(f);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }

        @Override
        public void onError(final @NotNull Throwable error) {
            subscriber.onError(error);
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
        }

        private static class Conditional<F, S>
                extends AdapterSubscriber<F, S, Fuseable.ConditionalSubscriber<? super F>>
                implements CoreWithSingleConditionalSubscriber<F, S> {

            public Conditional(
                    final @NotNull Fuseable.ConditionalSubscriber<? super F> subscriber,
                    final @NotNull CoreWithSingleSubscriber<? super F, ? super S> withSingleSubscriber,
                    final @NotNull WorkerScheduler scheduler) {

                super(subscriber, withSingleSubscriber, scheduler);
            }

            @Override
            public boolean tryOnNext(final @NotNull F f) {
                return subscriber.tryOnNext(f);
            }
        }
    }

    private static class WorkerScheduler implements Scheduler {

        private final @NotNull Scheduler scheduler;
        final @NotNull Worker worker;

        private WorkerScheduler(final @NotNull Scheduler scheduler) {
            this.scheduler = scheduler;
            worker = scheduler.createWorker();
        }

        @Override
        public @NotNull Worker createWorker() {
            return worker;
        }

        @Override
        public long now(final @NotNull TimeUnit unit) {
            return scheduler.now(unit);
        }

        @Override
        public @NotNull Disposable schedule(final @NotNull Runnable task) {
            return scheduler.schedule(task);
        }

        @Override
        public @NotNull Disposable schedule(
                final @NotNull Runnable task, final long delay, final @NotNull TimeUnit unit) {

            return scheduler.schedule(task, delay, unit);
        }

        @Override
        public @NotNull Disposable schedulePeriodically(
                final @NotNull Runnable task, final long initialDelay, final long period,
                final @NotNull TimeUnit unit) {

            return scheduler.schedulePeriodically(task, initialDelay, period, unit);
        }

        @Override
        public void start() {
            scheduler.start();
        }

        @Override
        public void dispose() {
            scheduler.dispose();
        }

        @Override
        public boolean isDisposed() {
            return scheduler.isDisposed();
        }
    }
}

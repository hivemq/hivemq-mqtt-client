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

package com.hivemq.client.internal.rx.operators;

import com.hivemq.client.internal.rx.WithSingleConditionalSubscriber;
import com.hivemq.client.rx.FlowableWithSingle;
import com.hivemq.client.rx.FlowableWithSingleSubscriber;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class FlowableWithSingleObserveOn<F, S> extends FlowableWithSingleOperator<F, S, F, S> {

    private final @NotNull Scheduler scheduler;
    private final boolean delayError;
    private final int bufferSize;

    public FlowableWithSingleObserveOn(
            final @NotNull FlowableWithSingle<F, S> source, final @NotNull Scheduler scheduler,
            final boolean delayError, final int bufferSize) {

        super(source);
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.bufferSize = bufferSize;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
        source.observeOn(scheduler, delayError, bufferSize).subscribe(subscriber);
    }

    @Override
    protected void subscribeBothActual(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        final WorkerScheduler workerScheduler = new WorkerScheduler(scheduler);
        new Adapter<>(source, subscriber, workerScheduler).observeOn(workerScheduler, delayError, bufferSize)
                .subscribe(subscriber);
    }

    private static class Adapter<F, S> extends Flowable<F> {

        private final @NotNull FlowableWithSingle<F, S> source;
        private final @NotNull WithSingleSubscriber<? super F, ? super S> withSingleSubscriber;
        private final @NotNull WorkerScheduler scheduler;

        private Adapter(
                final @NotNull FlowableWithSingle<F, S> source,
                final @NotNull WithSingleSubscriber<? super F, ? super S> withSingleSubscriber,
                final @NotNull WorkerScheduler scheduler) {

            this.source = source;
            this.withSingleSubscriber = withSingleSubscriber;
            this.scheduler = scheduler;
        }

        @Override
        protected void subscribeActual(final @NotNull Subscriber<? super F> subscriber) {
            if (subscriber instanceof ConditionalSubscriber) {
                //noinspection unchecked
                final ConditionalSubscriber<? super F> conditional = (ConditionalSubscriber<? super F>) subscriber;
                source.subscribeBoth(new AdapterSubscriber.Conditional<>(conditional, withSingleSubscriber, scheduler));
            } else {
                source.subscribeBoth(new AdapterSubscriber<>(subscriber, withSingleSubscriber, scheduler));
            }
        }
    }

    private static class AdapterSubscriber<F, S, T extends Subscriber<? super F>>
            implements FlowableWithSingleSubscriber<F, S>, Subscription {

        final @NotNull T subscriber;
        private final @NotNull WithSingleSubscriber<? super F, ? super S> withSingleSubscriber;
        private final @NotNull WorkerScheduler scheduler;
        private final @NotNull AtomicReference<Object> singleOrTerminal = new AtomicReference<>();
        private @Nullable Subscription subscription;

        private AdapterSubscriber(
                final @NotNull T subscriber,
                final @NotNull WithSingleSubscriber<? super F, ? super S> withSingleSubscriber,
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
            singleOrTerminal.set(s);
            scheduler.worker.schedule(() -> {
                withSingleSubscriber.onSingle(s);
                final Object singleOrTerminal = this.singleOrTerminal.getAndSet(null);
                if (singleOrTerminal instanceof Terminal) {
                    final Terminal terminal = (Terminal) singleOrTerminal;
                    if (terminal.error == null) {
                        subscriber.onComplete();
                    } else {
                        subscriber.onError(terminal.error);
                    }
                }
            });
        }

        @Override
        public void onNext(final @NotNull F f) {
            subscriber.onNext(f);
        }

        @Override
        public void onComplete() {
            if (singleOrTerminal.getAndSet(Terminal.COMPLETE) == null) {
                subscriber.onComplete();
            }
        }

        @Override
        public void onError(final @NotNull Throwable error) {
            if (singleOrTerminal.getAndSet(new Terminal(error)) == null) {
                subscriber.onError(error);
            }
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

        private static class Conditional<F, S> extends AdapterSubscriber<F, S, ConditionalSubscriber<? super F>>
                implements WithSingleConditionalSubscriber<F, S> {

            public Conditional(
                    final @NotNull ConditionalSubscriber<? super F> subscriber,
                    final @NotNull WithSingleSubscriber<? super F, ? super S> withSingleSubscriber,
                    final @NotNull WorkerScheduler scheduler) {

                super(subscriber, withSingleSubscriber, scheduler);
            }

            @Override
            public boolean tryOnNext(final @NotNull F f) {
                return subscriber.tryOnNext(f);
            }
        }

        private static class Terminal {

            static final @NotNull Terminal COMPLETE = new Terminal(null);

            final @Nullable Throwable error;

            private Terminal(final @Nullable Throwable error) {
                this.error = error;
            }
        }
    }

    private static class WorkerScheduler extends Scheduler {

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
        public @NotNull Disposable scheduleDirect(final @NotNull Runnable run) {
            return scheduler.scheduleDirect(run);
        }

        @Override
        public @NotNull Disposable scheduleDirect(
                final @NotNull Runnable run, final long delay, final @NotNull TimeUnit unit) {

            return scheduler.scheduleDirect(run, delay, unit);
        }

        @Override
        public @NotNull Disposable schedulePeriodicallyDirect(
                final @NotNull Runnable run, final long initialDelay, final long period, final @NotNull TimeUnit unit) {

            return scheduler.schedulePeriodicallyDirect(run, initialDelay, period, unit);
        }

        @Override
        public <S extends Scheduler & Disposable> @NotNull S when(
                final @NotNull Function<Flowable<Flowable<Completable>>, Completable> combine) {

            return scheduler.when(combine);
        }

        @Override
        public void start() {
            scheduler.start();
        }

        @Override
        public void shutdown() {
            scheduler.shutdown();
        }
    }
}

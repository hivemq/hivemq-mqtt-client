/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.mqtt.client2.libs.reactor;

import com.hivemq.mqtt.client2.libs.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Silvio Giebl
 */
class WithSingleStrictSubscriber<F, S> implements CoreWithSingleSubscriber<F, S>, Subscription {

    private final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber;

    private volatile @Nullable Subscription subscription;
    @SuppressWarnings("rawtypes")
    private static final @NotNull AtomicReferenceFieldUpdater<WithSingleStrictSubscriber, Subscription> SUBSCRIPTION =
            AtomicReferenceFieldUpdater.newUpdater(WithSingleStrictSubscriber.class, Subscription.class,
                    "subscription");

    private volatile long requested;
    @SuppressWarnings("rawtypes")
    private static final @NotNull AtomicLongFieldUpdater<WithSingleStrictSubscriber> REQUESTED =
            AtomicLongFieldUpdater.newUpdater(WithSingleStrictSubscriber.class, "requested");

    private volatile int wip;
    @SuppressWarnings("rawtypes")
    private static final @NotNull AtomicIntegerFieldUpdater<WithSingleStrictSubscriber> WIP =
            AtomicIntegerFieldUpdater.newUpdater(WithSingleStrictSubscriber.class, "wip");

    private volatile @Nullable Throwable error;
    @SuppressWarnings("rawtypes")
    private static final @NotNull AtomicReferenceFieldUpdater<WithSingleStrictSubscriber, Throwable> ERROR =
            AtomicReferenceFieldUpdater.newUpdater(WithSingleStrictSubscriber.class, Throwable.class, "error");

    private volatile boolean done;

    public WithSingleStrictSubscriber(final @NotNull WithSingleSubscriber<? super F, ? super S> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(final @NotNull Subscription subscription) {
        if (Operators.validate(this.subscription, subscription)) {

            subscriber.onSubscribe(this);

            if (Operators.setOnce(SUBSCRIPTION, this, subscription)) {
                final long requested = REQUESTED.getAndSet(this, 0L);
                if (requested != 0L) {
                    subscription.request(requested);
                }
            }
        } else {
            onError(new IllegalStateException("§2.12 violated: onSubscribe must be called at most once"));
        }
    }

    @Override
    public void onSingle(final @NotNull S s) {
        subscriber.onSingle(s);
    }

    @Override
    public void onNext(final @NotNull F f) {
        if (WIP.get(this) == 0 && WIP.compareAndSet(this, 0, 1)) {
            subscriber.onNext(f);
            if (WIP.decrementAndGet(this) != 0) {
                final Throwable error = Exceptions.terminate(ERROR, this);
                if (error != null) {
                    subscriber.onError(error);
                } else {
                    subscriber.onComplete();
                }
            }
        }
    }

    @Override
    public void onError(final @NotNull Throwable error) {
        done = true;
        if (Exceptions.addThrowable(ERROR, this, error)) {
            if (WIP.getAndIncrement(this) == 0) {
                subscriber.onError(Exceptions.terminate(ERROR, this));
            }
        } else {
            Operators.onErrorDropped(error, Context.empty());
        }
    }

    @Override
    public void onComplete() {
        done = true;
        if (WIP.getAndIncrement(this) == 0) {
            final Throwable error = Exceptions.terminate(ERROR, this);
            if (error != null) {
                subscriber.onError(error);
            } else {
                subscriber.onComplete();
            }
        }
    }

    @Override
    public void request(final long n) {
        if (n <= 0) {
            cancel();
            onError(new IllegalArgumentException("§3.9 violated: positive request amount required but it was " + n));
            return;
        }
        Subscription subscription = this.subscription;
        if (subscription != null) {
            subscription.request(n);
        } else {
            Operators.addCap(REQUESTED, this, n);
            subscription = this.subscription;
            if (subscription != null) {
                final long requested = REQUESTED.getAndSet(this, 0L);
                if (requested != 0L) {
                    subscription.request(requested);
                }
            }
        }
    }

    @Override
    public void cancel() {
        if (!done) {
            Operators.terminate(SUBSCRIPTION, this);
        }
    }
}

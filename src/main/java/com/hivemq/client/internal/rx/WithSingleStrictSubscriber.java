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

package com.hivemq.client.internal.rx;

import com.hivemq.client.rx.FlowableWithSingleSubscriber;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class WithSingleStrictSubscriber<F, S> implements FlowableWithSingleSubscriber<F, S>, Subscription {

    private final @NotNull WithSingleSubscriber<F, S> subscriber;
    private final @NotNull AtomicReference<@Nullable Subscription> subscription = new AtomicReference<>();
    private final @NotNull AtomicLong requested = new AtomicLong();
    private final @NotNull AtomicInteger wip = new AtomicInteger();
    private @Nullable Throwable error;

    public WithSingleStrictSubscriber(final @NotNull WithSingleSubscriber<F, S> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(final @NotNull Subscription subscription) {
        if (this.subscription.compareAndSet(null, this)) {
            subscriber.onSubscribe(this);
            if (this.subscription.compareAndSet(this, subscription)) {
                final long requested = this.requested.getAndSet(0);
                if (requested != 0) {
                    subscription.request(requested);
                }
            } else {
                subscription.cancel();
            }
        } else {
            subscription.cancel();
            cancel();
            onError(new IllegalStateException("§2.12 violated: onSubscribe must be called at most once"));
        }
    }

    @Override
    public void onSingle(final @NotNull S s) {
        subscriber.onSingle(s);
    }

    @Override
    public void onNext(final @NotNull F f) {
        if (wip.compareAndSet(0, 1)) {
            subscriber.onNext(f);
            if (wip.decrementAndGet() != 0) {
                if (error == null) {
                    subscriber.onComplete();
                } else {
                    subscriber.onError(error);
                    error = null;
                }
            }
        }
    }

    @Override
    public void onError(final @NotNull Throwable throwable) {
        error = throwable;
        if (wip.getAndIncrement() == 0) {
            subscriber.onError(throwable);
            error = null;
        }
    }

    @Override
    public void onComplete() {
        if (wip.getAndIncrement() == 0) {
            subscriber.onComplete();
        }
    }

    @Override
    public void request(final long n) {
        if (n <= 0) {
            cancel();
            onError(new IllegalArgumentException("§3.9 violated: positive request amount required but it was " + n));
        } else {
            Subscription subscription = this.subscription.get();
            if ((subscription != null) && (subscription != this)) {
                subscription.request(n);
            } else {
                BackpressureHelper.add(requested, n);
                subscription = this.subscription.get();
                if ((subscription != null) && (subscription != this)) {
                    final long requested = this.requested.getAndSet(0);
                    if (requested != 0) {
                        subscription.request(requested);
                    }
                }
            }
        }
    }

    @Override
    public void cancel() {
        final Subscription subscription = this.subscription.getAndSet(SubscriptionHelper.CANCELLED);
        if ((subscription != null) && (subscription != this) && (subscription != SubscriptionHelper.CANCELLED)) {
            subscription.cancel();
        }
    }
}

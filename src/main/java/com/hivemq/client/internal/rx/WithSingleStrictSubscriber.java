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

package com.hivemq.client.internal.rx;

import com.hivemq.client.rx.FlowableWithSingleSubscriber;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.BackpressureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class WithSingleStrictSubscriber<F, S> implements FlowableWithSingleSubscriber<F, S>, Subscription {

    private final @NotNull WithSingleSubscriber<F, S> subscriber;
    private final @NotNull AtomicReference<@Nullable Subscription> subscription = new AtomicReference<>();
    private final @NotNull AtomicLong requested = new AtomicLong();

    public WithSingleStrictSubscriber(final @NotNull WithSingleSubscriber<F, S> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(final @NotNull Subscription subscription) {
        subscriber.onSubscribe(subscription);
        if (this.subscription.compareAndSet(null, subscription)) {
            final long requested = this.requested.getAndSet(0);
            if (requested != 0) {
                subscription.request(requested);
            }
        } else {
            subscription.cancel();
        }
    }

    @Override
    public void onSingle(final @NotNull S s) {
        subscriber.onSingle(s);
    }

    @Override
    public void onNext(final @NotNull F f) {
        subscriber.onNext(f);
    }

    @Override
    public void onError(final @NotNull Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }

    @Override
    public void request(final long n) {
        Subscription subscription = this.subscription.get();
        if (subscription != null) {
            subscription.request(n);
        } else {
            BackpressureHelper.add(requested, n);
            subscription = this.subscription.get();
            if (subscription != null) {
                final long requested = this.requested.getAndSet(0);
                if (requested != 0) {
                    subscription.request(requested);
                }
            }
        }
    }

    @Override
    public void cancel() {
        final Subscription subscription = this.subscription.getAndSet(SubscriptionHelper.CANCELLED);
        if ((subscription != null) && (subscription != SubscriptionHelper.CANCELLED)) {
            subscription.cancel();
        }
    }
}

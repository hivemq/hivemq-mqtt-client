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

import io.reactivex.FlowableSubscriber;
import io.reactivex.internal.fuseable.QueueSubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Base for {@link io.reactivex.Flowable} operators that allow fusion.
 *
 * @param <U> the type of the upstream Flowable.
 * @param <D> the type of the downstream Flowable.
 * @param <S> the type of the downstream subscriber.
 * @author Silvio Giebl
 */
public abstract class FuseableSubscriber<U, D, S extends Subscriber<? super D>>
        implements FlowableSubscriber<U>, QueueSubscription<D> {

    protected final @NotNull S subscriber;

    protected @Nullable Subscription subscription;
    protected @Nullable QueueSubscription<U> queueSubscription;
    protected int sourceMode = NONE;

    public FuseableSubscriber(final @NotNull S subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(final @NotNull Subscription subscription) {
        this.subscription = subscription;
        if (subscription instanceof QueueSubscription) {
            //noinspection unchecked
            this.queueSubscription = (QueueSubscription<U>) subscription;
        }
        subscriber.onSubscribe(this);
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

    @Override
    public int requestFusion(final int mode) {
        if ((queueSubscription != null) && (mode & BOUNDARY) == 0) {
            sourceMode = queueSubscription.requestFusion(mode);
        }
        return sourceMode;
    }

    @Override
    public boolean isEmpty() {
        assert queueSubscription != null;
        return queueSubscription.isEmpty();
    }

    @Override
    public void clear() {
        assert queueSubscription != null;
        queueSubscription.clear();
    }

    @Override
    public final boolean offer(final @NotNull D value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public final boolean offer(final @NotNull D v1, final @NotNull D v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}

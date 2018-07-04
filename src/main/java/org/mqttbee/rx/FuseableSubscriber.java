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

import io.reactivex.FlowableSubscriber;
import io.reactivex.internal.fuseable.QueueSubscription;
import io.reactivex.plugins.RxJavaPlugins;
import org.mqttbee.annotations.NotNull;
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

    protected final S subscriber;

    protected Subscription subscription;
    protected QueueSubscription<U> queueSubscription;
    protected int sourceMode;
    protected boolean done;

    public FuseableSubscriber(@NotNull final S subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(final Subscription s) {
        this.subscription = s;
        if (s instanceof QueueSubscription) {
            @SuppressWarnings("unchecked")
            final QueueSubscription<U> qs = (QueueSubscription<U>) s;
            this.queueSubscription = qs;
        }
        subscriber.onSubscribe(this);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        subscriber.onComplete();
    }

    @Override
    public void onError(final Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        done = true;
        subscriber.onError(t);
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
    public boolean isEmpty() {
        return queueSubscription.isEmpty();
    }

    @Override
    public void clear() {
        queueSubscription.clear();
    }

    @Override
    public final boolean offer(final D value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public final boolean offer(final D v1, final D v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}

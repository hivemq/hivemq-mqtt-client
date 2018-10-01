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

package org.mqttbee.mqtt.handler.publish.outgoing;

import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.mqtt.handler.publish.outgoing.MqttPublishFlowableAckLink.LinkCancellable;
import org.mqttbee.mqtt.message.publish.MqttPublishResult;
import org.mqttbee.util.collections.ChunkedArrayQueue;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
public class MqttIncomingAckFlow implements Subscription, Runnable {

    private static final int STATE_NO_NEW_REQUESTS = 0;
    private static final int STATE_NEW_REQUESTS = 1;
    private static final int STATE_BLOCKED = 2;

    private final @NotNull Subscriber<? super MqttPublishResult> subscriber;
    private final @NotNull MqttOutgoingQosHandler outgoingQosHandler;

    private long requestedNettyLocal;
    private final @NotNull AtomicLong newRequested = new AtomicLong();
    private final @NotNull AtomicBoolean cancelled = new AtomicBoolean();
    private volatile long acknowledged;
    private long acknowledgedNettyLocal;

    private final @NotNull AtomicLong published = new AtomicLong();
    private @Nullable Throwable linkError; // synced over volatile published
    private @Nullable Throwable error; // synced over volatile published
    private final @NotNull AtomicBoolean doneEmitted = new AtomicBoolean();

    private final @NotNull ChunkedArrayQueue<MqttPublishResult> queue = new ChunkedArrayQueue<>(32);
    private final @NotNull AtomicInteger requestState = new AtomicInteger();

    private volatile @Nullable LinkCancellable linkCancellable;

    MqttIncomingAckFlow(
            final @NotNull Subscriber<? super MqttPublishResult> subscriber,
            final @NotNull MqttOutgoingQosHandler outgoingQosHandler) {

        this.subscriber = subscriber;
        this.outgoingQosHandler = outgoingQosHandler;
    }

    @CallByThread("Netty EventLoop")
    void onNext(final @NotNull MqttPublishResult result) {
        long emitted = 0;
        long acknowledged = 0;
        long requested = requested();
        if (!queue.isEmpty()) {
            outer:
            while (emitted < requested) {
                while (emitted < requested) {
                    final MqttPublishResult queuedResult = queue.poll();
                    if (queuedResult == null) {
                        break outer;
                    }
                    subscriber.onNext(queuedResult);
                    if (queuedResult.acknowledged()) {
                        acknowledged++;
                    }
                    emitted++;
                }
                requested = addNewRequested();
            }
        }
        if (emitted < requested) {
            subscriber.onNext(result);
            if (result.acknowledged()) {
                acknowledged++;
            }
            emitted++;
        } else if (cancelled.get()) {
            queue.clear();
        } else {
            queue.offer(result);
        }
        emitted(emitted);
        acknowledged(acknowledged);
    }

    @CallByThread("Netty EventLoop")
    private void emitted(final long emitted) {
        if (requestedNettyLocal != Long.MAX_VALUE) {
            requestedNettyLocal -= emitted;
        }
    }

    @CallByThread("Netty EventLoop")
    void acknowledged(final long acknowledged) {
        if (acknowledged > 0) {
            final long acknowledgedLocal = this.acknowledgedNettyLocal += acknowledged;
            this.acknowledged = acknowledgedLocal;
            if ((acknowledgedLocal == published.get()) && doneEmitted.compareAndSet(false, true)) {
                if (error != null) {
                    subscriber.onError(error);
                } else if (linkError != null) {
                    subscriber.onError(linkError);
                } else {
                    subscriber.onComplete();
                }
            }
            outgoingQosHandler.request(acknowledged);
        }
    }

    void onComplete(final long published) {
        if (!this.published.compareAndSet(0, published)) {
            return;
        }
        if ((acknowledged == published) && doneEmitted.compareAndSet(false, true)) {
            subscriber.onComplete();
        }
    }

    void onError(final @NotNull Throwable t, final long published) {
        linkError = t;
        if (!this.published.compareAndSet(0, published)) {
            RxJavaPlugins.onError(t);
            return;
        }
        if ((acknowledged == published) && doneEmitted.compareAndSet(false, true)) {
            subscriber.onError(t);
        }
    }

    @CallByThread("Netty EventLoop")
    void onError(final @NotNull Throwable t) {
        cancel();
        final long acknowledged = acknowledgedNettyLocal;
        final long published = acknowledged + queue.size();
        error = t;
        this.published.set(published);
        if ((acknowledged == published) && doneEmitted.compareAndSet(false, true)) {
            subscriber.onError(t);
        }
    }

    @Override
    public void request(final long n) {
        if (n > 0) {
            BackpressureHelper.add(newRequested, n);
            if (requestState.getAndSet(STATE_NEW_REQUESTS) == STATE_BLOCKED) {
                outgoingQosHandler.getEventLoop().execute(this);
            }
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        long emitted = 0;
        long acknowledged = 0;
        long requested = requested();
        outer:
        while (emitted < requested) {
            while (emitted < requested) {
                final MqttPublishResult queuedResult = queue.poll();
                if (queuedResult == null) {
                    break outer;
                }
                subscriber.onNext(queuedResult);
                if (queuedResult.acknowledged()) {
                    acknowledged++;
                }
                emitted++;
            }
            if (cancelled.get()) {
                queue.clear();
                break;
            }
            requested = addNewRequested();
        }
        emitted(emitted);
        acknowledged(acknowledged);
    }

    @CallByThread("Netty EventLoop")
    private long requested() {
        if (requestedNettyLocal <= 0) {
            return addNewRequested();
        }
        return requestedNettyLocal;
    }

    @CallByThread("Netty EventLoop")
    private long addNewRequested() {
        for (; ; ) { // setting both requestState and newRequested is not atomic
            if (requestState.compareAndSet(STATE_NO_NEW_REQUESTS, STATE_BLOCKED)) {
                return 0;
            } else { // requestState = STATE_NEW_REQUESTS
                requestState.set(STATE_NO_NEW_REQUESTS);
                final long newRequested = this.newRequested.getAndSet(0);
                // If request was called concurrently we may have included the newRequested amount already but
                // requestState is afterwards set to STATE_NEW_REQUESTS although newRequested is reset to 0.
                // If request is not called until the next invocation of this method, newRequested may be 0.
                if (newRequested > 0) {
                    return requestedNettyLocal = BackpressureHelper.addCap(requestedNettyLocal, newRequested);
                }
            }
        }
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            final LinkCancellable linkCancellable = this.linkCancellable;
            if (linkCancellable != null) {
                linkCancellable.cancelLink();
            }
        }
    }

    void link(final @NotNull LinkCancellable linkCancellable) {
        this.linkCancellable = linkCancellable;
        if (cancelled.get()) {
            linkCancellable.cancelLink();
        }
    }

}

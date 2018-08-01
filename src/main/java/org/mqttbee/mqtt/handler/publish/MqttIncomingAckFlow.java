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

package org.mqttbee.mqtt.handler.publish;

import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;
import org.mqttbee.annotations.CallByThread;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.mqtt.handler.publish.MqttPublishFlowableAckLink.LinkCancellable;
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

    private final Subscriber<? super Mqtt5PublishResult> subscriber;
    private final MqttOutgoingPublishService outgoingPublishService;

    private long requestedNettyLocal;
    private final AtomicLong newRequested = new AtomicLong();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private volatile long acknowledged;
    private long acknowledgedNettyLocal;

    private boolean done;
    private volatile long published;
    private Throwable error; // synced over volatile published
    private final AtomicBoolean doneEmitted = new AtomicBoolean();

    private final ChunkedArrayQueue<Mqtt5PublishResult> queue;
    private final AtomicInteger requestState = new AtomicInteger();

    private volatile LinkCancellable linkCancellable;

    MqttIncomingAckFlow(
            @NotNull final Subscriber<? super Mqtt5PublishResult> subscriber,
            @NotNull final MqttOutgoingPublishService outgoingPublishService) {

        this.subscriber = subscriber;
        this.outgoingPublishService = outgoingPublishService;
        queue = new ChunkedArrayQueue<>(64);
    }

    @CallByThread("Netty EventLoop")
    void onNext(@NotNull final Mqtt5PublishResult result) {
        long emitted = 0;
        long requested = requested();
        if (!queue.isEmpty()) {
            outer:
            while (emitted < requested) {
                while (emitted < requested) {
                    final Mqtt5PublishResult queuedResult = queue.poll();
                    if (queuedResult == null) {
                        break outer;
                    }
                    subscriber.onNext(queuedResult);
                    emitted++;
                }
                requested = addNewRequested();
            }
        }
        if (emitted < requested) {
            subscriber.onNext(result);
            emitted++;
        } else if (cancelled.get()) {
            queue.clear();
        } else {
            queue.offer(result);
        }
        emitted(emitted);
    }

    @CallByThread("Netty EventLoop")
    private void emitted(final long emitted) {
        if (emitted > 0) {
            final long acknowledgedLocal = this.acknowledgedNettyLocal += emitted;
            acknowledged = acknowledgedLocal;
            if (acknowledgedLocal == published && doneEmitted.compareAndSet(false, true)) {
                if (error == null) {
                    subscriber.onComplete();
                } else {
                    subscriber.onError(error);
                }
            }
            if (requestedNettyLocal != Long.MAX_VALUE) {
                requestedNettyLocal -= emitted;
                outgoingPublishService.request(emitted);
            }
        }
    }

    void onComplete(final long published) {
        if (done) {
            return;
        }
        done = true;
        this.published = published;
        if ((acknowledged == published) && doneEmitted.compareAndSet(false, true)) {
            subscriber.onComplete();
        }
    }

    void onError(@NotNull final Throwable t, final long published) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        done = true;
        error = t;
        this.published = published;
        if ((acknowledged == published) && doneEmitted.compareAndSet(false, true)) {
            subscriber.onError(t);
        }
    }

    @Override
    public void request(final long n) {
        if (n > 0) {
            BackpressureHelper.add(newRequested, n);
            if (requestState.getAndSet(STATE_NEW_REQUESTS) == STATE_BLOCKED) {
                outgoingPublishService.getNettyEventLoop().execute(this);
            }
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        long emitted = 0;
        long requested = requested();
        outer:
        while (emitted < requested) {
            while (emitted < requested) {
                final Mqtt5PublishResult queuedResult = queue.poll();
                if (queuedResult == null) {
                    break outer;
                }
                subscriber.onNext(queuedResult);
                emitted++;
            }
            if (cancelled.get()) {
                queue.clear();
                break;
            }
            requested = addNewRequested();
        }
        emitted(emitted);
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

    void link(@NotNull final LinkCancellable linkCancellable) {
        this.linkCancellable = linkCancellable;
        if (cancelled.get()) {
            linkCancellable.cancelLink();
        }
    }

}

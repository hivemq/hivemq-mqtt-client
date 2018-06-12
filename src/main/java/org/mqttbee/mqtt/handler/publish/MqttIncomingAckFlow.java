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
import org.mqttbee.annotations.NotNull;
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

    private final AtomicInteger wip = new AtomicInteger();
    private final ChunkedArrayQueue<Mqtt5PublishResult> queue;
    private volatile boolean queued;

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
        int missed = wip.incrementAndGet();

        long emitted = 0;
        while (true) {
            final long requested = requested();
            if (!queue.isEmpty()) {
                while (emitted != requested) {
                    final Mqtt5PublishResult queuedResult = queue.poll();
                    if (queuedResult == null) {
                        break;
                    }
                    subscriber.onNext(queuedResult);
                    emitted++;
                }
            }
            if (requested > emitted) {
                subscriber.onNext(result);
                emitted++;
                queued = false;
            } else {
                queue.offer(result);
                queued = false;
            }

            final int wip = this.wip.get();
            if (missed == wip) {
                missed = this.wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            } else {
                missed = wip;
            }
        }
        emitted(emitted);
    }

    @CallByThread("Netty EventLoop")
    private long requested() {
        if (requestedNettyLocal == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return this.requestedNettyLocal += newRequested.getAndSet(0);
    }

    @CallByThread("Netty EventLoop")
    private void emitted(final long emitted) {
        if (emitted > 0) {
            final long acknowledgedLocal = this.acknowledgedNettyLocal += emitted;
            acknowledged = acknowledgedLocal;
            if (acknowledgedLocal == published) {
                if (doneEmitted.compareAndSet(false, true)) {
                    if (error == null) {
                        subscriber.onComplete();
                    } else {
                        subscriber.onError(error);
                    }
                    queued = false;
                }
                return;
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
            queued = false;
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
            queued = false;
        }
    }

    @Override
    public void request(final long n) {
        BackpressureHelper.add(newRequested, n);
        if ((wip.getAndIncrement() == 0) && queued) {
            outgoingPublishService.getNettyEventLoop().execute(this);
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        int missed = wip.get();
        if (missed == 0) {
            return;
        }

        long emitted = 0;
        while (true) {
            final long requested = requested();
            while (emitted != requested) {
                final Mqtt5PublishResult queuedResult = queue.poll();
                if (queuedResult == null) {
                    break;
                }
                subscriber.onNext(queuedResult);
                emitted++;
            }
            if (queue.isEmpty()) {
                queued = false;
            }

            final int wip = this.wip.get();
            if (missed == wip) {
                missed = this.wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            } else {
                missed = wip;
            }
        }
        emitted(emitted);
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

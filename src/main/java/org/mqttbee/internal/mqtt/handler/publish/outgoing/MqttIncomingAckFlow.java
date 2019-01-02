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

package org.mqttbee.internal.mqtt.handler.publish.outgoing;

import io.netty.channel.EventLoop;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.internal.mqtt.handler.publish.outgoing.MqttPublishFlowableAckLink.LinkCancellable;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult;
import org.mqttbee.util.ExecutorUtil;
import org.mqttbee.util.collections.ChunkedArrayQueue;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class MqttIncomingAckFlow implements Subscription, Runnable {

    private static final int STATE_NO_NEW_REQUESTS = 0;
    private static final int STATE_NEW_REQUESTS = 1;
    private static final int STATE_BLOCKED = 2;

    private static final int STATE_NOT_DONE = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_CANCELLED = 2;

    private final @NotNull Subscriber<? super MqttPublishResult> subscriber;
    private final @NotNull MqttOutgoingQosHandler outgoingQosHandler;
    private final @NotNull EventLoop eventLoop;

    private long requestedNettyLocal;
    private final @NotNull AtomicLong newRequested = new AtomicLong();
    private final @NotNull AtomicInteger requestState = new AtomicInteger(STATE_NO_NEW_REQUESTS);
    private final @NotNull AtomicInteger doneState = new AtomicInteger(STATE_NOT_DONE);

    private volatile long acknowledged;
    private long acknowledgedNettyLocal;
    private final @NotNull AtomicLong published = new AtomicLong();
    private @Nullable Throwable linkError; // synced over volatile published
    private @Nullable Throwable error; // synced over volatile published

    private final @NotNull ChunkedArrayQueue<MqttPublishResult> queue = new ChunkedArrayQueue<>(32);

    private final @NotNull AtomicReference<@Nullable LinkCancellable> linkCancellable = new AtomicReference<>();

    MqttIncomingAckFlow(
            final @NotNull Subscriber<? super MqttPublishResult> subscriber,
            final @NotNull MqttOutgoingQosHandler outgoingQosHandler) {

        this.subscriber = subscriber;
        this.outgoingQosHandler = outgoingQosHandler;
        this.eventLoop = outgoingQosHandler.getClientConfig().acquireEventLoop();
    }

    @CallByThread("Netty EventLoop")
    void onNext(final @NotNull MqttPublishResult result) {
        long emitted = 0;
        long acknowledged = 0;
        long requested = requested();
        if (!queue.isEmpty()) {
            outer:
            while (emitted < requested) {
                do {
                    final MqttPublishResult queuedResult = queue.poll();
                    if (queuedResult == null) {
                        break outer;
                    }
                    subscriber.onNext(queuedResult);
                    if (queuedResult.acknowledged()) {
                        acknowledged++;
                    }
                    emitted++;
                } while (emitted < requested);
                if (isCancelled()) {
                    queue.clear();
                    break;
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
        } else if (isCancelled()) {
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
            if ((acknowledgedLocal == published.get()) && doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
                if (error != null) {
                    subscriber.onError(error);
                } else if (linkError != null) {
                    subscriber.onError(linkError);
                } else {
                    subscriber.onComplete();
                }
                releaseEventLoop();
            }
            outgoingQosHandler.request(acknowledged);
        }
    }

    void onComplete(final long published) {
        if (!this.published.compareAndSet(0, published)) {
            return;
        }
        if ((acknowledged == published) && doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
            subscriber.onComplete();
            releaseEventLoop();
        }
    }

    void onError(final @NotNull Throwable t, final long published) {
        linkError = t;
        if (!this.published.compareAndSet(0, published)) {
            RxJavaPlugins.onError(t);
            return;
        }
        if ((acknowledged == published) && doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
            subscriber.onError(t);
            releaseEventLoop();
        }
    }

    @CallByThread("Netty EventLoop")
    void onError(final @NotNull Throwable t) {
        cancelLink();
        final long acknowledged = acknowledgedNettyLocal;
        final long published = acknowledged + queue.size();
        error = t;
        this.published.set(published);
        if ((acknowledged == published) && doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
            subscriber.onError(t);
            releaseEventLoop();
        }
    }

    @Override
    public void request(final long n) {
        if ((n > 0) && !isCancelled()) {
            BackpressureHelper.add(newRequested, n);
            if (requestState.getAndSet(STATE_NEW_REQUESTS) == STATE_BLOCKED) {
                ExecutorUtil.execute(eventLoop, this);
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
            do {
                final MqttPublishResult queuedResult = queue.poll();
                if (queuedResult == null) {
                    break outer;
                }
                subscriber.onNext(queuedResult);
                if (queuedResult.acknowledged()) {
                    acknowledged++;
                }
                emitted++;
            } while (emitted < requested);
            if (isCancelled()) {
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
        if (doneState.compareAndSet(STATE_NOT_DONE, STATE_CANCELLED)) {
            cancelLink();
            releaseEventLoop();
        }
    }

    private boolean isCancelled() {
        return doneState.get() == STATE_CANCELLED;
    }

    private void cancelLink() {
        final LinkCancellable linkCancellable = this.linkCancellable.getAndSet(LinkCancellable.CANCELLED);
        if (linkCancellable != null) {
            linkCancellable.cancelLink();
        }
    }

    void link(final @NotNull LinkCancellable linkCancellable) {
        if (!this.linkCancellable.compareAndSet(null, linkCancellable)) {
            linkCancellable.cancelLink();
        }
    }

    private void releaseEventLoop() {
        outgoingQosHandler.getClientConfig().releaseEventLoop();
    }
}

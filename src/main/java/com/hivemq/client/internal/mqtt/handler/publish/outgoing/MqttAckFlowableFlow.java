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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttPublishFlowableAckLink.LinkedFlow;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult;
import com.hivemq.client.internal.util.collections.ChunkedArrayQueue;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
class MqttAckFlowableFlow extends MqttAckFlow implements Subscription, Runnable {

    private static final int STATE_NO_NEW_REQUESTS = 0;
    private static final int STATE_NEW_REQUESTS = 1;
    private static final int STATE_BLOCKED = 2;

    private final @NotNull Subscriber<? super MqttPublishResult> subscriber;
    private final @NotNull MqttOutgoingQosHandler outgoingQosHandler;

    private long requested;
    private final @NotNull AtomicLong newRequested = new AtomicLong();
    private final @NotNull AtomicInteger requestState = new AtomicInteger(STATE_NO_NEW_REQUESTS);

    private volatile long acknowledged;
    private final @NotNull AtomicLong published = new AtomicLong();
    private @Nullable Throwable error; // synced over volatile published

    private final @NotNull ChunkedArrayQueue<MqttPublishResult> queue = new ChunkedArrayQueue<>(32);

    private final @NotNull AtomicReference<@Nullable LinkedFlow> linkedFlow = new AtomicReference<>();

    MqttAckFlowableFlow(
            final @NotNull Subscriber<? super MqttPublishResult> subscriber,
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttOutgoingQosHandler outgoingQosHandler) {

        super(clientConfig);
        this.subscriber = subscriber;
        this.outgoingQosHandler = outgoingQosHandler;
        init();
    }

    @CallByThread("Netty EventLoop")
    @Override
    void onNext(final @NotNull MqttPublishResult result) {
        queue.offer(result);
        run();
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
                final MqttPublishResult result = queue.poll();
                if (result == null) {
                    break outer;
                }
                subscriber.onNext(result);
                if (result.acknowledged()) {
                    acknowledged++;
                }
                emitted++;
            } while (emitted < requested);

            if (isCancelled()) {
                MqttPublishResult result;
                while ((result = queue.poll()) != null) {
                    if (result.acknowledged()) {
                        acknowledged++;
                    }
                }
                break;
            }
            requested = addNewRequested();
        }
        if (this.requested != Long.MAX_VALUE) {
            this.requested -= emitted;
        }
        acknowledged(acknowledged);
    }

    @CallByThread("Netty EventLoop")
    @Override
    void acknowledged(final long newAcknowledged) {
        if (newAcknowledged > 0) {
            final long acknowledged = this.acknowledged + newAcknowledged;
            this.acknowledged = acknowledged;
            if ((acknowledged == published.get()) && setDone()) {
                if (error != null) {
                    subscriber.onError(error);
                } else {
                    subscriber.onComplete();
                }
            }
            outgoingQosHandler.request(newAcknowledged);
        }
    }

    void onComplete(final long published) {
        if (!this.published.compareAndSet(0, published)) {
            return;
        }
        if ((acknowledged == published) && setDone()) {
            subscriber.onComplete();
        }
    }

    void onError(final @NotNull Throwable error, final long published) {
        this.error = error;
        if (!this.published.compareAndSet(0, published)) {
            RxJavaPlugins.onError(error);
            return;
        }
        if ((acknowledged == published) && setDone()) {
            subscriber.onError(error);
        }
    }

    @Override
    public void request(final long n) {
        if ((n > 0) && !isCancelled()) {
            BackpressureHelper.add(newRequested, n);
            if (requestState.getAndSet(STATE_NEW_REQUESTS) == STATE_BLOCKED) {
                eventLoop.execute(this);
                // event loop is acquired even if done:
                // - cancelled is checked
                // - onComplete/onError wait for the queue to be empty -> requestState != STATE_BLOCKED
            }
        }
    }

    @CallByThread("Netty EventLoop")
    private long requested() {
        return (requested > 0) ? requested : addNewRequested();
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
                    return requested = BackpressureHelper.addCap(requested, newRequested);
                }
            }
        }
    }

    @Override
    protected void onCancel() {
        if (requestState.get() == STATE_BLOCKED) {
            eventLoop.execute(this); // clear queue and request unconsumed amount from outgoingQosHandler
        }
        cancelLink();
    }

    private void cancelLink() {
        final LinkedFlow linkedFlow = this.linkedFlow.getAndSet(LinkedFlow.CANCELLED);
        if (linkedFlow != null) {
            linkedFlow.cancelLink();
        }
    }

    void link(final @NotNull LinkedFlow linkedFlow) {
        if (!this.linkedFlow.compareAndSet(null, linkedFlow)) {
            linkedFlow.cancelLink();
        }
    }
}

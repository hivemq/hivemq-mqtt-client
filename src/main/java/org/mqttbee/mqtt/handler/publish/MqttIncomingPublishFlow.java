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

import io.reactivex.Emitter;
import io.reactivex.internal.util.BackpressureHelper;
import org.mqttbee.annotations.CallByThread;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
public abstract class MqttIncomingPublishFlow<S extends Subscriber<? super Mqtt5Publish>>
        implements Emitter<Mqtt5Publish>, Subscription, Runnable {

    private static final int STATE_NO_NEW_REQUESTS = 0;
    private static final int STATE_NEW_REQUESTS = 1;
    private static final int STATE_BLOCKED = 2;

    final MqttIncomingPublishService incomingPublishService;
    final S subscriber;

    long requested;
    private final AtomicLong newRequested = new AtomicLong();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private boolean unsubscribed;
    boolean done;

    private int referenced;
    private long blockedIndex;
    private boolean blocking;
    private final AtomicInteger requestState = new AtomicInteger();

    MqttIncomingPublishFlow(
            @NotNull final MqttIncomingPublishService incomingPublishService, @NotNull final S subscriber) {

        this.incomingPublishService = incomingPublishService;
        this.subscriber = subscriber;
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void onNext(@NotNull final Mqtt5Publish result) {
        if (done) {
            return;
        }
        subscriber.onNext(result);
        if (requested != Long.MAX_VALUE) {
            requested--;
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void onError(@NotNull final Throwable t) {
        if (done) {
            return;
        }
        done = true;
        subscriber.onError(t);
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        subscriber.onComplete();
    }

    @Override
    public void request(final long n) {
        if (n > 0) {
            BackpressureHelper.add(newRequested, n);
            if (requestState.getAndSet(STATE_NEW_REQUESTS) == STATE_BLOCKED) {
                incomingPublishService.getNettyEventLoop().execute(this);
            }
        }
    }

    @CallByThread("Netty EventLoop")
    public void run() { // only executed if was blocking
        if (referenced > 0) { // is blocking
            incomingPublishService.drain();
        }
    }

    @CallByThread("Netty EventLoop")
    long requested(final long runIndex) {
        if (requested <= 0) {
            if (blocking && (blockedIndex != runIndex)) {
                blocking = false; // unblock in a new run iteration
            }
            if (blocking) {
                return -1;
            } else {
                for (; ; ) { // setting both requestState and newRequested is not atomic
                    if (requestState.compareAndSet(STATE_NO_NEW_REQUESTS, STATE_BLOCKED)) {
                        blockedIndex = runIndex;
                        blocking = true;
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
        }
        return requested;
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            incomingPublishService.getNettyEventLoop().execute(this::runCancel);
        }
    }

    @CallByThread("Netty EventLoop")
    void runCancel() { // always executed if cancelled
        if (referenced > 0) { // is blocking
            incomingPublishService.drain();
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    @CallByThread("Netty EventLoop")
    void unsubscribe() {
        unsubscribed = true;
        if (referenced == 0) {
            onComplete();
        } else {
            incomingPublishService.drain();
        }
    }

    boolean isUnsubscribed() {
        return unsubscribed;
    }

    int reference() {
        return ++referenced;
    }

    int dereference() {
        return --referenced;
    }

}

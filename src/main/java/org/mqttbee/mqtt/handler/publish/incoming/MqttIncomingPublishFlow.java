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

package org.mqttbee.mqtt.handler.publish.incoming;

import io.netty.channel.EventLoop;
import io.reactivex.Emitter;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.util.ExecutorUtil;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

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

    private static final int STATE_NOT_DONE = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_CANCELLED = 2;

    final @NotNull S subscriber;
    final @NotNull MqttIncomingQosHandler incomingQosHandler;
    private final @NotNull EventLoop eventLoop;

    long requested;
    private final @NotNull AtomicLong newRequested = new AtomicLong();
    private final @NotNull AtomicInteger requestState = new AtomicInteger(STATE_NO_NEW_REQUESTS);
    private final @NotNull AtomicInteger doneState = new AtomicInteger(STATE_NOT_DONE);

    boolean done;
    private @Nullable Throwable error;

    private int referenced;
    private long blockedIndex;
    private boolean blocking;

    MqttIncomingPublishFlow(final @NotNull S subscriber, final @NotNull MqttIncomingQosHandler incomingQosHandler) {
        this.subscriber = subscriber;
        this.incomingQosHandler = incomingQosHandler;
        eventLoop = incomingQosHandler.getClientConfig().acquireEventLoop();
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void onNext(final @NotNull Mqtt5Publish result) {
        subscriber.onNext(result);
        if (requested != Long.MAX_VALUE) {
            requested--;
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        if (referenced == 0) {
            subscriber.onComplete();
            if (doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
                releaseEventLoop();
            }
        } else {
            incomingQosHandler.getIncomingPublishService().drain();
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void onError(final @NotNull Throwable t) {
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        error = t;
        done = true;
        if (referenced == 0) {
            subscriber.onError(t);
            if (doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
                releaseEventLoop();
            }
        } else {
            incomingQosHandler.getIncomingPublishService().drain();
        }
    }

    @CallByThread("Netty EventLoop")
    void checkDone() {
        if (done) {
            if (error != null) {
                subscriber.onError(error);
            } else {
                subscriber.onComplete();
            }
            if (doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
                releaseEventLoop();
            }
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
    public void run() { // only executed if was blocking
        if (referenced > 0) { // is blocking
            incomingQosHandler.getIncomingPublishService().drain();
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
        if (doneState.compareAndSet(STATE_NOT_DONE, STATE_CANCELLED)) {
            eventLoop.execute(this::runCancel);
            releaseEventLoop();
        }
    }

    @CallByThread("Netty EventLoop")
    void runCancel() { // always executed if cancelled
        if (referenced > 0) { // is blocking
            incomingQosHandler.getIncomingPublishService().drain();
        }
    }

    public boolean isCancelled() {
        return doneState.get() == STATE_CANCELLED;
    }

    @CallByThread("Netty EventLoop")
    int reference() {
        return ++referenced;
    }

    @CallByThread("Netty EventLoop")
    int dereference() {
        return --referenced;
    }

    public @NotNull EventLoop getEventLoop() {
        return eventLoop;
    }

    private void releaseEventLoop() {
        incomingQosHandler.getClientConfig().releaseEventLoop();
    }
}

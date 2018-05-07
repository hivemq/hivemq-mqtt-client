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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
public abstract class MqttIncomingPublishFlow implements Emitter<Mqtt5Publish>, Subscription {

    final MqttIncomingPublishService incomingPublishService;

    long requested;
    private final AtomicLong newRequested = new AtomicLong();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean unsubscribed = new AtomicBoolean();
    boolean done;

    private final AtomicInteger referenced = new AtomicInteger();
    private final AtomicBoolean blocking = new AtomicBoolean();

    private final Runnable requestRunnable = this::runRequest;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    MqttIncomingPublishFlow(@NotNull final MqttIncomingPublishService incomingPublishService) {
        this.incomingPublishService = incomingPublishService;
    }

    @NotNull
    abstract Subscriber<? super Mqtt5Publish> getSubscriber();

    @Override
    public void onNext(@NotNull final Mqtt5Publish result) {
        if (done) {
            return;
        }
        getSubscriber().onNext(result);
        if (requested != Long.MAX_VALUE) {
            requested--;
        }
    }

    @Override
    public void onError(@NotNull final Throwable t) {
        if (done) {
            return;
        }
        done = true;
        getSubscriber().onError(t);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        getSubscriber().onComplete();
    }

    @Override
    public void request(final long n) {
        if (!cancelled.get()) {
            BackpressureHelper.add(newRequested, n);
            schedule(requestRunnable);
        }
    }

    private void runRequest() {
        scheduled.set(false);
        applyRequests();
        if (referenced.get() > 0) {
            incomingPublishService.eventLoop();
        }
    }

    long requested() {
        return requested;
    }

    long applyRequests() { // called sequentially with onNext
        long requested = this.requested;
        if (requested == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        final long newRequested = this.newRequested.getAndSet(0);
        if (newRequested != 0) {
            if (requested == 0) {
                blocking.set(false);
            }
            requested = BackpressureHelper.addCap(requested, newRequested);
            this.requested = requested;
        }
        return requested;
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            incomingPublishService.getNettyEventLoop().execute(this::runRemoveOnCancel);
            schedule(this::runCancel);
        }
    }

    private void runCancel() {
        scheduled.set(false);
        // onComplete(); no onComplete on cancel
        if (referenced.get() > 0) {
            incomingPublishService.eventLoop();
        }
    }

    abstract void runRemoveOnCancel();

    boolean isCancelled() {
        return cancelled.get();
    }

    void unsubscribe() {
        unsubscribed.set(true);
        schedule(this::runUnsubscribe);
    }

    private void runUnsubscribe() {
        scheduled.set(false);
        if (referenced.get() == 0) {
            onComplete();
        } else {
            incomingPublishService.eventLoop();
        }
    }

    boolean isUnsubscribed() {
        return unsubscribed.get();
    }

    void reference() {
        referenced.getAndIncrement();
    }

    int dereference() {
        return referenced.decrementAndGet();
    }

    void setBlocking() {
        blocking.set(true);
    }

    private void schedule(@NotNull final Runnable runnable) {
        if (scheduled.compareAndSet(false, true)) {
            if (blocking.get()) {
                incomingPublishService.requestOnBlocking();
            }
            incomingPublishService.getRxEventLoop().schedule(runnable);
        }
    }

}

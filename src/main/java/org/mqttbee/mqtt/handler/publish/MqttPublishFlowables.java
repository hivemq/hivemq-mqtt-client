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

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.internal.util.BackpressureHelper;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttPublishFlowables extends Flowable<Flowable<MqttPublishWithFlow>> implements Subscription, Runnable {

    private static final int MAX_CONCURRENT_PUBLISH_FLOWABLES = 100;

    private final Scheduler.Worker rxEventLoop;

    private Subscriber<? super Flowable<MqttPublishWithFlow>> actual;

    private final AtomicLong requested = new AtomicLong();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    private final LinkedBlockingQueue<Flowable<MqttPublishWithFlow>> queue;
    private final AtomicInteger wip = new AtomicInteger();

    @Inject
    MqttPublishFlowables(@Named("outgoingPublishFlows") final Scheduler.Worker rxEventLoop) {
        this.rxEventLoop = rxEventLoop;

        queue = new LinkedBlockingQueue<>(MAX_CONCURRENT_PUBLISH_FLOWABLES);
    }

    @Override
    protected void subscribeActual(final Subscriber<? super Flowable<MqttPublishWithFlow>> s) {
        assert actual == null;

        actual = s;
        s.onSubscribe(this);
    }

    public void add(@NotNull final Flowable<MqttPublishWithFlow> publishFlowable) {
        try {
            queue.put(publishFlowable);
            trySchedule();
        } catch (final InterruptedException e) {
            // TODO log error
        }
    }

    @Override
    public void request(final long n) {
        BackpressureHelper.add(requested, n);
        trySchedule();
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            trySchedule();
        }
    }

    private void trySchedule() {
        if (wip.getAndIncrement() == 0) {
            rxEventLoop.schedule(this);
        }
    }

    @Override
    public void run() {
        int missed = 1;

        final Subscriber<? super Flowable<MqttPublishWithFlow>> actual = this.actual;
        final LinkedBlockingQueue<Flowable<MqttPublishWithFlow>> queue = this.queue;

        long emitted = 0;

        while (true) {
            final long requested = this.requested.get();

            while (emitted != requested) {
                if (checkCancelled()) {
                    return;
                }

                final Flowable<MqttPublishWithFlow> publishFlowable = queue.poll();
                if (publishFlowable == null) {
                    break;
                }
                actual.onNext(publishFlowable);
                emitted++;
            }

            if ((emitted == requested) && checkCancelled()) {
                return;
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
        if (emitted > 0) {
            if (requested.get() != Long.MAX_VALUE) {
                requested.addAndGet(-emitted);
            }
        }
    }

    private boolean checkCancelled() {
        if (cancelled.get()) {
            queue.clear();
            return true;
        }
        return false;
    }

}

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

package com.hivemq.client2.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client2.internal.logging.InternalLogger;
import com.hivemq.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.client2.internal.mqtt.ioc.ClientScope;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttPublishFlowables extends Flowable<Flowable<MqttPublishWithFlow>> implements Subscription {

    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttPublishFlowables.class);

    private @Nullable Subscriber<? super Flowable<MqttPublishWithFlow>> subscriber;
    private long requested;

    MqttPublishFlowables() {}

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Flowable<MqttPublishWithFlow>> s) {
        assert subscriber == null;
        subscriber = s;
        s.onSubscribe(this);
    }

    public void add(final @NotNull Flowable<MqttPublishWithFlow> publishFlowable) {
        synchronized (this) {
            while (requested == 0) {
                try {
                    this.wait();
                } catch (final InterruptedException e) {
                    LOGGER.error("thread interrupted while waiting to publish.", e);
                    return;
                }
            }
            assert subscriber != null;
            subscriber.onNext(publishFlowable);
            requested--;
        }
    }

    @Override
    public void request(final long n) {
        synchronized (this) {
            requested = BackpressureHelper.addCap(requested, n);
            if (requested == n) {
                this.notifyAll();
            }
        }
    }

    @Override
    public void cancel() {
        subscriber = null;
    }
}

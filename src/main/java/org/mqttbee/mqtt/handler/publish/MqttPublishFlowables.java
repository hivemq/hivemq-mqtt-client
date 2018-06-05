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
import io.reactivex.internal.util.BackpressureHelper;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttPublishFlowables extends Flowable<Flowable<MqttPublishWithFlow>> implements Subscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishFlowables.class);

    private Subscriber<? super Flowable<MqttPublishWithFlow>> subscriber;
    private final AtomicLong requested = new AtomicLong();

    @Inject
    MqttPublishFlowables() {
    }

    @Override
    protected void subscribeActual(final Subscriber<? super Flowable<MqttPublishWithFlow>> s) {
        assert subscriber == null;
        subscriber = s;
        s.onSubscribe(this);
    }

    public void add(@NotNull final Flowable<MqttPublishWithFlow> publishFlowable) {
        synchronized (this) {
            if (requested.get() == 0) {
                try {
                    this.wait();
                } catch (final InterruptedException e) {
                    LOGGER.error("thread interrupted while waiting to publish.", e);
                }
            }
            subscriber.onNext(publishFlowable);
        }
    }

    @Override
    public void request(final long n) {
        BackpressureHelper.add(requested, n);
        if (requested.get() == n) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    @Override
    public void cancel() {
        LOGGER.error("MqttPublishFlowables is global and should never be cancelled.");
    }

}

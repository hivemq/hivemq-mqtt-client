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

package org.mqttbee.internal.mqtt.handler.subscribe;

import io.netty.channel.EventLoop;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.rx.SingleFlow;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
class MqttSubOrUnsubAckFlow<T> implements SingleFlow<T>, Disposable {

    private final @NotNull SingleObserver<? super T> observer;
    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull EventLoop eventLoop;
    private final @NotNull AtomicBoolean done = new AtomicBoolean();

    MqttSubOrUnsubAckFlow(
            final @NotNull SingleObserver<? super T> observer, final @NotNull MqttClientConfig clientConfig) {

        this.observer = observer;
        this.clientConfig = clientConfig;
        eventLoop = clientConfig.acquireEventLoop();
    }

    @Override
    public void onSuccess(final @NotNull T t) {
        if (done.compareAndSet(false, true)) {
            observer.onSuccess(t);
            clientConfig.releaseEventLoop();
        }
    }

    @Override
    public void onError(final @NotNull Throwable t) {
        if (done.compareAndSet(false, true)) {
            observer.onError(t);
            clientConfig.releaseEventLoop();
        }
    }

    @Override
    public void dispose() {
        if (done.compareAndSet(false, true)) {
            clientConfig.releaseEventLoop();
        }
    }

    @Override
    public boolean isDisposed() {
        return done.get();
    }

    @Override
    public boolean isCancelled() {
        return done.get();
    }

    @NotNull EventLoop getEventLoop() {
        return eventLoop;
    }
}

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

package org.mqttbee.mqtt.handler.subscribe;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.rx.SingleFlow;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
public class MqttSubAckFlow implements SingleFlow<Mqtt5SubAck>, Disposable {

    private final SingleObserver<? super Mqtt5SubAck> observer;
    private final AtomicBoolean disposed = new AtomicBoolean();

    MqttSubAckFlow(@NotNull final SingleObserver<? super Mqtt5SubAck> observer) {
        this.observer = observer;
    }

    @Override
    public void onSuccess(final Mqtt5SubAck subAck) {
        observer.onSuccess(subAck);
    }

    @Override
    public void onError(final Throwable t) {
        observer.onError(t);
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

}

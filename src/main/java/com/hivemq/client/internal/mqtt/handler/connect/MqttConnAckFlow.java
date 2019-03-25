/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.connect;

import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckFlow implements Disposable {

    private final @NotNull SingleObserver<? super Mqtt5ConnAck> observer;
    private boolean error;
    private volatile boolean disposed;

    MqttConnAckFlow(final @NotNull SingleObserver<? super Mqtt5ConnAck> observer) {
        this.observer = observer;
    }

    public void onSuccess(final @NotNull Mqtt5ConnAck t) {
        observer.onSuccess(t);
    }

    public boolean onError(final @NotNull Throwable t) {
        if (error) {
            return false;
        }
        error = true;
        observer.onError(t);
        return true;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}

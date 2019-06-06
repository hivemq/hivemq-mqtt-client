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

import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImpl;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckFlow {

    private final @Nullable SingleObserver<? super Mqtt5ConnAck> observer;
    private final @NotNull Disposable disposable;
    private final @NotNull MqttClientTransportConfigImpl transportConfig;
    private final int attempts;
    private boolean done;

    MqttConnAckFlow(
            final @NotNull SingleObserver<? super Mqtt5ConnAck> observer,
            final @NotNull MqttClientTransportConfigImpl transportConfig) {

        this.observer = observer;
        disposable = new MqttConnAckDisposable();
        this.transportConfig = transportConfig;
        attempts = 0;
    }

    MqttConnAckFlow(
            final @Nullable MqttConnAckFlow oldFlow, final @NotNull MqttClientTransportConfigImpl transportConfig) {

        if (oldFlow == null) {
            observer = null;
            disposable = new MqttConnAckDisposable();
            attempts = 0;
        } else {
            observer = oldFlow.observer;
            disposable = oldFlow.disposable;
            attempts = oldFlow.attempts + 1;
        }
        this.transportConfig = transportConfig;
    }

    boolean setDone() {
        if (done) {
            return false;
        }
        done = true;
        return true;
    }

    void onSuccess(final @NotNull Mqtt5ConnAck t) {
        if (observer != null) {
            observer.onSuccess(t);
        }
    }

    void onError(final @NotNull Throwable t) {
        if (observer != null) {
            observer.onError(t);
        }
    }

    @NotNull Disposable getDisposable() {
        return disposable;
    }

    public @NotNull MqttClientTransportConfigImpl getTransportConfig() {
        return transportConfig;
    }

    int getAttempts() {
        return attempts;
    }

    private static class MqttConnAckDisposable implements Disposable {

        private volatile boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}

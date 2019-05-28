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
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckFlow implements Disposable {

    private final @Nullable SingleObserver<? super Mqtt5ConnAck> observer;
    private final @NotNull InetSocketAddress serverAddress;
    private final int attempts;
    private boolean done;
    private volatile boolean disposed;

    MqttConnAckFlow(
            final @NotNull SingleObserver<? super Mqtt5ConnAck> observer,
            final @NotNull InetSocketAddress serverAddress) {

        this.observer = observer;
        this.serverAddress = serverAddress;
        attempts = 0;
    }

    MqttConnAckFlow(final @Nullable MqttConnAckFlow oldFlow, final @NotNull InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
        if (oldFlow == null) {
            observer = null;
            attempts = 0;
        } else {
            observer = oldFlow.observer;
            attempts = oldFlow.attempts + 1;
        }
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

    @NotNull InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    int getAttempts() {
        return attempts;
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

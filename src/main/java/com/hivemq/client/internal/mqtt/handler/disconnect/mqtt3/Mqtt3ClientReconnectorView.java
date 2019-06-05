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

package com.hivemq.client.internal.mqtt.handler.disconnect.mqtt3;

import com.hivemq.client.internal.mqtt.handler.disconnect.MqttClientReconnector;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.mqtt.mqtt3.lifecycle.Mqtt3ClientReconnector;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientReconnectorView implements Mqtt3ClientReconnector {

    private final @NotNull MqttClientReconnector delegate;

    public Mqtt3ClientReconnectorView(final @NotNull MqttClientReconnector delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt3ClientReconnectorView reconnect(final boolean reconnect) {
        delegate.reconnect(reconnect);
        return this;
    }

    @Override
    public @NotNull <T> Mqtt3ClientReconnector reconnectWhen(
            final @NotNull CompletableFuture<T> future,
            final @Nullable BiConsumer<? super T, ? super Throwable> callback) {

        delegate.reconnectWhen(future, callback);
        return this;
    }

    @Override
    public boolean isReconnect() {
        return delegate.isReconnect();
    }

    @Override
    public int getAttempts() {
        return delegate.getAttempts();
    }

    @Override
    public @NotNull Mqtt3ClientReconnectorView delay(final long delay, final @NotNull TimeUnit timeUnit) {
        delegate.delay(delay, timeUnit);
        return this;
    }

    @Override
    public long getDelay(final @NotNull TimeUnit timeUnit) {
        return delegate.getDelay(timeUnit);
    }

    @Override
    public @NotNull Mqtt3ClientReconnectorView serverAddress(final @NotNull InetSocketAddress address) {
        delegate.serverAddress(address);
        return this;
    }

    @Override
    public @NotNull InetSocketAddress getServerAddress() {
        return delegate.getServerAddress();
    }

    @Override
    public @NotNull Mqtt3ClientReconnectorView connect(final @NotNull Mqtt3Connect connect) {
        delegate.connect(MqttChecks.connect(connect));
        return this;
    }

    @Override
    public @NotNull Mqtt3ConnectBuilder.Nested<? extends Mqtt3ClientReconnectorView> connectWith() {
        return new Mqtt3ConnectViewBuilder.Nested<>(getConnect(), this::connect);
    }

    @Override
    public @NotNull Mqtt3ConnectView getConnect() {
        return Mqtt3ConnectView.of(delegate.getConnect());
    }
}

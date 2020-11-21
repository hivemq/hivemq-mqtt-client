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

package com.hivemq.client.internal.mqtt.lifecycle.mqtt3;

import com.hivemq.client.internal.mqtt.MqttTransportConfigImpl;
import com.hivemq.client.internal.mqtt.MqttTransportConfigImplBuilder;
import com.hivemq.client.internal.mqtt.lifecycle.MqttReconnector;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.mqtt.MqttTransportConfig;
import com.hivemq.client.mqtt.mqtt3.lifecycle.Mqtt3Reconnector;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ReconnectorView implements Mqtt3Reconnector {

    private final @NotNull MqttReconnector delegate;

    Mqtt3ReconnectorView(final @NotNull MqttReconnector delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt3ReconnectorView reconnect(final boolean reconnect) {
        delegate.reconnect(reconnect);
        return this;
    }

    @Override
    public @NotNull <T> Mqtt3ReconnectorView reconnectWhen(
            final @Nullable CompletableFuture<T> future,
            final @Nullable BiConsumer<? super T, ? super Throwable> callback) {

        delegate.reconnectWhen(future, callback);
        return this;
    }

    @Override
    public boolean isReconnect() {
        return delegate.isReconnect();
    }

    @Override
    public @NotNull Mqtt3ReconnectorView resubscribeIfSessionExpired(final boolean resubscribe) {
        delegate.resubscribeIfSessionExpired(resubscribe);
        return this;
    }

    @Override
    public boolean isResubscribeIfSessionExpired() {
        return delegate.isResubscribeIfSessionExpired();
    }

    @Override
    public @NotNull Mqtt3ReconnectorView republishIfSessionExpired(final boolean republish) {
        delegate.republishIfSessionExpired(republish);
        return this;
    }

    @Override
    public boolean isRepublishIfSessionExpired() {
        return delegate.isRepublishIfSessionExpired();
    }

    @Override
    public int getAttempts() {
        return delegate.getAttempts();
    }

    @Override
    public @NotNull Mqtt3ReconnectorView delay(final long delay, final @Nullable TimeUnit timeUnit) {
        delegate.delay(delay, timeUnit);
        return this;
    }

    @Override
    public long getDelay(final @NotNull TimeUnit timeUnit) {
        return delegate.getDelay(timeUnit);
    }

    @Override
    public @NotNull Mqtt3ReconnectorView transportConfig(final @Nullable MqttTransportConfig transportConfig) {
        delegate.transportConfig(transportConfig);
        return this;
    }

    @Override
    public MqttTransportConfigImplBuilder.@NotNull Nested<Mqtt3ReconnectorView> transportConfigWith() {
        return new MqttTransportConfigImplBuilder.Nested<>(getTransportConfig(), this::transportConfig);
    }

    @Override
    public @NotNull MqttTransportConfigImpl getTransportConfig() {
        return delegate.getTransportConfig();
    }

    @Override
    public @NotNull Mqtt3ReconnectorView connect(final @Nullable Mqtt3Connect connect) {
        delegate.connect(MqttChecks.connect(connect));
        return this;
    }

    @Override
    public Mqtt3ConnectViewBuilder.@NotNull Nested<Mqtt3ReconnectorView> connectWith() {
        return new Mqtt3ConnectViewBuilder.Nested<>(getConnect(), this::connect);
    }

    @Override
    public @NotNull Mqtt3ConnectView getConnect() {
        return Mqtt3ConnectView.of(delegate.getConnect());
    }
}

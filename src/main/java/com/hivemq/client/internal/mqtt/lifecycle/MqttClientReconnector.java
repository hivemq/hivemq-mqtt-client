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

package com.hivemq.client.internal.mqtt.lifecycle;

import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImpl;
import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImplBuilder;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttClientTransportConfig;
import com.hivemq.client.mqtt.mqtt5.lifecycle.Mqtt5ClientReconnector;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 */
public class MqttClientReconnector implements Mqtt5ClientReconnector {

    private final @NotNull EventLoop eventLoop;
    private final int attempts;
    private boolean reconnect;
    private @Nullable CompletableFuture<?> future;
    private long delayNanos;
    private @NotNull MqttClientTransportConfigImpl transportConfig;
    private @NotNull MqttConnect connect;

    public MqttClientReconnector(
            final @NotNull EventLoop eventLoop, final int attempts, final @NotNull MqttConnect connect,
            final @NotNull MqttClientTransportConfigImpl transportConfig) {

        this.eventLoop = eventLoop;
        this.attempts = attempts;
        this.connect = connect;
        this.transportConfig = transportConfig;
    }

    @Override
    public int getAttempts() {
        checkThread();
        return attempts;
    }

    @Override
    public @NotNull MqttClientReconnector reconnect(final boolean reconnect) {
        checkThread();
        this.reconnect = reconnect;
        return this;
    }

    @Override
    public <T> @NotNull Mqtt5ClientReconnector reconnectWhen(
            @Nullable CompletableFuture<T> future, final @Nullable BiConsumer<? super T, ? super Throwable> callback) {

        checkThread();
        Checks.notNull(future, "Future");
        this.reconnect = true;
        if (callback != null) {
            future = future.whenCompleteAsync(callback, eventLoop);
        }
        if (this.future == null) {
            this.future = future;
        } else {
            this.future = CompletableFuture.allOf(this.future, future);
        }
        return this;
    }

    @Override
    public boolean isReconnect() {
        checkThread();
        return reconnect;
    }

    public @NotNull CompletableFuture<?> getFuture() {
        checkThread();
        return (future == null) ? CompletableFuture.completedFuture(null) : future;
    }

    @Override
    public @NotNull MqttClientReconnector delay(final long delay, final @Nullable TimeUnit timeUnit) {
        checkThread();
        Checks.notNull(timeUnit, "Time unit");
        this.delayNanos = timeUnit.toNanos(delay);
        return this;
    }

    @Override
    public long getDelay(final @NotNull TimeUnit timeUnit) {
        checkThread();
        Checks.notNull(timeUnit, "Time unit");
        return timeUnit.convert(delayNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public @NotNull MqttClientReconnector transportConfig(final @Nullable MqttClientTransportConfig transportConfig) {
        checkThread();
        this.transportConfig =
                Checks.notImplemented(transportConfig, MqttClientTransportConfigImpl.class, "Transport config");
        return this;
    }

    @Override
    public @NotNull MqttClientTransportConfigImplBuilder.Nested<MqttClientReconnector> transportConfig() {
        checkThread();
        return new MqttClientTransportConfigImplBuilder.Nested<>(transportConfig, this::transportConfig);
    }

    @Override
    public @NotNull MqttClientTransportConfigImpl getTransportConfig() {
        checkThread();
        return transportConfig;
    }

    @Override
    public @NotNull MqttClientReconnector connect(final @Nullable Mqtt5Connect connect) {
        checkThread();
        this.connect = MqttChecks.connect(connect);
        return this;
    }

    @Override
    public @NotNull MqttConnectBuilder.Nested<MqttClientReconnector> connectWith() {
        checkThread();
        return new MqttConnectBuilder.Nested<>(connect, this::connect);
    }

    @Override
    public @NotNull MqttConnect getConnect() {
        checkThread();
        return connect;
    }

    private void checkThread() {
        if (!eventLoop.inEventLoop()) {
            throw new IllegalStateException("MqttClientReconnector must be called from the eventLoop.");
        }
    }
}

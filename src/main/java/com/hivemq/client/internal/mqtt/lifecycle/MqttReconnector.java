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

package com.hivemq.client.internal.mqtt.lifecycle;

import com.hivemq.client.internal.mqtt.MqttTransportConfigImpl;
import com.hivemq.client.internal.mqtt.MqttTransportConfigImplBuilder;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttTransportConfig;
import com.hivemq.client.mqtt.mqtt5.lifecycle.Mqtt5Reconnector;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 */
public class MqttReconnector implements Mqtt5Reconnector {

    private final @NotNull EventLoop eventLoop;
    private final @Range(from = 0, to = Integer.MAX_VALUE) int attempts;
    private boolean reconnect = DEFAULT_RECONNECT;
    private @Nullable CompletableFuture<?> future;
    private boolean resubscribeIfSessionExpired = DEFAULT_RESUBSCRIBE_IF_SESSION_EXPIRED;
    private boolean republishIfSessionExpired = DEFAULT_REPUBLISH_IF_SESSION_EXPIRED;
    private long delayNanos = TimeUnit.MILLISECONDS.toNanos(DEFAULT_DELAY_MS);
    private @NotNull MqttTransportConfigImpl transportConfig;
    private @NotNull MqttConnect connect;

    private boolean afterOnDisconnected;

    public MqttReconnector(
            final @NotNull EventLoop eventLoop,
            final @Range(from = 0, to = Integer.MAX_VALUE) int attempts,
            final @NotNull MqttConnect connect,
            final @NotNull MqttTransportConfigImpl transportConfig) {

        this.eventLoop = eventLoop;
        this.attempts = attempts;
        this.connect = connect;
        this.transportConfig = transportConfig;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getAttempts() {
        checkInEventLoop();
        return attempts;
    }

    @Override
    public @NotNull MqttReconnector reconnect(final boolean reconnect) {
        checkInEventLoop();
        this.reconnect = reconnect;
        return this;
    }

    @Override
    public <T> @NotNull MqttReconnector reconnectWhen(
            @Nullable CompletableFuture<T> future, final @Nullable BiConsumer<? super T, ? super Throwable> callback) {

        checkInOnDisconnected("reconnectWhen");
        Checks.notNull(future, "Future");
        this.reconnect = true;
        if (callback != null) {
            future = future.whenCompleteAsync(callback, eventLoop);
        }
        this.future = (this.future == null) ? future : CompletableFuture.allOf(this.future, future);
        return this;
    }

    @Override
    public boolean isReconnect() {
        checkInEventLoop();
        return reconnect;
    }

    public @NotNull CompletableFuture<?> getFuture() {
        checkInEventLoop();
        return (future == null) ? CompletableFuture.completedFuture(null) : future;
    }

    @Override
    public @NotNull MqttReconnector resubscribeIfSessionExpired(final boolean resubscribe) {
        checkInOnDisconnected("resubscribeIfSessionExpired");
        resubscribeIfSessionExpired = resubscribe;
        return this;
    }

    @Override
    public boolean isResubscribeIfSessionExpired() {
        checkInEventLoop();
        return resubscribeIfSessionExpired;
    }

    @Override
    public @NotNull MqttReconnector republishIfSessionExpired(final boolean republish) {
        checkInOnDisconnected("republishIfSessionExpired");
        republishIfSessionExpired = republish;
        return this;
    }

    @Override
    public boolean isRepublishIfSessionExpired() {
        checkInEventLoop();
        return republishIfSessionExpired;
    }

    @Override
    public @NotNull MqttReconnector delay(final long delay, final @Nullable TimeUnit timeUnit) {
        checkInOnDisconnected("delay");
        Checks.notNull(timeUnit, "Time unit");
        this.delayNanos = timeUnit.toNanos(delay);
        return this;
    }

    @Override
    public long getDelay(final @NotNull TimeUnit timeUnit) {
        checkInEventLoop();
        Checks.notNull(timeUnit, "Time unit");
        return timeUnit.convert(delayNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public @NotNull MqttReconnector transportConfig(final @Nullable MqttTransportConfig transportConfig) {
        checkInEventLoop();
        this.transportConfig =
                Checks.notImplemented(transportConfig, MqttTransportConfigImpl.class, "Transport config");
        return this;
    }

    @Override
    public MqttTransportConfigImplBuilder.@NotNull Nested<MqttReconnector> transportConfig() {
        checkInEventLoop();
        return new MqttTransportConfigImplBuilder.Nested<>(transportConfig, this::transportConfig);
    }

    @Override
    public @NotNull MqttTransportConfigImpl getTransportConfig() {
        checkInEventLoop();
        return transportConfig;
    }

    @Override
    public @NotNull MqttReconnector connect(final @Nullable Mqtt5Connect connect) {
        checkInEventLoop();
        this.connect = MqttChecks.connect(connect);
        return this;
    }

    @Override
    public MqttConnectBuilder.@NotNull Nested<MqttReconnector> connectWith() {
        checkInEventLoop();
        return new MqttConnectBuilder.Nested<>(connect, this::connect);
    }

    @Override
    public @NotNull MqttConnect getConnect() {
        checkInEventLoop();
        return connect;
    }

    public void afterOnDisconnected() {
        afterOnDisconnected = true;
    }

    private void checkInEventLoop() {
        Checks.state(eventLoop.inEventLoop(), "MqttReconnector must be called from the eventLoop.");
    }

    private void checkInOnDisconnected(final @NotNull String method) {
        checkInEventLoop();
        if (afterOnDisconnected) {
            throw new UnsupportedOperationException(method + " must only be called in onDisconnected.");
        }
    }
}

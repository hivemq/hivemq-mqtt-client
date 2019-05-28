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

package com.hivemq.client.internal.mqtt.handler.disconnect;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.handler.disconnect.mqtt3.Mqtt3ClientReconnectorView;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.lifecycle.Mqtt5ClientReconnector;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 */
public class MqttClientReconnector implements Mqtt5ClientReconnector {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull EventLoop eventLoop;
    private final int attempts;
    private boolean reconnect;
    private @Nullable CompletableFuture<?> future;
    private long delayNanos;
    private @NotNull InetSocketAddress serverAddress;
    private @NotNull MqttConnect connect;

    public MqttClientReconnector(
            final @NotNull MqttClientConfig clientConfig, final @NotNull EventLoop eventLoop, final int attempts,
            final @NotNull MqttConnect connect, final @NotNull InetSocketAddress serverAddress) {

        this.clientConfig = clientConfig;
        this.eventLoop = eventLoop;
        this.attempts = attempts;
        this.connect = connect;
        this.serverAddress = serverAddress;
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
            @NotNull CompletableFuture<T> future, final @Nullable BiConsumer<? super T, ? super Throwable> callback) {

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
    public @NotNull MqttClientReconnector delay(final long delay, final @NotNull TimeUnit timeUnit) {
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
    public @NotNull MqttClientReconnector serverAddress(final @NotNull InetSocketAddress address) {
        checkThread();
        this.serverAddress = Checks.notNull(address, "Server address");
        return this;
    }

    @Override
    public @NotNull InetSocketAddress getServerAddress() {
        checkThread();
        return serverAddress;
    }

    @Override
    public @NotNull MqttClientReconnector connect(final @NotNull Mqtt5Connect connect) {
        checkThread();
        this.connect = MqttChecks.connect(connect);
        return this;
    }

    @Override
    public @NotNull Mqtt5ConnectBuilder.Nested<MqttClientReconnector> connectWith() {
        checkThread();
        return new MqttConnectBuilder.Nested<>(connect, this::connect);
    }

    @Override
    public @NotNull MqttConnect getConnect() {
        checkThread();
        return connect;
    }

    @NotNull com.hivemq.client.mqtt.lifecycle.MqttClientReconnector toVersionSpecific() {
        if (clientConfig.getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            return new Mqtt3ClientReconnectorView(this);
        }
        return this;
    }

    private void checkThread() {
        if (!eventLoop.inEventLoop()) {
            throw new IllegalStateException("MqttClientReconnector must be called from the eventLoop.");
        }
    }
}

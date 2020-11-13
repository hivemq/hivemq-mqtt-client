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

package com.hivemq.client.mqtt.mqtt3.lifecycle;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttTransportConfig;
import com.hivemq.client.mqtt.MqttTransportConfigBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttReconnector;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A {@link MqttReconnector} with methods specific to an {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface Mqtt3Reconnector extends MqttReconnector {

    @Override
    @NotNull Mqtt3Reconnector reconnect(boolean reconnect);

    @Override
    <T> @NotNull Mqtt3Reconnector reconnectWhen(
            @NotNull CompletableFuture<T> future, @Nullable BiConsumer<? super T, ? super Throwable> callback);

    @Override
    @NotNull Mqtt3Reconnector resubscribeIfSessionExpired(boolean resubscribe);

    @Override
    @NotNull Mqtt3Reconnector republishIfSessionExpired(boolean republish);

    @Override
    @NotNull Mqtt3Reconnector delay(long delay, @NotNull TimeUnit timeUnit);

    @Override
    @NotNull Mqtt3Reconnector transportConfig(@NotNull MqttTransportConfig transportConfig);

    @Override
    @CheckReturnValue
    MqttTransportConfigBuilder.@NotNull Nested<? extends Mqtt3Reconnector> transportConfig();

    /**
     * Sets a different Connect message the client will try to reconnect with.
     *
     * @param connect the Connect message.
     * @return this reconnector.
     */
    @NotNull Mqtt3Reconnector connect(@NotNull Mqtt3Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt3Connect)}.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder.Nested#applyConnect()} on the returned builder has the same effect as calling
     * {@link #connect(Mqtt3Connect)} with the result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    @CheckReturnValue
    Mqtt3ConnectBuilder.@NotNull Nested<? extends Mqtt3Reconnector> connectWith();

    /**
     * Returns the currently set Connect message the client will try to reconnect with.@
     * <p>
     * If the {@link #connect(Mqtt3Connect)} method has not been called before (including previous {@link
     * com.hivemq.client.mqtt.lifecycle.MqttDisconnectedListener MqttDisconnectedListener}s) it will be the Connect
     * message that is reconstructed from the {@link com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConnectionConfig
     * Mqtt3ClientConnectionConfig} or the Connect message of the previous connect try if it has not been successfully
     * connected.
     *
     * @return the Connect message.
     */
    @NotNull Mqtt3Connect getConnect();
}

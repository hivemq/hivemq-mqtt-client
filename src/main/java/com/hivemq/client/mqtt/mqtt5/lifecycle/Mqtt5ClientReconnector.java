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

package com.hivemq.client.mqtt.mqtt5.lifecycle;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttTransportConfig;
import com.hivemq.client.mqtt.MqttTransportConfigBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttClientReconnector;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A {@link MqttClientReconnector} with methods specific to an {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client
 * Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface Mqtt5ClientReconnector extends MqttClientReconnector {

    @Override
    @NotNull Mqtt5ClientReconnector reconnect(boolean reconnect);

    @Override
    <T> @NotNull Mqtt5ClientReconnector reconnectWhen(
            @NotNull CompletableFuture<T> future, @Nullable BiConsumer<? super T, ? super Throwable> callback);

    @Override
    @NotNull Mqtt5ClientReconnector resubscribeIfSessionExpired(boolean resubscribe);

    @Override
    @NotNull Mqtt5ClientReconnector republishIfSessionExpired(boolean republish);

    @Override
    @NotNull Mqtt5ClientReconnector delay(long delay, @NotNull TimeUnit timeUnit);

    @Override
    @NotNull Mqtt5ClientReconnector transportConfig(@NotNull MqttTransportConfig transportConfig);

    @Override
    @CheckReturnValue
    MqttTransportConfigBuilder.@NotNull Nested<? extends Mqtt5ClientReconnector> transportConfig();

    /**
     * Sets a different Connect message the client will try to reconnect with.
     *
     * @param connect the Connect message.
     * @return this reconnector.
     */
    @NotNull Mqtt5ClientReconnector connect(@NotNull Mqtt5Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt5Connect)}.
     * <p>
     * Calling {@link Mqtt5ConnectBuilder.Nested#applyConnect()} on the returned builder has the same effect as calling
     * {@link #connect(Mqtt5Connect)} with the result of {@link Mqtt5ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt5Connect)
     */
    @CheckReturnValue
    Mqtt5ConnectBuilder.@NotNull Nested<? extends Mqtt5ClientReconnector> connectWith();

    /**
     * Returns the currently set Connect message the client will try to reconnect with.
     * <p>
     * If the {@link #connect(Mqtt5Connect)} method has not been called before (including previous {@link
     * com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener MqttClientDisconnectedListeners}) it will be the
     * Connect message that is reconstructed from the {@link com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig
     * Mqtt5ClientConnectionConfig} or the Connect message of the previous connect try if it has not been successfully
     * connected.
     *
     * @return the Connect message.
     */
    @NotNull Mqtt5Connect getConnect();
}

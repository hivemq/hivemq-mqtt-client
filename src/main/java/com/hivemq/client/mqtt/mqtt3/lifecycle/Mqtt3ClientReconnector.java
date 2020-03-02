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

package com.hivemq.client.mqtt.mqtt3.lifecycle;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttClientTransportConfig;
import com.hivemq.client.mqtt.MqttClientTransportConfigBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttClientReconnector;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A {@link MqttClientReconnector} with methods specific to a {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client
 * Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface Mqtt3ClientReconnector extends MqttClientReconnector {

    @Override
    @NotNull Mqtt3ClientReconnector reconnect(boolean reconnect);

    @Override
    <T> @NotNull Mqtt3ClientReconnector reconnectWhen(
            @NotNull CompletableFuture<T> future, @Nullable BiConsumer<? super T, ? super Throwable> callback);

    @Override
    @NotNull Mqtt3ClientReconnector delay(long delay, @NotNull TimeUnit timeUnit);

    @Override
    @NotNull Mqtt3ClientReconnector transportConfig(@NotNull MqttClientTransportConfig transportConfig);

    @Override
    @NotNull MqttClientTransportConfigBuilder.Nested<? extends Mqtt3ClientReconnector> transportConfig();

    /**
     * Sets a different Connect message the client will try to reconnect with.
     *
     * @param connect the Connect message.
     * @return this reconnector.
     */
    @NotNull Mqtt3ClientReconnector connect(@NotNull Mqtt3Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt3Connect)}.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder.Nested#applyConnect()} on the returned builder has the same effect as calling
     * {@link #connect(Mqtt3Connect)} with the result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    @NotNull Mqtt3ConnectBuilder.Nested<? extends Mqtt3ClientReconnector> connectWith();

    /**
     * Returns the currently set Connect message the client will try to reconnect with.
     * <p>
     * If the {@link #connect(Mqtt3Connect)} method has not been called before (including previous {@link
     * com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener MqttClientDisconnectedListeners}) it will be the
     * Connect message that is reconstructed from the {@link com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConnectionConfig
     * Mqtt3ClientConnectionConfig} or the Connect message of the previous connect try if it has not been successfully
     * connected.
     *
     * @return the Connect message.
     */
    @NotNull Mqtt3Connect getConnect();
}

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

package com.hivemq.client.mqtt.lifecycle;

import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientReconnector {

    int getAttempts();

    @NotNull MqttClientReconnector reconnect(boolean reconnect);

    <T> @NotNull MqttClientReconnector reconnectWhen(
            @NotNull CompletableFuture<T> future, @NotNull BiConsumer<? super T, ? super Throwable> consumer);

    boolean isReconnect();

    @NotNull MqttClientReconnector delay(long delay, @NotNull TimeUnit timeUnit);

    long getDelay(@NotNull TimeUnit timeUnit);

    @NotNull MqttClientReconnector serverAddress(@NotNull InetSocketAddress address);

    @NotNull InetSocketAddress getServerAddress();
}

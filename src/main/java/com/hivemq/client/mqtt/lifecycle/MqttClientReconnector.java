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
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A reconnector is supplied by a {@link MqttClientDisconnectedListener.Context} and can be used for reconnecting.
 * <p>
 * The client will reconnect only if at least one of the methods {@link #reconnect(boolean)} or {@link
 * #reconnectWhen(CompletableFuture, BiConsumer)} are called.
 * <p>
 * All methods must only be called in {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedListener.Context)}
 * or in the callback of the {@link #reconnectWhen(CompletableFuture, BiConsumer)} method.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientReconnector {

    /**
     * @return the number of failed connection attempts.
     */
    int getAttempts();

    /**
     * Instructs the client to reconnect or not.
     *
     * @param reconnect whether to reconnect or not.
     * @return this reconnector.
     */
    @NotNull MqttClientReconnector reconnect(boolean reconnect);

    /**
     * Instructs the client to reconnect after a future completes.
     * <p>
     * If also a {@link #delay(long, TimeUnit) delay} is supplied, the client will reconnect after both are complete.
     *
     * @param future   the client will reconnect only after the future completes.
     * @param callback the callback that will be called after the future completes and before the client will reconnect.
     *                 It can be used to set new connect properties (e.g. credentials).
     * @param <T>      the result type of the future.
     * @return this reconnector.
     */
    <T> @NotNull MqttClientReconnector reconnectWhen(
            @NotNull CompletableFuture<T> future, @Nullable BiConsumer<? super T, ? super Throwable> callback);

    /**
     * @return whether the client will reconnect or not.
     */
    boolean isReconnect();

    /**
     * Sets a delay which the client will wait before trying to reconnect.
     * <p>
     * The client will reconnect after the delay only if at least one of the methods {@link #reconnect(boolean)} or
     * {@link #reconnectWhen(CompletableFuture, BiConsumer)} are called.
     * <p>
     * If also a {@link #reconnectWhen(CompletableFuture, BiConsumer) future} is supplied, the client will reconnect
     * after both are complete.
     *
     * @param delay    delay which the client will wait before trying to reconnect.
     * @param timeUnit the time unit of the delay.
     * @return this reconnector.
     */
    @NotNull MqttClientReconnector delay(long delay, @NotNull TimeUnit timeUnit);

    /**
     * Returns the currently set delay which the client will wait before trying to reconnect.
     * <p>
     * If the {@link #delay(long, TimeUnit)} method has not been called before (including previous {@link
     * MqttClientDisconnectedListener MqttClientDisconnectedListeners}) it will be <code>0</code>.
     *
     * @param timeUnit the time unit of the returned delay.
     * @return the delay in the given time unit.
     */
    long getDelay(@NotNull TimeUnit timeUnit);

    /**
     * Sets a different server address the client will try to reconnect to.
     *
     * @param address the server address the client will try to reconnect to.
     * @return this reconnector.
     */
    @NotNull MqttClientReconnector serverAddress(@NotNull InetSocketAddress address);

    /**
     * Returns the currently set server address the client will try to reconnect to.
     * <p>
     * If the {@link #serverAddress(InetSocketAddress)} method has not been called before (including previous {@link
     * MqttClientDisconnectedListener MqttClientDisconnectedListeners}) it will be the server address the client was
     * connected to or the {@link com.hivemq.client.mqtt.MqttClientConfig#getServerAddress() default server address} if
     * it has not been connected yet.
     *
     * @return the server address.
     */
    @NotNull InetSocketAddress getServerAddress();
}

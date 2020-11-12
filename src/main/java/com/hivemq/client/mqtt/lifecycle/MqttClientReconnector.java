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

package com.hivemq.client.mqtt.lifecycle;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttTransportConfig;
import com.hivemq.client.mqtt.MqttTransportConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A reconnector is supplied by a {@link MqttClientDisconnectedContext} and can be used for reconnecting.
 * <p>
 * The client will reconnect only if at least one of the methods {@link #reconnect(boolean)} or {@link
 * #reconnectWhen(CompletableFuture, BiConsumer)} is called.
 * <p>
 * All methods must only be called in {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}.
 * Some methods can also be called in the callback supplied to {@link #reconnectWhen(CompletableFuture, BiConsumer)}.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
@DoNotImplement
public interface MqttClientReconnector {

    /**
     * If reconnect is enabled by default.
     *
     * @since 1.2
     */
    boolean DEFAULT_RECONNECT = false;
    /**
     * If resubscribe when the session expired before the client reconnected successfully is enabled by default.
     *
     * @since 1.2
     */
    boolean DEFAULT_RESUBSCRIBE_IF_SESSION_EXPIRED = true;
    /**
     * If republish when the session expired before the client reconnected successfully is enabled by default.
     *
     * @since 1.2
     */
    boolean DEFAULT_REPUBLISH_IF_SESSION_EXPIRED = false;
    /**
     * Default delay in milliseconds the client will wait for before trying to reconnect.
     *
     * @since 1.2
     */
    long DEFAULT_DELAY_MS = 0;

    /**
     * @return the number of failed connection attempts.
     */
    int getAttempts();

    /**
     * Instructs the client to reconnect or not.
     *
     * @param reconnect whether to reconnect.
     * @return this reconnector.
     */
    @NotNull MqttClientReconnector reconnect(boolean reconnect);

    /**
     * Instructs the client to reconnect after a future completes.
     * <p>
     * If additionally a {@link #delay(long, TimeUnit) delay} is supplied, the client will reconnect after both are
     * complete.
     * <p>
     * This method must only be called in {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}
     * and not in the supplied callback.
     *
     * @param future   the client will reconnect only after the future completes.
     * @param callback the callback that will be called after the future completes and before the client will reconnect.
     *                 It can be used to set new connect properties (e.g. credentials).
     * @param <T>      the result type of the future.
     * @return this reconnector.
     * @throws UnsupportedOperationException if called outside of {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}.
     */
    <T> @NotNull MqttClientReconnector reconnectWhen(
            @NotNull CompletableFuture<T> future, @Nullable BiConsumer<? super T, ? super Throwable> callback);

    /**
     * @return whether the client will reconnect.
     */
    boolean isReconnect();

    /**
     * Instructs the client to automatically restore its subscriptions when the session expired before it reconnected
     * successfully.
     * <p>
     * When the client reconnected successfully and its session is still present, the server still knows its
     * subscriptions and they do not need to be restored.
     * <p>
     * This setting only has effect if the client will reconnect (at least one of the methods {@link
     * #reconnect(boolean)} or {@link #reconnectWhen(CompletableFuture, BiConsumer)} is called).
     * <p>
     * This method must only be called in {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}
     * and not in the callback supplied to {@link #reconnectWhen(CompletableFuture, BiConsumer)}.
     *
     * @param resubscribe whether to resubscribe when the session expired before the client reconnected successfully.
     * @return this reconnector.
     * @throws UnsupportedOperationException if called outside of {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}.
     * @since 1.2
     */
    @NotNull MqttClientReconnector resubscribeIfSessionExpired(boolean resubscribe);

    /**
     * @return whether the client will resubscribe when the session expired before it reconnected successfully.
     * @since 1.2
     */
    boolean isResubscribeIfSessionExpired();

    /**
     * Instructs the client to queue pending Publish messages and automatically publish them even if the session expired
     * before reconnected successfully.
     * <p>
     * When the client reconnected successfully and its session is still present, the client will always queue pending
     * Publish messages and automatically publish them to ensure the QoS guarantees.
     * <p>
     * This setting only has effect if the client will reconnect (at least one of the methods {@link
     * #reconnect(boolean)} or {@link #reconnectWhen(CompletableFuture, BiConsumer)} is called).
     * <p>
     * This method must only be called in {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}
     * and not in the callback supplied to {@link #reconnectWhen(CompletableFuture, BiConsumer)}.
     *
     * @param republish whether to republish when the session expired before the client reconnected successfully.
     * @return this reconnector.
     * @throws UnsupportedOperationException if called outside of {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}.
     * @since 1.2
     */
    @NotNull MqttClientReconnector republishIfSessionExpired(boolean republish);

    /**
     * @return whether the client will republish when the session expired before it reconnected successfully.
     * @since 1.2
     */
    boolean isRepublishIfSessionExpired();

    /**
     * Sets a delay the client will wait for before trying to reconnect.
     * <p>
     * This setting only has effect if the client will reconnect (at least one of the methods {@link
     * #reconnect(boolean)} or {@link #reconnectWhen(CompletableFuture, BiConsumer)} is called).
     * <p>
     * If additionally a {@link #reconnectWhen(CompletableFuture, BiConsumer) future} is supplied, the client will
     * reconnect after both are complete.
     * <p>
     * This method must only be called in {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}
     * and not in the callback supplied to {@link #reconnectWhen(CompletableFuture, BiConsumer)}.
     *
     * @param delay    delay which the client will wait before trying to reconnect.
     * @param timeUnit the time unit of the delay.
     * @return this reconnector.
     * @throws UnsupportedOperationException if called outside of {@link MqttClientDisconnectedListener#onDisconnected(MqttClientDisconnectedContext)}.
     */
    @NotNull MqttClientReconnector delay(long delay, @NotNull TimeUnit timeUnit);

    /**
     * Returns the currently set delay the client will wait for before trying to reconnect.
     * <p>
     * If the {@link #delay(long, TimeUnit)} method has not been called before (including previous {@link
     * MqttClientDisconnectedListener MqttClientDisconnectedListeners}) it will be {@link #DEFAULT_DELAY_MS}.
     *
     * @param timeUnit the time unit of the returned delay.
     * @return the delay in the given time unit.
     */
    long getDelay(@NotNull TimeUnit timeUnit);

    /**
     * Sets a different transport configuration the client will try to reconnect with.
     *
     * @param transportConfig the transport configuration the client will try to reconnect with.
     * @return this reconnector.
     */
    @NotNull MqttClientReconnector transportConfig(@NotNull MqttTransportConfig transportConfig);

    /**
     * Fluent counterpart of {@link #transportConfig(MqttTransportConfig)}.
     * <p>
     * Calling {@link MqttTransportConfigBuilder.Nested#applyTransportConfig()} on the returned builder has the effect
     * of extending the current transport configuration.
     *
     * @return the fluent builder for the transport configuration.
     * @see #transportConfig(MqttTransportConfig)
     */
    @CheckReturnValue
    MqttTransportConfigBuilder.@NotNull Nested<? extends MqttClientReconnector> transportConfig();

    /**
     * Returns the currently set transport configuration the client will try to reconnect with.
     * <p>
     * If the {@link #transportConfig(MqttTransportConfig)} method has not been called before (including previous {@link
     * MqttClientDisconnectedListener MqttClientDisconnectedListeners}) it will be the transport configuration the
     * client was connected with or the {@link com.hivemq.client.mqtt.MqttClientConfig#getTransportConfig() default
     * transport configuration} if it has not been connected yet.
     *
     * @return the transport configuration.
     */
    @NotNull MqttTransportConfig getTransportConfig();
}

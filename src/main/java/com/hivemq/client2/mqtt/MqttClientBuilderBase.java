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

package com.hivemq.client2.mqtt;

import com.hivemq.client2.annotations.CheckReturnValue;
import com.hivemq.client2.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client2.mqtt.lifecycle.MqttAutoReconnect;
import com.hivemq.client2.mqtt.lifecycle.MqttAutoReconnectBuilder;
import com.hivemq.client2.mqtt.lifecycle.MqttConnectedListener;
import com.hivemq.client2.mqtt.lifecycle.MqttDisconnectedListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder base for an {@link MqttClient}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttClientBuilderBase<B extends MqttClientBuilderBase<B>> extends MqttTransportConfigBuilderBase<B> {

    /**
     * Sets the {@link MqttClientConfig#getIdentifier() Client Identifier}.
     *
     * @param identifier the string representation of the Client Identifier.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B identifier(@NotNull String identifier);

    /**
     * Sets the {@link MqttClientConfig#getIdentifier() Client Identifier}.
     *
     * @param identifier the Client Identifier.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B identifier(@NotNull MqttClientIdentifier identifier);

    /**
     * Sets the {@link MqttClientConfig#getTransportConfig() transport configuration}.
     *
     * @param transportConfig the transport configuration.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B transportConfig(@NotNull MqttTransportConfig transportConfig);

    /**
     * Fluent counterpart of {@link #transportConfig(MqttTransportConfig)}.
     * <p>
     * Calling {@link MqttTransportConfigBuilder.Nested#applyTransportConfig()} on the returned builder has the effect
     * of extending the current transport configuration.
     *
     * @return the fluent builder for the transport configuration.
     * @see #transportConfig(MqttTransportConfig)
     * @since 1.1
     */
    @CheckReturnValue
    MqttTransportConfigBuilder.@NotNull Nested<? extends B> transportConfigWith();

    /**
     * Sets the {@link MqttClientConfig#getExecutorConfig() executor configuration}.
     *
     * @param executorConfig the executor configuration.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B executorConfig(@NotNull MqttExecutorConfig executorConfig);

    /**
     * Fluent counterpart of {@link #executorConfig(MqttExecutorConfig)}.
     * <p>
     * Calling {@link MqttExecutorConfigBuilder.Nested#applyExecutorConfig()} on the returned builder has the effect of
     * extending the current executor configuration.
     *
     * @return the fluent builder for the executor configuration.
     * @see #executorConfig(MqttExecutorConfig)
     */
    @CheckReturnValue
    MqttExecutorConfigBuilder.@NotNull Nested<? extends B> executorConfigWith();

    /**
     * Uses automatic reconnect with the default configuration.
     *
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B automaticReconnect();

    /**
     * Sets the optional {@link MqttClientConfig#getAutomaticReconnect() automatic reconnect strategy}.
     *
     * @param autoReconnect the automatic reconnect strategy or <code>null</code> to remove any previously set automatic
     *                      reconnect strategy.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B automaticReconnect(@Nullable MqttAutoReconnect autoReconnect);

    /**
     * Fluent counterpart of {@link #automaticReconnect(MqttAutoReconnect)}.
     * <p>
     * Calling {@link MqttAutoReconnectBuilder.Nested#applyAutomaticReconnect()} on the returned builder has the effect
     * of extending the current automatic reconnect strategy.
     *
     * @return the fluent builder for the automatic reconnect strategy.
     * @see #automaticReconnect(MqttAutoReconnect)
     * @since 1.1
     */
    @CheckReturnValue
    MqttAutoReconnectBuilder.@NotNull Nested<? extends B> automaticReconnectWith();

    /**
     * Adds a listener which is notified when the client is connected (a successful ConnAck message is received).
     * <p>
     * The listeners are called in the same order in which they are added.
     *
     * @param connectedListener the listener to add.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B addConnectedListener(@NotNull MqttConnectedListener connectedListener);

    /**
     * Adds a listener which is notified when the client is disconnected (with or without a Disconnect message) or the
     * connection fails.
     * <p>
     * The listeners are called in the same order in which they are added.
     *
     * @param disconnectedListener the listener to add.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull B addDisconnectedListener(@NotNull MqttDisconnectedListener disconnectedListener);
}

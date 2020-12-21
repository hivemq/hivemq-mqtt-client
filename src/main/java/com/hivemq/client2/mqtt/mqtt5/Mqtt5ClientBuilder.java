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

package com.hivemq.client2.mqtt.mqtt5;

import com.hivemq.client2.annotations.CheckReturnValue;
import com.hivemq.client2.mqtt.MqttClientBuilderBase;
import com.hivemq.client2.mqtt.mqtt5.advanced.Mqtt5AdvancedConfig;
import com.hivemq.client2.mqtt.mqtt5.advanced.Mqtt5AdvancedConfigBuilder;
import com.hivemq.client2.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client2.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder for an {@link Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ClientBuilder extends MqttClientBuilderBase<Mqtt5ClientBuilder> {

    /**
     * Sets the {@link Mqtt5ClientConfig#getAdvancedConfig() advanced configuration}.
     *
     * @param advancedConfig the advanced configuration.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull Mqtt5ClientBuilder advancedConfig(@NotNull Mqtt5AdvancedConfig advancedConfig);

    /**
     * Fluent counterpart of {@link #advancedConfig(Mqtt5AdvancedConfig)}.
     * <p>
     * Calling {@link Mqtt5AdvancedConfigBuilder.Nested#applyAdvancedConfig()} on the returned builder has the effect of
     * extending the current advanced configuration.
     *
     * @return the fluent builder for the advanced configuration.
     * @see #advancedConfig(Mqtt5AdvancedConfig)
     */
    @CheckReturnValue
    Mqtt5AdvancedConfigBuilder.@NotNull Nested<? extends Mqtt5ClientBuilder> advancedConfigWith();

    /**
     * Sets the optional {@link Mqtt5ClientConfig#getSimpleAuth() simple authentication and/or authorization related
     * data}.
     *
     * @param simpleAuth the simple auth related data or <code>null</code> to remove any previously set simple auth
     *                   related data.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull Mqtt5ClientBuilder simpleAuth(@Nullable Mqtt5SimpleAuth simpleAuth);

    /**
     * Fluent counterpart of {@link #simpleAuth(Mqtt5SimpleAuth)}.
     * <p>
     * Calling {@link Mqtt5SimpleAuthBuilder.Nested.Complete#applySimpleAuth()} on the returned builder has the same
     * effect as calling {@link #simpleAuth(Mqtt5SimpleAuth)} with the result of {@link
     * Mqtt5SimpleAuthBuilder.Complete#build()}.
     *
     * @return the fluent builder for the simple auth related data.
     * @see #simpleAuth(Mqtt5SimpleAuth)
     * @since 1.1
     */
    @CheckReturnValue
    Mqtt5SimpleAuthBuilder.@NotNull Nested<? extends Mqtt5ClientBuilder> simpleAuthWith();

    /**
     * Sets the {@link Mqtt5ClientConfig#getEnhancedAuthMechanism() enhanced authentication and/or authorization
     * mechanism}.
     *
     * @param enhancedAuthMechanism the enhanced auth mechanism or <code>null</code> to remove any previously set
     *                              enhanced auth mechanism.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull Mqtt5ClientBuilder enhancedAuth(@Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism);

    /**
     * Sets the optional {@link Mqtt5ClientConfig#getWillPublish() Will Publish}.
     *
     * @param willPublish the Will Publish or <code>null</code> to remove any previously set Will Publish.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull Mqtt5ClientBuilder willPublish(@Nullable Mqtt5Publish willPublish);

    /**
     * Fluent counterpart of {@link #willPublish(Mqtt5Publish)}.
     * <p>
     * Calling {@link Mqtt5WillPublishBuilder.Nested.Complete#applyWillPublish()} on the returned builder has the same
     * effect as calling {@link #willPublish(Mqtt5Publish)} with the result of {@link
     * Mqtt5WillPublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Will Publish.
     * @see #willPublish(Mqtt5Publish)
     * @since 1.1
     */
    @CheckReturnValue
    Mqtt5WillPublishBuilder.@NotNull Nested<? extends Mqtt5ClientBuilder> willPublishWith();

    /**
     * Builds the {@link Mqtt5Client}.
     *
     * @return the built {@link Mqtt5Client}.
     */
    @CheckReturnValue
    @NotNull Mqtt5Client build();

    /**
     * Builds the {@link Mqtt5RxClient}.
     *
     * @return the built {@link Mqtt5RxClient}.
     */
    @CheckReturnValue
    @NotNull Mqtt5RxClient buildRx();

    /**
     * Builds the {@link Mqtt5AsyncClient}.
     *
     * @return the built {@link Mqtt5AsyncClient}.
     */
    @CheckReturnValue
    @NotNull Mqtt5AsyncClient buildAsync();

    /**
     * Builds the {@link Mqtt5BlockingClient}.
     *
     * @return the built {@link Mqtt5BlockingClient}.
     */
    @CheckReturnValue
    @NotNull Mqtt5BlockingClient buildBlocking();
}

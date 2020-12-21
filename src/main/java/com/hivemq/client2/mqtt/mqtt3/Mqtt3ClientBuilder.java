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

package com.hivemq.client2.mqtt.mqtt3;

import com.hivemq.client2.annotations.CheckReturnValue;
import com.hivemq.client2.mqtt.MqttClientBuilderBase;
import com.hivemq.client2.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client2.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder for an {@link Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3ClientBuilder extends MqttClientBuilderBase<Mqtt3ClientBuilder> {

    /**
     * Sets the optional {@link Mqtt3ClientConfig#getSimpleAuth() simple authentication and/or authorization related
     * data}.
     *
     * @param simpleAuth the simple auth related data or <code>null</code> to remove any previously set simple auth
     *                   related data.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull Mqtt3ClientBuilder simpleAuth(@Nullable Mqtt3SimpleAuth simpleAuth);

    /**
     * Fluent counterpart of {@link #simpleAuth(Mqtt3SimpleAuth)}.
     * <p>
     * Calling {@link Mqtt3SimpleAuthBuilder.Nested.Complete#applySimpleAuth()} on the returned builder has the same
     * effect as calling {@link #simpleAuth(Mqtt3SimpleAuth)} with the result of {@link
     * Mqtt3SimpleAuthBuilder.Complete#build()}.
     *
     * @return the fluent builder for the simple auth related data.
     * @see #simpleAuth(Mqtt3SimpleAuth)
     * @since 1.1
     */
    @CheckReturnValue
    Mqtt3SimpleAuthBuilder.@NotNull Nested<? extends Mqtt3ClientBuilder> simpleAuthWith();

    /**
     * Sets the optional {@link Mqtt3ClientConfig#getWillPublish() Will Publish}.
     *
     * @param willPublish the Will Publish or <code>null</code> to remove any previously set Will Publish.
     * @return the builder.
     * @since 1.1
     */
    @CheckReturnValue
    @NotNull Mqtt3ClientBuilder willPublish(@Nullable Mqtt3Publish willPublish);

    /**
     * Fluent counterpart of {@link #willPublish(Mqtt3Publish)}.
     * <p>
     * Calling {@link Mqtt3WillPublishBuilder.Nested.Complete#applyWillPublish()} on the returned builder has the same
     * effect as calling {@link #willPublish(Mqtt3Publish)} with the result of {@link
     * Mqtt3WillPublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Will Publish.
     * @see #willPublish(Mqtt3Publish)
     * @since 1.1
     */
    @CheckReturnValue
    Mqtt3WillPublishBuilder.@NotNull Nested<? extends Mqtt3ClientBuilder> willPublishWith();

    /**
     * Builds the {@link Mqtt3Client}.
     *
     * @return the built {@link Mqtt3Client}.
     */
    @CheckReturnValue
    @NotNull Mqtt3Client build();

    /**
     * Builds the {@link Mqtt3RxClient}.
     *
     * @return the built {@link Mqtt3RxClient}.
     */
    @CheckReturnValue
    @NotNull Mqtt3RxClient buildRx();

    /**
     * Builds the {@link Mqtt3AsyncClient}.
     *
     * @return the built {@link Mqtt3AsyncClient}.
     */
    @CheckReturnValue
    @NotNull Mqtt3AsyncClient buildAsync();

    /**
     * Builds the {@link Mqtt3BlockingClient}.
     *
     * @return the built {@link Mqtt3BlockingClient}.
     */
    @CheckReturnValue
    @NotNull Mqtt3BlockingClient buildBlocking();
}

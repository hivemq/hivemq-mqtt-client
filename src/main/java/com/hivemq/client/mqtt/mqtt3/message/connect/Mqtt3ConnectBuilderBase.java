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

package com.hivemq.client.mqtt.mqtt3.message.connect;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder base for a {@link Mqtt3Connect}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3ConnectBuilderBase<B extends Mqtt3ConnectBuilderBase<B>> {

    /**
     * Sets the {@link Mqtt3Connect#getKeepAlive() keep alive} in seconds.
     * <p>
     * The value must be in the range of an unsigned short: [0, 65_535].
     *
     * @param keepAlive the keep alive in seconds.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B keepAlive(int keepAlive);

    /**
     * Disables the {@link Mqtt3Connect#getKeepAlive() keep alive} by setting it to {@link Mqtt3Connect#NO_KEEP_ALIVE}.
     *
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B noKeepAlive();

    /**
     * Sets whether the client connects with a {@link Mqtt3Connect#isCleanSession() clean session}.
     *
     * @param cleanSession whether the client connects with a clean session.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B cleanSession(boolean cleanSession);

    /**
     * Sets the optional {@link Mqtt3Connect#getSimpleAuth() simple authentication and/or authorization related data}.
     *
     * @param simpleAuth the simple auth related data or <code>null</code> to remove any previously set simple auth
     *                   related data.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B simpleAuth(@Nullable Mqtt3SimpleAuth simpleAuth);

    /**
     * Fluent counterpart of {@link #simpleAuth(Mqtt3SimpleAuth)}.
     * <p>
     * Calling {@link Mqtt3SimpleAuthBuilder.Nested.Complete#applySimpleAuth()} on the returned builder has the same
     * effect as calling {@link #simpleAuth(Mqtt3SimpleAuth)} with the result of {@link
     * Mqtt3SimpleAuthBuilder.Complete#build()}.
     *
     * @return the fluent builder for the simple auth related data.
     * @see #simpleAuth(Mqtt3SimpleAuth)
     */
    @CheckReturnValue
    Mqtt3SimpleAuthBuilder.@NotNull Nested<? extends B> simpleAuth();

    /**
     * Sets the optional {@link Mqtt3Connect#getWillPublish() Will Publish}.
     *
     * @param willPublish the Will Publish or <code>null</code> to remove any previously set Will Publish.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B willPublish(@Nullable Mqtt3Publish willPublish);

    /**
     * Fluent counterpart of {@link #willPublish(Mqtt3Publish)}.
     * <p>
     * Calling {@link Mqtt3WillPublishBuilder.Nested.Complete#applyWillPublish()} on the returned builder has the same
     * effect as calling {@link #willPublish(Mqtt3Publish)} with the result of {@link
     * Mqtt3WillPublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Will Publish.
     * @see #willPublish(Mqtt3Publish)
     */
    @CheckReturnValue
    Mqtt3WillPublishBuilder.@NotNull Nested<? extends B> willPublish();
}

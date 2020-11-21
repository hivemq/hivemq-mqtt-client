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

package com.hivemq.client.mqtt.mqtt5.message.connect;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Builder base for a {@link Mqtt5Connect}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ConnectBuilderBase<B extends Mqtt5ConnectBuilderBase<B>> {

    /**
     * Sets the {@link Mqtt5Connect#getKeepAlive() keep alive} in seconds.
     * <p>
     * The value must be in the range of an unsigned short: [0, 65_535].
     *
     * @param keepAlive the keep alive in seconds.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B keepAlive(@Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int keepAlive);

    /**
     * Disables the {@link Mqtt5Connect#getKeepAlive() keep alive} by setting it to {@link Mqtt5Connect#NO_KEEP_ALIVE}.
     *
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B noKeepAlive();

    /**
     * Sets whether the client {@link Mqtt5Connect#isCleanStart() starts a clean session}.
     *
     * @param cleanStart whether the client starts a clean session.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B cleanStart(boolean cleanStart);

    /**
     * Sets the {@link Mqtt5Connect#getSessionExpiryInterval() session expiry interval} in seconds.
     * <p>
     * The value must be in the range of an unsigned int: [0, 4_294_967_295].
     *
     * @param sessionExpiryInterval the session expiry interval in seconds.
     * @return teh builder.
     */
    @CheckReturnValue
    @NotNull B sessionExpiryInterval(
            @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval);

    /**
     * Disables the {@link Mqtt5Connect#getSessionExpiryInterval() session expiry} by setting it to {@link
     * Mqtt5Connect#NO_SESSION_EXPIRY}.
     *
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B noSessionExpiry();

    /**
     * Sets the {@link Mqtt5Connect#getRestrictions() restrictions} from the client.
     *
     * @param restrictions the restrictions from the client.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B restrictions(@NotNull Mqtt5ConnectRestrictions restrictions);

    /**
     * Fluent counterpart of {@link #restrictions(Mqtt5ConnectRestrictions)}.
     * <p>
     * Calling {@link Mqtt5ConnectRestrictionsBuilder.Nested#applyRestrictions()} on the returned builder has the effect
     * of {@link Mqtt5ConnectRestrictions#extend() extending} the current restrictions.
     *
     * @return the fluent builder for the restrictions.
     * @see #restrictions(Mqtt5ConnectRestrictions)
     */
    @CheckReturnValue
    Mqtt5ConnectRestrictionsBuilder.@NotNull Nested<? extends B> restrictionsWith();

    /**
     * Sets the optional {@link Mqtt5Connect#getSimpleAuth() simple authentication and/or authorization related data}.
     *
     * @param simpleAuth the simple auth related data or <code>null</code> to remove any previously set simple auth
     *                   related data.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B simpleAuth(@Nullable Mqtt5SimpleAuth simpleAuth);

    /**
     * Fluent counterpart of {@link #simpleAuth(Mqtt5SimpleAuth)}.
     * <p>
     * Calling {@link Mqtt5SimpleAuthBuilder.Nested.Complete#applySimpleAuth()} on the returned builder has the same
     * effect as calling {@link #simpleAuth(Mqtt5SimpleAuth)} with the result of {@link
     * Mqtt5SimpleAuthBuilder.Complete#build()}.
     *
     * @return the fluent builder for the simple auth related data.
     * @see #simpleAuth(Mqtt5SimpleAuth)
     */
    @CheckReturnValue
    Mqtt5SimpleAuthBuilder.@NotNull Nested<? extends B> simpleAuthWith();

    /**
     * Sets the {@link Mqtt5Connect#getEnhancedAuthMechanism() enhanced authentication and/or authorization mechanism}.
     *
     * @param enhancedAuthMechanism the enhanced auth mechanism or <code>null</code> to remove any previously set
     *                              enhanced auth mechanism.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B enhancedAuth(@Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism);

    /**
     * Sets the optional {@link Mqtt5Connect#getWillPublish() Will Publish}.
     *
     * @param willPublish the Will Publish or <code>null</code> to remove any previously set Will Publish.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B willPublish(@Nullable Mqtt5Publish willPublish);

    /**
     * Fluent counterpart of {@link #willPublish(Mqtt5Publish)}.
     * <p>
     * Calling {@link Mqtt5WillPublishBuilder.Nested.Complete#applyWillPublish()} on the returned builder has the same
     * effect as calling {@link #willPublish(Mqtt5Publish)} with the result of {@link
     * Mqtt5WillPublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Will Publish.
     * @see #willPublish(Mqtt5Publish)
     */
    @CheckReturnValue
    Mqtt5WillPublishBuilder.@NotNull Nested<? extends B> willPublishWith();

    /**
     * Sets the {@link Mqtt5Connect#getUserProperties() User Properties}.
     *
     * @param userProperties the User Properties.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B userProperties(@NotNull Mqtt5UserProperties userProperties);

    /**
     * Fluent counterpart of {@link #userProperties(Mqtt5UserProperties)}.
     * <p>
     * Calling {@link Mqtt5UserPropertiesBuilder.Nested#applyUserProperties()} on the returned builder has the effect of
     * {@link Mqtt5UserProperties#extend() extending} the current User Properties.
     *
     * @return the fluent builder for the User Properties.
     * @see #userProperties(Mqtt5UserProperties)
     */
    @CheckReturnValue
    Mqtt5UserPropertiesBuilder.@NotNull Nested<? extends B> userPropertiesWith();
}

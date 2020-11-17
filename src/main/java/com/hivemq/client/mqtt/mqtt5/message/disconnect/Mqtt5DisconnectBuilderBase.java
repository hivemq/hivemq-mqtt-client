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

package com.hivemq.client.mqtt.mqtt5.message.disconnect;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Builder base for a {@link Mqtt5Disconnect}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5DisconnectBuilderBase<B extends Mqtt5DisconnectBuilderBase<B>> {

    /**
     * Sets the {@link Mqtt5Disconnect#getReasonCode() Reason Code}.
     *
     * @param reasonCode the Reason Code.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B reasonCode(@NotNull Mqtt5DisconnectReasonCode reasonCode);

    /**
     * Sets the {@link Mqtt5Disconnect#getSessionExpiryInterval() session expiry interval} in seconds.
     * <p>
     * The value must be in the range of an unsigned int: [0, 4_294_967_295].
     *
     * @param sessionExpiryInterval the session expiry interval in seconds.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B sessionExpiryInterval(
            @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long sessionExpiryInterval);

    /**
     * Disables the {@link Mqtt5Disconnect#getSessionExpiryInterval() session expiry} by setting it to {@link
     * com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect#NO_SESSION_EXPIRY Mqtt5Connect.NO_SESSION_EXPIRY}.
     *
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B noSessionExpiry();

    /**
     * Sets the optional {@link Mqtt5Disconnect#getServerReference() server reference}.
     *
     * @param serverReference the server reference.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverReference(@Nullable String serverReference);

    /**
     * Sets the optional {@link Mqtt5Disconnect#getServerReference() server reference}.
     *
     * @param serverReference the server reference.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B serverReference(@Nullable MqttUtf8String serverReference);

    /**
     * Sets the optional {@link Mqtt5Disconnect#getReasonString() Reason String}.
     *
     * @param reasonString the Reason String.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B reasonString(@Nullable String reasonString);

    /**
     * Sets the optional {@link Mqtt5Disconnect#getReasonString() Reason String}.
     *
     * @param reasonString the Reason String.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B reasonString(@Nullable MqttUtf8String reasonString);

    /**
     * Sets the {@link Mqtt5Disconnect#getUserProperties() User Properties}.
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
    Mqtt5UserPropertiesBuilder.@NotNull Nested<? extends B> userProperties();
}

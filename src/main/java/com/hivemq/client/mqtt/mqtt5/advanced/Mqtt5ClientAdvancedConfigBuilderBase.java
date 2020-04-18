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

package com.hivemq.client.mqtt.mqtt5.advanced;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptors;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptorsBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder base for a {@link Mqtt5ClientAdvancedConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5ClientAdvancedConfigBuilderBase<B extends Mqtt5ClientAdvancedConfigBuilderBase<B>> {

    /**
     * Sets whether {@link Mqtt5ClientAdvancedConfig#isAllowServerReAuth() server re-auth is allowed}.
     *
     * @param allowServerReAuth whether server re-auth is allowed.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B allowServerReAuth(boolean allowServerReAuth);

    /**
     * Sets whether {@link Mqtt5ClientAdvancedConfig#isValidatePayloadFormat() the payload format is validated}.
     *
     * @param validatePayloadFormat whether the payload format is validated.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B validatePayloadFormat(boolean validatePayloadFormat);

    /**
     * Sets the optional {@link Mqtt5ClientInterceptors collection of interceptors of MQTT messages}.
     *
     * @param interceptors collection of interceptors of MQTT messages or <code>null</code> to remove any previously set
     *                     interceptors.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B interceptors(@Nullable Mqtt5ClientInterceptors interceptors);

    /**
     * Fluent counterpart of {@link #interceptors(Mqtt5ClientInterceptors)}.
     * <p>
     * Calling {@link Mqtt5ClientInterceptorsBuilder.Nested#applyInterceptors()} on the returned builder has the effect
     * of extending the current collection of interceptors.
     *
     * @return the fluent builder for the collection of interceptors.
     * @see #interceptors(Mqtt5ClientInterceptors)
     */
    @CheckReturnValue
    @NotNull Mqtt5ClientInterceptorsBuilder.Nested<? extends B> interceptors();
}

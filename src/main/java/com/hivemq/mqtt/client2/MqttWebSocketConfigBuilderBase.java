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

package com.hivemq.mqtt.client2;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Builder base for a {@link MqttWebSocketConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttWebSocketConfigBuilderBase<B extends MqttWebSocketConfigBuilderBase<B>> {

    /**
     * Sets the {@link MqttWebSocketConfig#getPath() path}.
     *
     * @param path the path.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B path(@NotNull String path);

    /**
     * Sets the {@link MqttWebSocketConfig#getQuery() query}.
     *
     * @param query the query.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B query(@NotNull String query);

    /**
     * Sets the {@link MqttWebSocketConfig#getSubprotocol() subprotocol}.
     *
     * @param subprotocol the subprotocol.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B subprotocol(@NotNull String subprotocol);

    /**
     * Sets the {@link MqttWebSocketConfig#getHandshakeTimeoutMs() WebSocket handshake timeout}.
     * <p>
     * The timeout in milliseconds must be in the range: [0, {@link Integer#MAX_VALUE}].
     *
     * @param timeout  the WebSocket handshake timeout or <code>0</code> to disable the timeout.
     * @param timeUnit the time unit of the given timeout (this timeout only supports millisecond precision).
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B handshakeTimeout(long timeout, @NotNull TimeUnit timeUnit);

    /**
     * Sets the {@link MqttWebSocketConfig#getHttpHeaders() headers}.
     *
     * @param httpHeaders http headers.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B httpHeaders(@NotNull Map<@NotNull String, @NotNull String> httpHeaders);
}

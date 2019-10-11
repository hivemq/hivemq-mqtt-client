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

package com.hivemq.client.mqtt;

import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder base for a {@link MqttWebSocketConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttWebSocketConfigBuilderBase<B extends MqttWebSocketConfigBuilderBase<B>> {

    /**
     * Sets the {@link MqttWebSocketConfig#getServerPath() server path}.
     *
     * @param serverPath the server path.
     * @return the builder.
     */
    @NotNull B serverPath(@NotNull String serverPath);

    /**
     * Sets the {@link MqttWebSocketConfig#getQueryString() query string}.
     *
     * @param queryString the query string.
     * @return the builder.
     */
    @NotNull B queryString(@NotNull String queryString);

    /**
     * Sets the {@link MqttWebSocketConfig#getSubprotocol() subprotocol}.
     *
     * @param subprotocol the subprotocol.
     * @return the builder.
     */
    @NotNull B subprotocol(@NotNull String subprotocol);
}

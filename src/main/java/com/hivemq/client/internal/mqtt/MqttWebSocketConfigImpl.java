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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.mqtt.MqttWebSocketConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author David Katz
 * @author Christian Hoff
 */
@Unmodifiable
public class MqttWebSocketConfigImpl implements MqttWebSocketConfig {

    static final @NotNull MqttWebSocketConfigImpl DEFAULT =
            new MqttWebSocketConfigImpl(DEFAULT_SERVER_PATH, DEFAULT_QUERY_STRING, DEFAULT_MQTT_SUBPROTOCOL,
                    DEFAULT_HANDSHAKE_TIMEOUT_MS);

    private final @NotNull String serverPath;
    private final @NotNull String queryString;
    private final @NotNull String subprotocol;
    private final @Range(from = 0, to = Integer.MAX_VALUE) int handshakeTimeoutMs;

    MqttWebSocketConfigImpl(
            final @NotNull String serverPath,
            final @NotNull String queryString,
            final @NotNull String subprotocol,
            final @Range(from = 0, to = Integer.MAX_VALUE) int handshakeTimeoutMs) {

        this.serverPath = serverPath;
        this.queryString = queryString;
        this.subprotocol = subprotocol;
        this.handshakeTimeoutMs = handshakeTimeoutMs;
    }

    @Override
    public @NotNull String getServerPath() {
        return serverPath;
    }

    @Override
    public @NotNull String getQueryString() {
        return queryString;
    }

    @Override
    public @NotNull String getSubprotocol() {
        return subprotocol;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getHandshakeTimeoutMs() {
        return handshakeTimeoutMs;
    }

    @Override
    public MqttWebSocketConfigImplBuilder.@NotNull Default extend() {
        return new MqttWebSocketConfigImplBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttWebSocketConfigImpl)) {
            return false;
        }
        final MqttWebSocketConfigImpl that = (MqttWebSocketConfigImpl) o;

        return serverPath.equals(that.serverPath) && queryString.equals(that.queryString) &&
                subprotocol.equals(that.subprotocol) && (handshakeTimeoutMs == that.handshakeTimeoutMs);
    }

    @Override
    public int hashCode() {
        int result = serverPath.hashCode();
        result = 31 * result + queryString.hashCode();
        result = 31 * result + subprotocol.hashCode();
        result = 31 * result + Integer.hashCode(handshakeTimeoutMs);
        return result;
    }
}

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
 */

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.mqtt.MqttWebSocketConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Katz
 * @author Christian Hoff
 */
public class MqttWebSocketConfigImpl implements MqttWebSocketConfig {

    static final @NotNull MqttWebSocketConfigImpl DEFAULT =
            new MqttWebSocketConfigImpl(DEFAULT_SERVER_PATH, DEFAULT_QUERY_STRING, DEFAULT_MQTT_SUBPROTOCOL);

    private final @NotNull String serverPath;
    private final @NotNull String queryString;
    private final @NotNull String subprotocol;

    MqttWebSocketConfigImpl(
            final @NotNull String serverPath, final @NotNull String queryString, final @NotNull String subprotocol) {

        this.serverPath = serverPath;
        this.queryString = queryString;
        this.subprotocol = subprotocol;
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
    public @NotNull MqttWebSocketConfigImplBuilder.Default extend() {
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
                subprotocol.equals(that.subprotocol);
    }

    @Override
    public int hashCode() {
        int result = serverPath.hashCode();
        result = 31 * result + queryString.hashCode();
        result = 31 * result + subprotocol.hashCode();
        return result;
    }
}

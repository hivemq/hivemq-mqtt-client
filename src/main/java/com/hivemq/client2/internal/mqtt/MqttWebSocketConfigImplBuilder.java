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

package com.hivemq.client2.internal.mqtt;

import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.MqttWebSocketConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttWebSocketConfigImplBuilder<B extends MqttWebSocketConfigImplBuilder<B>> {

    private @NotNull String serverPath = MqttWebSocketConfigImpl.DEFAULT_SERVER_PATH;
    private @NotNull String queryString = MqttWebSocketConfigImpl.DEFAULT_QUERY_STRING;
    private @NotNull String subprotocol = MqttWebSocketConfigImpl.DEFAULT_MQTT_SUBPROTOCOL;
    private @Range(from = 0, to = Integer.MAX_VALUE) int handshakeTimeoutMs =
            MqttWebSocketConfigImpl.DEFAULT_HANDSHAKE_TIMEOUT_MS;

    MqttWebSocketConfigImplBuilder() {}

    MqttWebSocketConfigImplBuilder(final @Nullable MqttWebSocketConfigImpl webSocketConfig) {
        if (webSocketConfig != null) {
            serverPath = webSocketConfig.getServerPath();
            queryString = webSocketConfig.getQueryString();
            subprotocol = webSocketConfig.getSubprotocol();
            handshakeTimeoutMs = webSocketConfig.getHandshakeTimeoutMs();
        }
    }

    abstract @NotNull B self();

    public @NotNull B serverPath(final @Nullable String serverPath) {
        // remove any leading slashes
        this.serverPath = Checks.notNull(serverPath, "Server path").replaceAll("^/+", "");
        return self();
    }

    public @NotNull B queryString(final @Nullable String queryString) {
        this.queryString = Checks.notNull(queryString, "Query string");
        return self();
    }

    public @NotNull B subprotocol(final @Nullable String subprotocol) {
        this.subprotocol = Checks.notNull(subprotocol, "Subprotocol");
        return self();
    }

    public @NotNull B handshakeTimeout(final long timeout, final @Nullable TimeUnit timeUnit) {
        Checks.notNull(timeUnit, "Time unit");
        this.handshakeTimeoutMs = (int) Checks.range(timeUnit.toMillis(timeout), 0, Integer.MAX_VALUE,
                "Handshake timeout in milliseconds");
        return self();
    }

    public @NotNull MqttWebSocketConfigImpl build() {
        return new MqttWebSocketConfigImpl(serverPath, queryString, subprotocol, handshakeTimeoutMs);
    }

    public static class Default extends MqttWebSocketConfigImplBuilder<Default> implements MqttWebSocketConfigBuilder {

        public Default() {}

        Default(final @Nullable MqttWebSocketConfigImpl webSocketConfig) {
            super(webSocketConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttWebSocketConfigImplBuilder<Nested<P>>
            implements MqttWebSocketConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttWebSocketConfigImpl, P> parentConsumer;

        Nested(
                final @Nullable MqttWebSocketConfigImpl webSocketConfig,
                final @NotNull Function<? super MqttWebSocketConfigImpl, P> parentConsumer) {

            super(webSocketConfig);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyWebSocketConfig() {
            return parentConsumer.apply(build());
        }
    }
}

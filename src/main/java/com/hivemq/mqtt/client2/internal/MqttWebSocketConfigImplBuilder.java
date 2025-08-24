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

package com.hivemq.mqtt.client2.internal;

import com.hivemq.mqtt.client2.MqttWebSocketConfigBuilder;
import com.hivemq.mqtt.client2.internal.util.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttWebSocketConfigImplBuilder<B extends MqttWebSocketConfigImplBuilder<B>> {

    private @NotNull String path = MqttWebSocketConfigImpl.DEFAULT_PATH;
    private @NotNull String query = MqttWebSocketConfigImpl.DEFAULT_QUERY;
    private @NotNull String subprotocol = MqttWebSocketConfigImpl.DEFAULT_SUBPROTOCOL;
    private @Range(from = 0, to = Integer.MAX_VALUE) int handshakeTimeoutMs =
            MqttWebSocketConfigImpl.DEFAULT_HANDSHAKE_TIMEOUT_MS;
    private @NotNull @Unmodifiable Map<@NotNull String, @NotNull String> headers =
            MqttWebSocketConfigImpl.DEFAULT_HEADERS;

    MqttWebSocketConfigImplBuilder() {}

    MqttWebSocketConfigImplBuilder(final @Nullable MqttWebSocketConfigImpl webSocketConfig) {
        if (webSocketConfig != null) {
            path = webSocketConfig.getPath();
            query = webSocketConfig.getQuery();
            subprotocol = webSocketConfig.getSubprotocol();
            handshakeTimeoutMs = webSocketConfig.getHandshakeTimeoutMs();
        }
    }

    abstract @NotNull B self();

    public @NotNull B path(final @Nullable String path) {
        Checks.notNull(path, "Path");
        if (!(path.isEmpty() || path.startsWith("/"))) {
            throw new IllegalArgumentException("Path must be empty or start with a '/'");
        }
        this.path = path;
        return self();
    }

    public @NotNull B query(final @Nullable String query) {
        this.query = Checks.notNull(query, "Query string");
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

    public @NotNull B headers(final @Nullable Map<@Nullable String, @Nullable String> headers) {
        Checks.notNull(headers, "Headers");
        if (headers.isEmpty()) {
            this.headers = Collections.emptyMap();
        } else {
            final LinkedHashMap<String, String> headersCopy = new LinkedHashMap<>();
            for (final Map.Entry<String, String> entry : headers.entrySet()) {
                headersCopy.put(
                        Checks.notNull(entry.getKey(), "Header key"), Checks.notNull(entry.getValue(), "Header value"));
            }
            this.headers = Collections.unmodifiableMap(headersCopy);
        }
        return self();
    }

    public @NotNull MqttWebSocketConfigImpl build() {
        return new MqttWebSocketConfigImpl(path, query, subprotocol, handshakeTimeoutMs, headers);
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

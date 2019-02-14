/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.internal.mqtt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.util.Checks;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.mqtt.MqttClientSslConfig;
import org.mqttbee.mqtt.MqttClientSslConfigBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttClientSslConfigImplBuilder<B extends MqttClientSslConfigImplBuilder<B>> {

    private @Nullable KeyManagerFactory keyManagerFactory;
    private @Nullable TrustManagerFactory trustManagerFactory;
    private @Nullable ImmutableList<String> cipherSuites;
    private @Nullable ImmutableList<String> protocols;
    private long handshakeTimeoutMs = MqttClientSslConfig.DEFAULT_HANDSHAKE_TIMEOUT_MS;

    abstract @NotNull B self();

    public @NotNull B keyManagerFactory(final @Nullable KeyManagerFactory keyManagerFactory) {
        this.keyManagerFactory = keyManagerFactory;
        return self();
    }

    public @NotNull B trustManagerFactory(final @Nullable TrustManagerFactory trustManagerFactory) {
        this.trustManagerFactory = trustManagerFactory;
        return self();
    }

    public @NotNull B cipherSuites(final @Nullable Collection<@Nullable String> cipherSuites) {
        this.cipherSuites = (cipherSuites == null) ? null : ImmutableList.copyOf(cipherSuites, "Cipher suites");
        return self();
    }

    public @NotNull B protocols(final @Nullable Collection<@Nullable String> protocols) {
        this.protocols = (protocols == null) ? null : ImmutableList.copyOf(protocols, "Protocols");
        return self();
    }

    public @NotNull B handshakeTimeout(final long timeout, final @Nullable TimeUnit timeUnit) {
        Checks.notNull(timeUnit, "Time unit");
        this.handshakeTimeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        return self();
    }

    public @NotNull MqttClientSslConfigImpl build() {
        return new MqttClientSslConfigImpl(
                keyManagerFactory, trustManagerFactory, cipherSuites, protocols, handshakeTimeoutMs);
    }

    public static class Default extends MqttClientSslConfigImplBuilder<Default> implements MqttClientSslConfigBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientSslConfigImplBuilder<Nested<P>>
            implements MqttClientSslConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientSslConfigImpl, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttClientSslConfigImpl, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applySslConfig() {
            return parentConsumer.apply(build());
        }
    }
}

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

import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientSslConfigBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
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
    private int handshakeTimeoutMs = (int) MqttClientSslConfigImpl.DEFAULT_HANDSHAKE_TIMEOUT_MS;
    private @Nullable HostnameVerifier hostnameVerifier = MqttClientSslConfigImpl.DEFAULT_HOSTNAME_VERIFIER;

    MqttClientSslConfigImplBuilder() {}

    MqttClientSslConfigImplBuilder(final @Nullable MqttClientSslConfigImpl sslConfig) {
        if (sslConfig != null) {
            keyManagerFactory = sslConfig.getRawKeyManagerFactory();
            trustManagerFactory = sslConfig.getRawTrustManagerFactory();
            cipherSuites = sslConfig.getRawCipherSuites();
            protocols = sslConfig.getRawProtocols();
            handshakeTimeoutMs = (int) sslConfig.getHandshakeTimeoutMs();
            hostnameVerifier = sslConfig.getRawHostnameVerifier();
        }
    }

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
        this.handshakeTimeoutMs = (int) Checks.range(timeUnit.toMillis(timeout), 0, Integer.MAX_VALUE,
                "Handshake timeout in milliseconds");
        return self();
    }

    public @NotNull B hostnameVerifier(final @Nullable HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier =
                (hostnameVerifier == null) ? MqttClientSslConfigImpl.DEFAULT_HOSTNAME_VERIFIER : hostnameVerifier;
        return self();
    }

    public @NotNull MqttClientSslConfigImpl build() {
        return new MqttClientSslConfigImpl(
                keyManagerFactory, trustManagerFactory, cipherSuites, protocols, handshakeTimeoutMs, hostnameVerifier);
    }

    public static class Default extends MqttClientSslConfigImplBuilder<Default> implements MqttClientSslConfigBuilder {

        public Default() {}

        Default(final @Nullable MqttClientSslConfigImpl sslConfig) {
            super(sslConfig);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttClientSslConfigImplBuilder<Nested<P>>
            implements MqttClientSslConfigBuilder.Nested<P> {

        private final @NotNull Function<? super MqttClientSslConfigImpl, P> parentConsumer;

        Nested(
                final @Nullable MqttClientSslConfigImpl sslConfig,
                final @NotNull Function<? super MqttClientSslConfigImpl, P> parentConsumer) {

            super(sslConfig);
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

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

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttTlsConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public class MqttTlsConfigImpl implements MqttTlsConfig {

    static final @Nullable HostnameVerifier DEFAULT_HOSTNAME_VERIFIER;

    static {
        HostnameVerifier hostnameVerifier = null;
        try {
            new SSLParameters().setEndpointIdentificationAlgorithm("HTTPS");
        } catch (final NoSuchMethodError e) { // Android API < 24 compatibility
            hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        }
        DEFAULT_HOSTNAME_VERIFIER = hostnameVerifier;
    }

    static final @NotNull MqttTlsConfigImpl DEFAULT =
            new MqttTlsConfigImpl(null, null, null, null, (int) DEFAULT_HANDSHAKE_TIMEOUT_MS,
                    DEFAULT_HOSTNAME_VERIFIER);

    private final @Nullable KeyManagerFactory keyManagerFactory;
    private final @Nullable TrustManagerFactory trustManagerFactory;
    private final @Nullable ImmutableList<String> cipherSuites;
    private final @Nullable ImmutableList<String> protocols;
    private final int handshakeTimeoutMs;
    private final @Nullable HostnameVerifier hostnameVerifier;

    MqttTlsConfigImpl(
            final @Nullable KeyManagerFactory keyManagerFactory,
            final @Nullable TrustManagerFactory trustManagerFactory,
            final @Nullable ImmutableList<String> cipherSuites,
            final @Nullable ImmutableList<String> protocols,
            final int handshakeTimeoutMs,
            final @Nullable HostnameVerifier hostnameVerifier) {

        this.keyManagerFactory = keyManagerFactory;
        this.trustManagerFactory = trustManagerFactory;
        this.cipherSuites = cipherSuites;
        this.protocols = protocols;
        this.handshakeTimeoutMs = handshakeTimeoutMs;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public @NotNull Optional<KeyManagerFactory> getKeyManagerFactory() {
        return Optional.ofNullable(keyManagerFactory);
    }

    public @Nullable KeyManagerFactory getRawKeyManagerFactory() {
        return keyManagerFactory;
    }

    @Override
    public @NotNull Optional<TrustManagerFactory> getTrustManagerFactory() {
        return Optional.ofNullable(trustManagerFactory);
    }

    public @Nullable TrustManagerFactory getRawTrustManagerFactory() {
        return trustManagerFactory;
    }

    @Override
    public @NotNull Optional<List<String>> getCipherSuites() {
        return Optional.ofNullable(cipherSuites);
    }

    public @Nullable ImmutableList<String> getRawCipherSuites() {
        return cipherSuites;
    }

    @Override
    public @NotNull Optional<List<String>> getProtocols() {
        return Optional.ofNullable(protocols);
    }

    public @Nullable ImmutableList<String> getRawProtocols() {
        return protocols;
    }

    @Override
    public long getHandshakeTimeoutMs() {
        return handshakeTimeoutMs;
    }

    @Override
    public @NotNull Optional<HostnameVerifier> getHostnameVerifier() {
        return Optional.ofNullable(hostnameVerifier);
    }

    public @Nullable HostnameVerifier getRawHostnameVerifier() {
        return hostnameVerifier;
    }

    @Override
    public MqttTlsConfigImplBuilder.@NotNull Default extend() {
        return new MqttTlsConfigImplBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttTlsConfigImpl)) {
            return false;
        }
        final MqttTlsConfigImpl that = (MqttTlsConfigImpl) o;

        return Objects.equals(keyManagerFactory, that.keyManagerFactory) &&
                Objects.equals(trustManagerFactory, that.trustManagerFactory) &&
                Objects.equals(cipherSuites, that.cipherSuites) && Objects.equals(protocols, that.protocols) &&
                (handshakeTimeoutMs == that.handshakeTimeoutMs) &&
                Objects.equals(hostnameVerifier, that.hostnameVerifier);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(keyManagerFactory);
        result = 31 * result + Objects.hashCode(trustManagerFactory);
        result = 31 * result + Objects.hashCode(cipherSuites);
        result = 31 * result + Objects.hashCode(protocols);
        result = 31 * result + Integer.hashCode(handshakeTimeoutMs);
        result = 31 * result + Objects.hashCode(hostnameVerifier);
        return result;
    }
}

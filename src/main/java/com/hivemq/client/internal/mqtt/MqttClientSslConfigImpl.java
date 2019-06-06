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

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public class MqttClientSslConfigImpl implements MqttClientSslConfig {

    static final @NotNull MqttClientSslConfigImpl DEFAULT =
            new MqttClientSslConfigImpl(null, null, null, null, DEFAULT_HANDSHAKE_TIMEOUT_MS);

    private final @Nullable KeyManagerFactory keyManagerFactory;
    private final @Nullable TrustManagerFactory trustManagerFactory;
    private final @Nullable ImmutableList<String> cipherSuites;
    private final @Nullable ImmutableList<String> protocols;
    private final long handshakeTimeoutMs;

    MqttClientSslConfigImpl(
            final @Nullable KeyManagerFactory keyManagerFactory,
            final @Nullable TrustManagerFactory trustManagerFactory, final @Nullable ImmutableList<String> cipherSuites,
            final @Nullable ImmutableList<String> protocols, final long handshakeTimeoutMs) {

        this.keyManagerFactory = keyManagerFactory;
        this.trustManagerFactory = trustManagerFactory;
        this.cipherSuites = cipherSuites;
        this.protocols = protocols;
        this.handshakeTimeoutMs = handshakeTimeoutMs;
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
    public @NotNull MqttClientSslConfigImplBuilder.Default extend() {
        return new MqttClientSslConfigImplBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttClientSslConfigImpl)) {
            return false;
        }
        final MqttClientSslConfigImpl that = (MqttClientSslConfigImpl) o;

        return Objects.equals(keyManagerFactory, that.keyManagerFactory) &&
                Objects.equals(trustManagerFactory, that.trustManagerFactory) &&
                Objects.equals(cipherSuites, that.cipherSuites) && Objects.equals(protocols, that.protocols) &&
                (handshakeTimeoutMs == that.handshakeTimeoutMs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(keyManagerFactory);
        result = 31 * result + Objects.hashCode(trustManagerFactory);
        result = 31 * result + Objects.hashCode(cipherSuites);
        result = 31 * result + Objects.hashCode(protocols);
        result = 31 * result + Long.hashCode(handshakeTimeoutMs);
        return result;
    }
}

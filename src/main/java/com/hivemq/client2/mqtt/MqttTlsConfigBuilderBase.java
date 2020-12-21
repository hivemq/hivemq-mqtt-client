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

package com.hivemq.client2.mqtt;

import com.hivemq.client2.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Builder base for a {@link MqttTlsConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttTlsConfigBuilderBase<B extends MqttTlsConfigBuilderBase<B>> {

    /**
     * Sets the optional user defined {@link MqttTlsConfig#getKeyManagerFactory() key manager factory}.
     *
     * @param keyManagerFactory the key manager factory or <code>null</code> to remove any previously set key manager
     *                          factory.
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B keyManagerFactory(@Nullable KeyManagerFactory keyManagerFactory);

    /**
     * Sets the optional user defined {@link MqttTlsConfig#getTrustManagerFactory() trust manager factory}.
     *
     * @param trustManagerFactory the trust manager factory or <code>null</code> to remove any previously set trust
     *                            manager factory
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B trustManagerFactory(@Nullable TrustManagerFactory trustManagerFactory);

    /**
     * Sets the optional user defined {@link MqttTlsConfig#getCipherSuites() cipher suites}.
     *
     * @param cipherSuites the cipher suites or <code>null</code> to use the default cipher suites of Netty (network
     *                     communication framework).
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B cipherSuites(@Nullable Collection<@NotNull String> cipherSuites);

    /**
     * Sets the optional user defined {@link MqttTlsConfig#getProtocols() protocols}.
     *
     * @param protocols the protocols or <code>null</code> to use the default protocols of Netty (network communication
     *                  framework).
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B protocols(@Nullable Collection<@NotNull String> protocols);

    /**
     * Sets the {@link MqttTlsConfig#getHandshakeTimeoutMs() TLS handshake timeout}.
     * <p>
     * The timeout in milliseconds must be in the range: [0, {@link Integer#MAX_VALUE}].
     *
     * @param timeout  the TLS handshake timeout or <code>0</code> to disable the timeout.
     * @param timeUnit the time unit of the given timeout (this timeout only supports millisecond precision).
     * @return the builder.
     */
    @CheckReturnValue
    @NotNull B handshakeTimeout(long timeout, @NotNull TimeUnit timeUnit);

    /**
     * Sets the optional user defined {@link MqttTlsConfig#getHostnameVerifier() hostname verifier}.
     *
     * @param hostnameVerifier the hostname verifier or <code>null</code> to use https hostname verification.
     * @return the builder.
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull B hostnameVerifier(@Nullable HostnameVerifier hostnameVerifier);
}

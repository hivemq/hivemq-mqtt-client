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
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Builder base for a {@link MqttClientSslConfig}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientSslConfigBuilderBase<B extends MqttClientSslConfigBuilderBase<B>> {

    /**
     * Sets the optional user defined {@link MqttClientSslConfig#getKeyManagerFactory() key manager factory}.
     *
     * @param keyManagerFactory the key manager factory or <code>null</code> to remove any previously set key manager
     *                          factory.
     * @return the builder.
     */
    @NotNull B keyManagerFactory(@Nullable KeyManagerFactory keyManagerFactory);

    /**
     * Sets the optional user defined {@link MqttClientSslConfig#getTrustManagerFactory() trunst manager factory}.
     *
     * @param trustManagerFactory the trust manager factory or <code>null</code> to remove any previously set trust
     *                            manager factory
     * @return the builder.
     */
    @NotNull B trustManagerFactory(@Nullable TrustManagerFactory trustManagerFactory);

    /**
     * Sets the optional user defined {@link MqttClientSslConfig#getCipherSuites() cipher suites}.
     *
     * @param cipherSuites the cipher suites or <code>null</code> to use the default cipher suites of Netty (network
     *                     communication framework).
     * @return the builder.
     */
    @NotNull B cipherSuites(@Nullable Collection<@NotNull String> cipherSuites);

    /**
     * Sets the optional user defined {@link MqttClientSslConfig#getProtocols() protocols}.
     *
     * @param protocols the protocols or <code>null</code> to use the default protocols of Netty (network communication
     *                  framework).
     * @return the builder.
     */
    @NotNull B protocols(@Nullable Collection<@NotNull String> protocols);

    /**
     * Sets the {@link MqttClientSslConfig#getHandshakeTimeoutMs() handshake timeout}.
     *
     * @param timeout  the handshake timeout.
     * @param timeUnit the time unit of the given timeout.
     * @return the builder.
     */
    @NotNull B handshakeTimeout(long timeout, @NotNull TimeUnit timeUnit);
}

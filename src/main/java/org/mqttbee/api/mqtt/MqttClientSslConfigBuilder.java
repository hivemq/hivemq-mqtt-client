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
 */

package org.mqttbee.api.mqtt;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientSslConfigImpl;
import org.mqttbee.util.FluentBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Christian Hoff
 */
public class MqttClientSslConfigBuilder<P> extends FluentBuilder<MqttClientSslConfig, P> {

    private @Nullable KeyManagerFactory keyManagerFactory;
    private @Nullable TrustManagerFactory trustManagerFactory;
    private @Nullable ImmutableList<String> cipherSuites;
    private @Nullable ImmutableList<String> protocols;
    private long handshakeTimeoutMs = MqttClientSslConfig.DEFAULT_HANDSHAKE_TIMEOUT_MS;

    public MqttClientSslConfigBuilder(final @Nullable Function<? super MqttClientSslConfig, P> parentConsumer) {
        super(parentConsumer);
    }

    public @NotNull MqttClientSslConfigBuilder<P> keyManagerFactory(
            final @Nullable KeyManagerFactory keyManagerFactory) {

        this.keyManagerFactory = keyManagerFactory;
        return this;
    }

    public @NotNull MqttClientSslConfigBuilder<P> trustManagerFactory(
            final @Nullable TrustManagerFactory trustManagerFactory) {

        this.trustManagerFactory = trustManagerFactory;
        return this;
    }

    /**
     * @param cipherSuites if <code>null</code>, netty's default cipher suites will be used
     */
    public @NotNull MqttClientSslConfigBuilder<P> cipherSuites(final @Nullable List<String> cipherSuites) {
        this.cipherSuites = (cipherSuites == null) ? null : ImmutableList.copyOf(cipherSuites);
        return this;
    }

    /**
     * @param protocols if <code>null</code>, netty's default protocols will be used
     */
    public @NotNull MqttClientSslConfigBuilder<P> protocols(final @Nullable List<String> protocols) {
        this.protocols = (protocols == null) ? null : ImmutableList.copyOf(protocols);
        return this;
    }

    public @NotNull MqttClientSslConfigBuilder<P> handshakeTimeout(
            final long timeout, final @NotNull TimeUnit timeUnit) {

        this.handshakeTimeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        return this;
    }

    @Override
    public @NotNull MqttClientSslConfig build() {
        return new MqttClientSslConfigImpl(
                keyManagerFactory, trustManagerFactory, cipherSuites, protocols, handshakeTimeoutMs);
    }

    public @NotNull P applySslConfig() {
        return apply();
    }

}

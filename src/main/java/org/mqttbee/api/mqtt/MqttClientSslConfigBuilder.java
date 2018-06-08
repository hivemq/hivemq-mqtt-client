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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientSslConfigImpl;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MqttClientSslConfigBuilder {

    private KeyStore keyStore = null;
    private String keyStorePassword = "";
    private KeyStore trustStore = null;
    private ImmutableList<String> cipherSuites = null;
    private ImmutableList<String> protocols = null;
    private long handshakeTimeoutMs = MqttClientSslConfig.DEFAULT_HANDSHAKE_TIMEOUT_MS;

    @NotNull
    public MqttClientSslConfigBuilder keyStore(@Nullable final KeyStore keyStore) {
        this.keyStore = keyStore;
        return this;
    }

    @NotNull
    public MqttClientSslConfigBuilder keyStorePassword(@NotNull final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    @NotNull
    public MqttClientSslConfigBuilder trustStore(@Nullable final KeyStore trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    /**
     * @param cipherSuites if <code>null</code>, netty's default cipher suites will be used
     */
    @NotNull
    public MqttClientSslConfigBuilder cipherSuites(@Nullable final List<String> cipherSuites) {
        this.cipherSuites = (cipherSuites == null) ? null : ImmutableList.copyOf(cipherSuites);
        return this;
    }

    /**
     * @param protocols if <code>null</code>, netty's default protocols will be used
     */
    @NotNull
    public MqttClientSslConfigBuilder protocols(@Nullable final List<String> protocols) {
        this.protocols = (protocols == null) ? null : ImmutableList.copyOf(protocols);
        return this;
    }

    @NotNull
    public MqttClientSslConfigBuilder handshakeTimeout(long timeout, TimeUnit timeUnit) {
        this.handshakeTimeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        return this;
    }

    @NotNull
    public MqttClientSslConfig build() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return new MqttClientSslConfigImpl(keyManagerFactory, trustManagerFactory, cipherSuites, protocols, handshakeTimeoutMs);
    }

}

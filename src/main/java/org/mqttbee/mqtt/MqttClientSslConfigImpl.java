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
package org.mqttbee.mqtt;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClientSslConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.Arrays;
import java.util.List;

/**
 *  Default MqttClientSslConfig implementation:
 *
 *  Handshake timeout set to 10 seconds
 *
 *  @author David Katz
 */
public class MqttClientSslConfigImpl implements MqttClientSslConfig {
    public static final MqttClientSslConfig DEFAULT = new MqttClientSslConfigImpl(null, null);

    private final KeyManagerFactory keyManagerFactory;
    private final TrustManagerFactory trustManagerFactory;
    private final ImmutableList<String> cipherSuites;
    private final List<String> protocols = Arrays.asList("TLSv1.2", "TLSv1.1");
    private final long handshakeTimeoutMs;

    public MqttClientSslConfigImpl(
            @Nullable KeyManagerFactory keyManagerFactory, @Nullable TrustManagerFactory trustManagerFactory) {
        this(keyManagerFactory, trustManagerFactory, null, DEFAULT_HANDSHAKE_TIMEOUT_MS);
    }

    public MqttClientSslConfigImpl(
            @Nullable KeyManagerFactory keyManagerFactory, @Nullable TrustManagerFactory trustManagerFactory,
            @Nullable ImmutableList<String> cipherSuites, long handshakeTimeoutMs) {
        this.keyManagerFactory = keyManagerFactory;
        this.trustManagerFactory = trustManagerFactory;
        this.cipherSuites = cipherSuites;
        this.handshakeTimeoutMs = handshakeTimeoutMs;
    }

    @Nullable
    @Override
    public KeyManagerFactory getKeyManagerFactory() {
        return keyManagerFactory;
    }

    @Nullable
    @Override
    public TrustManagerFactory getTrustManagerFactory() {
        return trustManagerFactory;
    }

    @Nullable
    @Override
    public ImmutableList<String> getCipherSuites() {
        return cipherSuites;
    }

    @NotNull
    @Override
    public List<String> getProtocols() {
        return protocols;
    }

    @Override
    public long getHandshakeTimeoutMs() {
        return handshakeTimeoutMs;
    }
}

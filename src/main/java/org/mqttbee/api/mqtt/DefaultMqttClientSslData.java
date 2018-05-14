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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.*;
import java.util.Arrays;
import java.util.List;

/**
 *  Default MqttClientSslData implementation:
 *
 *  Handshake timeout set to 10 seconds and pre-selected
 *  list of allowed cipher suites
 *
 *  @author David Katz
 */
public class DefaultMqttClientSslData implements MqttClientSslData {
    private static final List<String> cipherSuites = Arrays.asList("TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305",
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305",
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_SHA",
            "TLS_ECDHE_RSA_WITH_CHACHA20_SHA",

            "TLS_DHE_RSA_WITH_CHACHA20_POLY1305",
            "TLS_RSA_WITH_CHACHA20_POLY1305",
            "TLS_DHE_RSA_WITH_CHACHA20_SHA",
            "TLS_RSA_WITH_CHACHA20_SHA",

            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",

            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256");
    private KeyManagerFactory keyManagerFactory;
    private final TrustManagerFactory trustManagerFactory;
    private final List<String> protocols = Arrays.asList("TLSv1.2", "TLSv1.1");

    public DefaultMqttClientSslData() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(null, "".toCharArray());
        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
    }

    @Override
    public KeyManagerFactory keyManagerFactory() {
        return keyManagerFactory;
    }

    @Override
    public TrustManagerFactory trustManagerFactory() {
        return trustManagerFactory;
    }

    @Override
    public List<String> cipherSuites() {
        return cipherSuites;
    }

    @Override
    public List<String> protocols() {
        return protocols;
    }

    @Override
    public int handshakeTimeoutMs() {
        return 10 * 1000;
    }
}

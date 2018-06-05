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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientSslConfigImpl;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class MqttClientSslConfigBuilder {

    private KeyStore keyStore = null;
    private String keyStorePassword = "";
    private KeyStore trustStore = null;

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

    @NotNull
    public MqttClientSslConfig build() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return new MqttClientSslConfigImpl(keyManagerFactory, trustManagerFactory);
    }

}

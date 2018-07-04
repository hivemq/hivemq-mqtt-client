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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** @author Christian Hoff */
public class MqttClientSslConfigBuilderTest {
    @Test
    public void cipherSuites_simple()
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        List<String> expectedCipherSuites = Arrays.asList("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305");

        MqttClientSslConfig sslConfig =
                MqttClientSslConfig.builder()
                        .cipherSuites(Lists.newArrayList("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305"))
                        .build();

        assertThat(sslConfig.getCipherSuites(), is(expectedCipherSuites));
    }

    @Test
    public void cipherSuites_null()
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().cipherSuites(null).build();

        assertNull(sslConfig.getCipherSuites());
    }

    @Test
    public void protocols_simple()
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        List<String> expectedProtocols = Arrays.asList("TLSv1.2", "TLSv1.1");

        MqttClientSslConfig sslConfig =
                MqttClientSslConfig.builder()
                        .protocols(Lists.newArrayList("TLSv1.2", "TLSv1.1"))
                        .build();

        assertThat(sslConfig.getProtocols(), is(expectedProtocols));
    }

    @Test
    public void protocols_null()
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().protocols(null).build();

        assertNull(sslConfig.getProtocols());
    }
}

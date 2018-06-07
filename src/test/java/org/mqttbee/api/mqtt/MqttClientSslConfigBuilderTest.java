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

import com.google.common.collect.Lists;
import org.junit.Test;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Christian Hoff
 */
public class MqttClientSslConfigBuilderTest {
    @Test
    public void cipherSuites_simple() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        List<String> expectedCipherSuites = Arrays.asList("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305");

        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder()
                .cipherSuites(Lists.newArrayList("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305"))
                .build();

        assertThat(sslConfig.getCipherSuites(), is(expectedCipherSuites));
    }

    @Test
    public void cipherSuites_null() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder()
                .cipherSuites(null)
                .build();

        assertNull(sslConfig.getCipherSuites());
    }
}

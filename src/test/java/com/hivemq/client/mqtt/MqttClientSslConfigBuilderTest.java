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

package com.hivemq.client.mqtt;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Hoff
 */
class MqttClientSslConfigBuilderTest {

    @Test
    void cipherSuites_simple() {
        final List<String> expectedCipherSuites = Arrays.asList("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305");

        final MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().cipherSuites(expectedCipherSuites).build();

        assertNotNull(sslConfig.getCipherSuites());
        assertTrue(sslConfig.getCipherSuites().isPresent());
        assertEquals(sslConfig.getCipherSuites().get(), expectedCipherSuites);
    }

    @Test
    void cipherSuites_null() {
        final MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().cipherSuites(null).build();

        assertNotNull(sslConfig.getCipherSuites());
        assertFalse(sslConfig.getCipherSuites().isPresent());
    }

    @Test
    void protocols_simple() {
        final List<String> expectedProtocols = Arrays.asList("TLSv1.2", "TLSv1.1");

        final MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().protocols(expectedProtocols).build();

        assertNotNull(sslConfig.getProtocols());
        assertTrue(sslConfig.getProtocols().isPresent());
        assertEquals(sslConfig.getProtocols().get(), expectedProtocols);
    }

    @Test
    void protocols_null() {
        final MqttClientSslConfig sslConfig = MqttClientSslConfig.builder().protocols(null).build();

        assertNotNull(sslConfig.getProtocols());
        assertFalse(sslConfig.getProtocols().isPresent());
    }
}

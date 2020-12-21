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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Hoff
 */
class MqttTlsConfigBuilderTest {

    @Test
    void cipherSuites_simple() {
        final List<String> expectedCipherSuites = Arrays.asList("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305");

        final MqttTlsConfig tlsConfig = MqttTlsConfig.builder().cipherSuites(expectedCipherSuites).build();

        assertNotNull(tlsConfig.getCipherSuites());
        assertTrue(tlsConfig.getCipherSuites().isPresent());
        assertEquals(tlsConfig.getCipherSuites().get(), expectedCipherSuites);
    }

    @Test
    void cipherSuites_null() {
        final MqttTlsConfig tlsConfig = MqttTlsConfig.builder().cipherSuites(null).build();

        assertNotNull(tlsConfig.getCipherSuites());
        assertFalse(tlsConfig.getCipherSuites().isPresent());
    }

    @Test
    void protocols_simple() {
        final List<String> expectedProtocols = Arrays.asList("TLSv1.2", "TLSv1.1");

        final MqttTlsConfig tlsConfig = MqttTlsConfig.builder().protocols(expectedProtocols).build();

        assertNotNull(tlsConfig.getProtocols());
        assertTrue(tlsConfig.getProtocols().isPresent());
        assertEquals(tlsConfig.getProtocols().get(), expectedProtocols);
    }

    @Test
    void protocols_null() {
        final MqttTlsConfig tlsConfig = MqttTlsConfig.builder().protocols(null).build();

        assertNotNull(tlsConfig.getProtocols());
        assertFalse(tlsConfig.getProtocols().isPresent());
    }
}

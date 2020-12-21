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

package com.hivemq.client2.internal.mqtt;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.NoSuchAlgorithmException;

/**
 * @author Silvio Giebl
 */
class MqttTransportConfigImplTest {

    @Test
    void equals() throws NoSuchAlgorithmException {
        final KeyManagerFactory kmf1 = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final KeyManagerFactory kmf2 = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final TrustManagerFactory tmf1 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        final TrustManagerFactory tmf2 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        EqualsVerifier.forClass(MqttTransportConfigImpl.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .withIgnoredAnnotations(NotNull.class) // EqualsVerifier thinks @NotNull Optional is @NotNull
                .withNonnullFields("serverAddress")
                .withPrefabValues(KeyManagerFactory.class, kmf1, kmf2)
                .withPrefabValues(TrustManagerFactory.class, tmf1, tmf2)
                .verify();
    }
}
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

package com.hivemq.client2.internal.mqtt.message.publish;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Hoff
 * @author Silvio Giebl
 */
class MqttPublishTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(MqttPublish.class)
                .withIgnoredAnnotations(NotNull.class) // EqualsVerifier thinks @NotNull Optional is @NotNull
                .withNonnullFields("topic", "qos", "userProperties")
                .withIgnoredFields("confirmable")
                .withRedefinedSubclass(MqttWillPublish.class)
                .verify();
    }

    @Test
    void getPayloadAsBytes() {
        final byte[] payload = {1, 2, 3, 4, 5};
        final MqttPublish publish = new MqttPublishBuilder.Default().topic("topic").payload(payload).build();
        assertArrayEquals(payload, publish.getPayloadAsBytes());
    }

    @Test
    void getPayloadAsBytes_payloadIsNull() {
        final MqttPublish publish = new MqttPublishBuilder.Default().topic("topic").build();
        assertNotNull(publish.getPayloadAsBytes());
        assertEquals(0, publish.getPayloadAsBytes().length);
    }

    @Test
    void getPayloadAsBytes_concurrent() {
        final byte[] payload = new byte[1_000_000];
        final MqttPublish publish = new MqttPublishBuilder.Default().topic("topic").payload(payload).build();
        final Executable executable = () -> {
            for (int i = 0; i < 100; i++) {
                assertArrayEquals(payload, publish.getPayloadAsBytes());
            }
        };
        assertAll(IntStream.range(0, 16).mapToObj(i -> executable).parallel());
    }
}

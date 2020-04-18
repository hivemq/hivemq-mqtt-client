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

package com.hivemq.client.internal.mqtt.message.publish;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

/**
 * @author Silvio Giebl
 */
class MqttWillPublishTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(MqttWillPublish.class)
                .withIgnoredAnnotations(NotNull.class) // EqualsVerifier thinks @NotNull Optional is @NotNull
                .withNonnullFields("topic", "qos", "userProperties")
                .withIgnoredFields("confirmable")
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
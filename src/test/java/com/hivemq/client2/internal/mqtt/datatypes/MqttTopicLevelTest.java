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

package com.hivemq.client2.internal.mqtt.datatypes;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttTopicLevelTest {

    @Test
    void of_singleLevelWildcardIsSame() {
        final MqttTopicLevel topicLevel1 = MqttTopicLevel.of("+".getBytes(), 0, 1);
        final MqttTopicLevel topicLevel2 = MqttTopicLevel.of("+".getBytes(), 0, 1);
        final MqttTopicLevel topicLevel3 = MqttTopicLevel.of("+/abc".getBytes(), 0, 1);
        assertSame(topicLevel1, topicLevel2);
        assertSame(topicLevel1, topicLevel3);
        assertEquals(1, topicLevel1.getArray().length);
        assertEquals(1, topicLevel2.getArray().length);
        assertEquals(1, topicLevel3.getArray().length);
    }

    @Test
    void isSingleLevelWildcard() {
        final MqttTopicLevel topicLevel1 = MqttTopicLevel.of("+".getBytes(), 0, 1);
        final MqttTopicLevel topicLevel2 = MqttTopicLevel.of("+/abc".getBytes(), 0, 1);
        final MqttTopicLevel topicLevel3 = MqttTopicLevel.of("+/abc/def".getBytes(), 0, 1);
        assertTrue(topicLevel1.isSingleLevelWildcard());
        assertTrue(topicLevel2.isSingleLevelWildcard());
        assertTrue(topicLevel3.isSingleLevelWildcard());
        assertEquals(1, topicLevel1.getArray().length);
        assertEquals(1, topicLevel2.getArray().length);
        assertEquals(1, topicLevel3.getArray().length);
        final MqttTopicLevel topicLevel4 = MqttTopicLevel.of("abc/+".getBytes(), 0, 3);
        final MqttTopicLevel topicLevel5 = MqttTopicLevel.of("abc/+/def".getBytes(), 0, 3);
        assertFalse(topicLevel4.isSingleLevelWildcard());
        assertFalse(topicLevel5.isSingleLevelWildcard());
        assertEquals(3, topicLevel4.getArray().length);
        assertEquals(3, topicLevel5.getArray().length);
    }

    @Test
    void trim_isSame() {
        final MqttTopicLevel topicLevel1 = MqttTopicLevel.of("+".getBytes(), 0, 1);
        final MqttTopicLevel topicLevel2 = MqttTopicLevel.of("abc".getBytes(), 0, 3);
        final MqttTopicLevel topicLevel3 = MqttTopicLevel.of("abc/def".getBytes(), 0, 3);
        assertSame(topicLevel1, topicLevel1.trim());
        assertSame(topicLevel2, topicLevel2.trim());
        assertSame(topicLevel3, topicLevel3.trim());
    }

    @Test
    void equals() {
        EqualsVerifier.forClass(MqttTopicLevel.class).verify();
    }
}
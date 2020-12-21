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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttTopicLevelsTest {

    @Test
    void concat_topicLevel() {
        final MqttTopicLevel level1 = MqttTopicLevel.of("test".getBytes(), 0, 4);
        final MqttTopicLevel level2 = MqttTopicLevel.of("topic".getBytes(), 0, 5);
        final MqttTopicLevels concat = MqttTopicLevels.concat(level1, level2);
        assertEquals(level1, concat);
        assertEquals(4, concat.getEnd());
        assertEquals(4 + 1 + 5, concat.getArray().length);
        assertEqualsWithClass(level1, concat.before(4));
        assertEqualsWithClass(level2, concat.after(4));
    }

    @Test
    void concat_topicLevelAndTopicLevels() {
        final MqttTopicLevel level1 = MqttTopicLevel.of("test".getBytes(), 0, 4);
        final MqttTopicLevel level2 = MqttTopicLevel.of("topic".getBytes(), 0, 5);
        final MqttTopicLevel level3 = MqttTopicLevel.of("filter".getBytes(), 0, 6);
        final MqttTopicLevels levels1 = MqttTopicLevels.concat(level1, level2);
        final MqttTopicLevels levels2 = MqttTopicLevels.concat(level2, level3);
        final MqttTopicLevels concat1 = MqttTopicLevels.concat(levels1, level3);
        final MqttTopicLevels concat2 = MqttTopicLevels.concat(level1, levels2);
        assertEquals(level1, concat1);
        assertEquals(level1, concat2);
        assertEquals(4, concat1.getEnd());
        assertEquals(4, concat2.getEnd());
        assertEquals(4 + 1 + 5 + 1 + 6, concat1.getArray().length);
        assertEquals(4 + 1 + 5 + 1 + 6, concat2.getArray().length);
        assertEqualsWithClass(level1, concat1.before(4));
        assertEqualsWithClass(level1, concat2.before(4));
        assertEqualsWithClass(levels1, concat1.before(4 + 1 + 5));
        assertEqualsWithClass(levels1, concat2.before(4 + 1 + 5));
        assertEqualsWithClass(levels2, concat1.after(4));
        assertEqualsWithClass(levels2, concat2.after(4));
        assertEqualsWithClass(level3, concat1.after(4 + 1 + 5));
        assertEqualsWithClass(level3, concat2.after(4 + 1 + 5));
    }

    @Test
    void concat_TopicLevels() {
        final MqttTopicLevel level1 = MqttTopicLevel.of("test".getBytes(), 0, 4);
        final MqttTopicLevel level2 = MqttTopicLevel.of("topic".getBytes(), 0, 5);
        final MqttTopicLevel level3 = MqttTopicLevel.of("filter".getBytes(), 0, 6);
        final MqttTopicLevel level4 = MqttTopicLevel.of("abc".getBytes(), 0, 3);
        final MqttTopicLevels levels1 = MqttTopicLevels.concat(level1, level2);
        final MqttTopicLevels levels2 = MqttTopicLevels.concat(level3, level4);
        final MqttTopicLevels levels3 = MqttTopicLevels.concat(levels1, level3);
        final MqttTopicLevels levels4 = MqttTopicLevels.concat(level2, levels2);
        final MqttTopicLevels concat = MqttTopicLevels.concat(levels1, levels2);
        assertEquals(level1, concat);
        assertEquals(4, concat.getEnd());
        assertEquals(4 + 1 + 5 + 1 + 6 + 1 + 3, concat.getArray().length);
        assertEqualsWithClass(level1, concat.before(4));
        assertEqualsWithClass(levels1, concat.before(4 + 1 + 5));
        assertEqualsWithClass(levels3, concat.before(4 + 1 + 5 + 1 + 6));
        assertEqualsWithClass(levels4, concat.after(4));
        assertEqualsWithClass(levels2, concat.after(4 + 1 + 5));
        assertEqualsWithClass(level4, concat.after(4 + 1 + 5 + 1 + 6));
    }

    @Test
    void before_allIsSame() {
        final MqttTopicLevel level1 = MqttTopicLevel.of("test".getBytes(), 0, 4);
        final MqttTopicLevel level2 = MqttTopicLevel.of("topic".getBytes(), 0, 5);
        final MqttTopicLevel level3 = MqttTopicLevel.of("filter".getBytes(), 0, 6);
        final MqttTopicLevels levels1 = MqttTopicLevels.concat(level1, level2);
        final MqttTopicLevels concat = MqttTopicLevels.concat(levels1, level3);
        assertSame(concat, concat.before(4 + 1 + 5 + 1 + 6));
    }

    static void assertEqualsWithClass(final @NotNull MqttTopicLevel level1, final @NotNull MqttTopicLevel level2) {
        assertEquals(level1, level2);
        assertSame(level1.getClass(), level2.getClass());
        assertArrayEquals(level1.trim().getArray(), level2.trim().getArray());
    }
}
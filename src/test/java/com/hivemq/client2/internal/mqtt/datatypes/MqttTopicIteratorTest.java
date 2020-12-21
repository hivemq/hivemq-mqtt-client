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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttTopicIteratorTest {

    @Test
    void of_topic() {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicImpl.of("test/topic"));

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level1 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("test".getBytes(), 0, 4), level1);
        assertFalse(level1.isSingleLevelWildcard());

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level2 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("topic".getBytes(), 0, 5), level2);
        assertFalse(level2.isSingleLevelWildcard());

        assertFalse(topicIterator.hasNext());
        assertFalse(topicIterator.hasMultiLevelWildcard());
        assertThrows(NoSuchElementException.class, topicIterator::next);
    }

    @Test
    void of_topicFilter() {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of("test/+/topic/#"));

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level1 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("test".getBytes(), 0, 4), level1);
        assertFalse(level1.isSingleLevelWildcard());

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level2 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("+".getBytes(), 0, 1), level2);
        assertTrue(level2.isSingleLevelWildcard());

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level3 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("topic".getBytes(), 0, 5), level3);
        assertFalse(level3.isSingleLevelWildcard());

        assertFalse(topicIterator.hasNext());
        assertTrue(topicIterator.hasMultiLevelWildcard());
        assertThrows(NoSuchElementException.class, topicIterator::next);
    }

    @Test
    void fork() {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of("test/+/topic/#"));
        topicIterator.next();

        final MqttTopicIterator fork = topicIterator.fork();

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level2 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("+".getBytes(), 0, 1), level2);
        assertTrue(level2.isSingleLevelWildcard());
        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel level3 = topicIterator.next();
        assertEquals(MqttTopicLevel.of("topic".getBytes(), 0, 5), level3);
        assertFalse(level3.isSingleLevelWildcard());
        assertFalse(topicIterator.hasNext());
        assertTrue(topicIterator.hasMultiLevelWildcard());
        assertThrows(NoSuchElementException.class, topicIterator::next);

        assertTrue(fork.hasNext());
        final MqttTopicLevel forkLevel2 = fork.next();
        assertEquals(MqttTopicLevel.of("+".getBytes(), 0, 1), forkLevel2);
        assertTrue(forkLevel2.isSingleLevelWildcard());
        assertTrue(fork.hasNext());
        final MqttTopicLevel forkLevel3 = fork.next();
        assertEquals(MqttTopicLevel.of("topic".getBytes(), 0, 5), forkLevel3);
        assertFalse(forkLevel3.isSingleLevelWildcard());
        assertFalse(fork.hasNext());
        assertTrue(fork.hasMultiLevelWildcard());
        assertThrows(NoSuchElementException.class, fork::next);
    }

    @ParameterizedTest
    @CsvSource({"test/topic, topic", "test/+, +", "test/+/#, +"})
    void trim_topicLevel(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevel trim = topicIterator.next().trim();
        final MqttTopicLevel level = MqttTopicLevel.of(topicLevels.getBytes(), 0, topicLevels.getBytes().length);
        MqttTopicLevelsTest.assertEqualsWithClass(level, trim);
    }

    @ParameterizedTest
    @CsvSource({"test/+/topic, +/topic", "test/+/topic/#, +/topic"})
    void trim_topicLevels(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevel trim = topicIterator.next().trim();
        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        MqttTopicLevelsTest.assertEqualsWithClass(levels, trim);
    }

    @ParameterizedTest
    @CsvSource({"test/topic/filter, test/topic/filter", "test/+/topic, test/+/topic", "test/+/topic/#, test/+/topic"})
    void forwardIfEqual_equalToEnd(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertTrue(topicIterator.forwardIfEqual(levels));

        assertFalse(topicIterator.hasNext());
        assertEquals(topicFilter.endsWith("#"), topicIterator.hasMultiLevelWildcard());
    }

    @ParameterizedTest
    @CsvSource({
            "test/topic/filter/abc, test/topic/filter", "test/+/topic/abc, test/+/topic",
            "test/+/topic/abc/#, test/+/topic"
    })
    void forwardIfEqual_remainingLevels(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertTrue(topicIterator.forwardIfEqual(levels));

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel lastLevel = topicIterator.next();
        assertEquals(MqttTopicLevel.of("abc".getBytes(), 0, 3), lastLevel);
        assertFalse(lastLevel.isSingleLevelWildcard());
        assertFalse(topicIterator.hasNext());
        assertEquals(topicFilter.endsWith("#"), topicIterator.hasMultiLevelWildcard());
    }

    @ParameterizedTest
    @CsvSource({
            "test/topic/filter, test/topic/filter/abc", "test/+/topic, test/+/topic/abc",
            "test/+/topic/#, test/+/topic/abc"
    })
    void forwardIfEqual_tooShort(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();
        final int start = topicIterator.getStart();
        final int end = topicIterator.getEnd();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertFalse(topicIterator.forwardIfEqual(levels));

        assertEquals(start, topicIterator.getStart());
        assertEquals(end, topicIterator.getEnd());
    }

    @ParameterizedTest
    @CsvSource({
            "test, test/topic", "test/topic/filter, test/topic/filte2", "test/topic/filter, test/topic/filter2",
            "test/topic/filter2, test/topic/filter", "test/+/topic, test/+/topi2", "test/+/topic, test/+/topic2",
            "test/+/topic2, test/+/topic", "test/+/topic/#, test/+/topi2", "test/+/topic/#, test/+/topic2",
            "test/+/topic2/#, test/+/topic", "test/topic/+, test/topic/abc"
    })
    void forwardIfEqual_notFullyEqual(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();
        final int start = topicIterator.getStart();
        final int end = topicIterator.getEnd();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertFalse(topicIterator.forwardIfEqual(levels));

        assertEquals(start, topicIterator.getStart());
        assertEquals(end, topicIterator.getEnd());
    }

    @ParameterizedTest
    @CsvSource({"test/topic/filter, test/topic/filter", "test/+/topic, test/+/topic", "test/+/topic/#, test/+/topic"})
    void forwardWhileEqual_equalToEnd(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertEquals(topicLevels.length(), topicIterator.forwardWhileEqual(levels));

        assertFalse(topicIterator.hasNext());
        assertEquals(topicFilter.endsWith("#"), topicIterator.hasMultiLevelWildcard());
    }

    @ParameterizedTest
    @CsvSource({
            "test/topic/filter/abc, test/topic/filter", "test/+/topic/abc, test/+/topic",
            "test/+/topic/abc/#, test/+/topic"
    })
    void forwardWhileEqual_remainingLevels(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertEquals(topicLevels.length(), topicIterator.forwardWhileEqual(levels));

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel lastLevel = topicIterator.next();
        assertEquals(MqttTopicLevel.of("abc".getBytes(), 0, 3), lastLevel);
        assertFalse(lastLevel.isSingleLevelWildcard());
        assertFalse(topicIterator.hasNext());
        assertEquals(topicFilter.endsWith("#"), topicIterator.hasMultiLevelWildcard());
    }

    @ParameterizedTest
    @CsvSource({
            "test, test/topic", "test/topic/filter, test/topic/filter/abc", "test/+/topic, test/+/topic/abc",
            "test/+/topic/#, test/+/topic/abc"
    })
    void forwardWhileEqual_tooShort(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final boolean hasMultiLevelWildcard = topicFilter.endsWith("#");
        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertEquals(
                hasMultiLevelWildcard ? topicFilter.length() - 2 : topicFilter.length(),
                topicIterator.forwardWhileEqual(levels));

        assertFalse(topicIterator.hasNext());
        assertEquals(hasMultiLevelWildcard, topicIterator.hasMultiLevelWildcard());
    }

    @ParameterizedTest
    @CsvSource({
            "test/topic/filter, test/topic/filte2, 10", "test/topic/filter, test/topic/filter2, 10",
            "test/topic/filter2, test/topic/filter, 10", "test/+/topic, test/+/topi2, 6",
            "test/+/topic, test/+/topic2, 6", "test/+/topic2, test/+/topic, 6", "test/+/topic/#, test/+/topi2, 6",
            "test/+/topic/#, test/+/topic2, 6", "test/+/topic2/#, test/+/topic, 6", "test/topic/+, test/topic/abc, 10"
    })
    void forwardWhileEqual_notFullyEqual(
            final @NotNull String topicFilter, final @NotNull String topicLevels, final int branchIndex) {

        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertEquals(branchIndex, topicIterator.forwardWhileEqual(levels));

        assertEquals(branchIndex, topicIterator.getStart());
        assertEquals(branchIndex, topicIterator.getEnd());
    }

    @ParameterizedTest
    @CsvSource(
            {"test/topic/filter, test/topic/filter", "test/topic/filter, test/+/filter", "test/topic/filter, test/+/+"})
    void forwardIfMatch_equalToEnd(final @NotNull String topic, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicImpl.of(topic));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertTrue(topicIterator.forwardIfMatch(levels));

        assertFalse(topicIterator.hasNext());
    }

    @ParameterizedTest
    @CsvSource({
            "test/topic/filter/abc, test/topic/filter", "test/topic/filter/abc, test/+/filter",
            "test/topic/filter/abc, test/+/+"
    })
    void forwardIfMatch_remainingLevels(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertTrue(topicIterator.forwardIfMatch(levels));

        assertTrue(topicIterator.hasNext());
        final MqttTopicLevel lastLevel = topicIterator.next();
        assertEquals(MqttTopicLevel.of("abc".getBytes(), 0, 3), lastLevel);
        assertFalse(lastLevel.isSingleLevelWildcard());
        assertFalse(topicIterator.hasNext());
    }

    @ParameterizedTest
    @CsvSource({
            "test/topic/filter, test/topic/filter/abc", "test/topic/filter, test/+/filter/abc",
            "test/topic/filter, test/+/+/abc", "test/topic/filter, test/+/filter/+"
    })
    void forwardIfMatch_tooShort(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();
        final int start = topicIterator.getStart();
        final int end = topicIterator.getEnd();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertFalse(topicIterator.forwardIfMatch(levels));

        assertEquals(start, topicIterator.getStart());
        assertEquals(end, topicIterator.getEnd());
    }

    @ParameterizedTest
    @CsvSource({
            "test, test/topic", "test/topic/filter, test/topic/filte2", "test/topic/filter, test/topic/filter2",
            "test/topic/filter2, test/topic/filter", "test/topic/filter, test/+/filte2",
            "test/topic/filter, test/+/filter2", "test/topic/filter2, test/+/filter"
    })
    void forwardIfMatch_notFullyEqual(final @NotNull String topicFilter, final @NotNull String topicLevels) {
        final MqttTopicIterator topicIterator = MqttTopicIterator.of(MqttTopicFilterImpl.of(topicFilter));
        topicIterator.next();
        final int start = topicIterator.getStart();
        final int end = topicIterator.getEnd();

        final MqttTopicLevels levels = createTopicLevels(topicLevels);
        assertFalse(topicIterator.forwardIfMatch(levels));

        assertEquals(start, topicIterator.getStart());
        assertEquals(end, topicIterator.getEnd());
    }

    private static @NotNull MqttTopicLevels createTopicLevels(final @NotNull String levels) {
        final byte[] bytes = levels.getBytes();
        final int firstEnd = levels.indexOf('/');
        return new MqttTopicLevels(bytes, (firstEnd == -1) ? bytes.length : firstEnd);
    }
}
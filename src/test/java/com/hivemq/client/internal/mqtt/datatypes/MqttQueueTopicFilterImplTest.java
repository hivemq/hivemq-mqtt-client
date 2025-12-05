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

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.util.collections.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jean-François Côté
 */
class MqttQueueTopicFilterImplTest {

    enum QueueTopicFilterSource {
        STRING,
        BYTE_BUF,
        TOPIC_FILTER
    }

    private @Nullable MqttTopicFilterImpl from(
            final @NotNull QueueTopicFilterSource source,
            final @NotNull String topicFilter) {

        if (source == QueueTopicFilterSource.TOPIC_FILTER) {
            return MqttQueueTopicFilterImpl.of(topicFilter);
        }

        final String queueTopicFilter = "$q/" + topicFilter;

        switch (source) {
            case BYTE_BUF:
                final ByteBuf byteBuf = Unpooled.buffer();
                final byte[] binary = queueTopicFilter.getBytes(StandardCharsets.UTF_8);
                byteBuf.writeShort(binary.length);
                byteBuf.writeBytes(binary);
                final MqttTopicFilterImpl mqtt5TopicFilter = MqttTopicFilterImpl.decode(byteBuf);
                byteBuf.release();
                return mqtt5TopicFilter;
            case STRING:
                return MqttTopicFilterImpl.of(queueTopicFilter);
            default:
                throw new IllegalStateException();
        }
    }

    private @Nullable MqttTopicFilterImpl fromFullString(
            final @NotNull QueueTopicFilterSource source, final @NotNull String fullQueueTopicFilter) {

        switch (source) {
            case BYTE_BUF:
                final ByteBuf byteBuf = Unpooled.buffer();
                final byte[] binary = fullQueueTopicFilter.getBytes(StandardCharsets.UTF_8);
                byteBuf.writeShort(binary.length);
                byteBuf.writeBytes(binary);
                final MqttTopicFilterImpl mqtt5TopicFilter = MqttTopicFilterImpl.decode(byteBuf);
                byteBuf.release();
                return mqtt5TopicFilter;
            case STRING:
                return MqttTopicFilterImpl.of(fullQueueTopicFilter);
            default:
                throw new IllegalStateException();
        }
    }

    @ParameterizedTest
    @EnumSource(QueueTopicFilterSource.class)
    void from_simple(final @NotNull QueueTopicFilterSource source) {
        final String topicFilter = "abc/def";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertInstanceOf(MqttQueueTopicFilterImpl.class, mqtt5TopicFilter);

        final MqttQueueTopicFilterImpl mqtt5QueueTopicFilter = (MqttQueueTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(topicFilter, mqtt5QueueTopicFilter.getTopicFilter().toString());
    }

    private static @NotNull List<Arguments> invalidQueueSubscriptions() {
        final List<Arguments> testSpecs = new LinkedList<>();
        // queueTopicFilter, message
        testSpecs.add(Arguments.of("$q/", "Topic filter must be at least one character long"));
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("invalidQueueSubscriptions")
    void from_invalidQueueSubscriptionByteBuf_returnsNull(
            final @NotNull String queueTopicFilter, @SuppressWarnings("unused") final @NotNull String message) {

        final MqttTopicFilterImpl mqtt5TopicFilter = fromFullString(QueueTopicFilterSource.BYTE_BUF, queueTopicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("invalidQueueSubscriptions")
    void from_invalidQueueSubscriptionString_throws(
            final @NotNull String queueTopicFilter, final @NotNull String message) {

        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> fromFullString(QueueTopicFilterSource.STRING, queueTopicFilter));
        assertTrue(exception.getMessage().contains(message), "IllegalArgumentException must give hint that " + message);
    }

    private static @NotNull List<Arguments> invalidTopicFilter(
            final @NotNull QueueTopicFilterSource source) {

        final List<Arguments> testSpecs = new LinkedList<>();
        // source, topicFilter, errorMsg
        testSpecs.add(Arguments.of(source, "abc/def/ghi/#/",
                "Topic filter [abc/def/ghi/#/] contains misplaced wildcard characters. " +
                        "Multi level wildcard (#) must be the last character."));
        testSpecs.add(Arguments.of(source, "abc/def/ghi#",
                "Topic filter [abc/def/ghi#] contains misplaced wildcard characters. " +
                        "Wildcard (#) at index 11 must follow a topic level separator."));
        testSpecs.add(Arguments.of(source, "abc+/def/ghi",
                "Topic filter [abc+/def/ghi] contains misplaced wildcard characters. " +
                        "Wildcard (+) at index 3 must follow a topic level separator."));
        testSpecs.add(Arguments.of(source, "abc/+def/ghi",
                "Topic filter [abc/+def/ghi] contains misplaced wildcard characters. " +
                        "Single level wildcard (+) at index 4 must be followed by a topic level separator."));
        testSpecs.add(Arguments.of(source, "abc/def/ghi/+#",
                "Topic filter [abc/def/ghi/+#] contains misplaced wildcard characters. " +
                        "Single level wildcard (+) at index 12 must be followed by a topic level separator."));
        testSpecs.add(Arguments.of(source, "abc/++/def/ghi",
                "Topic filter [abc/++/def/ghi] contains misplaced wildcard characters. " +
                        "Single level wildcard (+) at index 4 must be followed by a topic level separator."));
        testSpecs.add(Arguments.of(source, "", "Topic filter must be at least one character long"));
        return testSpecs;
    }

    private static @NotNull List<Arguments> invalidTopicFilterFromStringAndFromTopicFilter() {
        final List<Arguments> testSpecs = invalidTopicFilter(QueueTopicFilterSource.STRING);
        testSpecs.addAll(invalidTopicFilter(QueueTopicFilterSource.TOPIC_FILTER));
        return testSpecs;
    }

    private static @NotNull List<Arguments> invalidTopicFilterFromByteBuf() {
        return invalidTopicFilter(QueueTopicFilterSource.BYTE_BUF);
    }

    @ParameterizedTest
    @MethodSource("invalidTopicFilterFromByteBuf")
    void from_invalidTopicFilterByteBuf_returnsNull(
            final @NotNull QueueTopicFilterSource source,
            final @NotNull String topicFilter,
            @SuppressWarnings("unused") final @NotNull String message) {

        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("invalidTopicFilterFromStringAndFromTopicFilter")
    void from_invalidTopicFilterString_throws(
            final @NotNull QueueTopicFilterSource source,
            final @NotNull String topicFilter,
            final @NotNull String message) {

        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> from(source, topicFilter));
        assertTrue(exception.getMessage().contains(message), "IllegalArgumentException must give hint that " + message);
    }

    @Test
    void test_queue_topic_filter_must_be_case_sensitive() {
        final String topicFilter1 = "abc";
        final String topicFilter2 = "ABC";
        final MqttQueueTopicFilterImpl mqtt5TopicFilter1 = MqttQueueTopicFilterImpl.of(topicFilter1);
        final MqttQueueTopicFilterImpl mqtt5TopicFilter2 = MqttQueueTopicFilterImpl.of(topicFilter2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertNotEquals(mqtt5TopicFilter1, mqtt5TopicFilter2);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());

        assertEquals(topicFilter1, mqtt5TopicFilter1.getTopicFilter().toString());
        assertEquals(topicFilter2, mqtt5TopicFilter2.getTopicFilter().toString());
    }

    private static @NotNull List<Arguments> validTopicFilter() {
        final List<Arguments> testSpecs = new LinkedList<>();
        for (final QueueTopicFilterSource source : QueueTopicFilterSource.values()) {
            // source, testDescription, topicFilter
            testSpecs.add(Arguments.of(source, "topic filter with space", "ab c/def"));
            testSpecs.add(Arguments.of(source, "topic filter is single space", " "));
            testSpecs.add(Arguments.of(source, "topic filter contains multi level wildcard", "abc/def/ghi/#"));
            testSpecs.add(Arguments.of(source, "topic filter is multi level wildcard", "#"));
            testSpecs.add(Arguments.of(source, "topic filter with single level wildcard", "abc/+/def/ghi"));
            testSpecs.add(Arguments.of(source, "topic filter is single level wildcard", "+"));
            testSpecs.add(Arguments.of(source, "topic filter with multiple single level wildcards",
                    "+/abc/+/def/+/+/ghi/+"));
            testSpecs.add(Arguments.of(source, "topic filter with multi and single level wildcards",
                    "abc/+/def/+/ghi/#"));
        }
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("validTopicFilter")
    void from_validTopicFilter(
            final @NotNull QueueTopicFilterSource source,
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String topicFilter) {

        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertInstanceOf(MqttQueueTopicFilterImpl.class, mqtt5TopicFilter);

        final MqttQueueTopicFilterImpl mqtt5QueueTopicFilter = (MqttQueueTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(topicFilter, mqtt5QueueTopicFilter.getTopicFilter().toString());
    }

    @ParameterizedTest
    @EnumSource(QueueTopicFilterSource.class)
    void getLevels_onlyTopicLevelSeparator(final @NotNull QueueTopicFilterSource source) {
        final String topicFilter = "/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertInstanceOf(MqttQueueTopicFilterImpl.class, mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("", ""));

        final MqttQueueTopicFilterImpl mqtt5QueueTopicFilter = (MqttQueueTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(topicFilter, mqtt5QueueTopicFilter.getTopicFilter().toString());
    }

    @ParameterizedTest
    @EnumSource(QueueTopicFilterSource.class)
    void getLevels_simple(final @NotNull QueueTopicFilterSource source) {
        final String topicFilter = "abc/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertInstanceOf(MqttQueueTopicFilterImpl.class, mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("abc", "def", "ghi"));
    }

    @ParameterizedTest
    @EnumSource(QueueTopicFilterSource.class)
    void getLevels_multipleEmptyLevels(final @NotNull QueueTopicFilterSource source) {
        final String topicFilter = "/abc//def///ghi/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertInstanceOf(MqttQueueTopicFilterImpl.class, mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("", "abc", "", "def", "", "", "ghi", ""));
    }
}
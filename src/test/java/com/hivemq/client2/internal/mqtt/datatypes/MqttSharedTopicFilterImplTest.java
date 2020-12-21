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

import com.hivemq.client2.internal.util.collections.ImmutableList;
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
 * @author Silvio Giebl
 */
class MqttSharedTopicFilterImplTest {

    enum SharedTopicFilterSource {
        STRING,
        BYTE_BUF,
        SHARE_NAME_AND_TOPIC_FILTER
    }

    private @Nullable MqttTopicFilterImpl from(
            final @NotNull SharedTopicFilterSource source,
            final @NotNull String shareName,
            final @NotNull String topicFilter) {

        if (source == SharedTopicFilterSource.SHARE_NAME_AND_TOPIC_FILTER) {
            return MqttSharedTopicFilterImpl.of(shareName, topicFilter);
        } else {
            return from(source, "$share/" + shareName + "/" + topicFilter);
        }
    }

    private @Nullable MqttTopicFilterImpl from(
            final @NotNull SharedTopicFilterSource source, final @NotNull String sharedSubscriptionTopicFilter) {

        switch (source) {
            case BYTE_BUF:
                final ByteBuf byteBuf = Unpooled.buffer();
                final byte[] binary = sharedSubscriptionTopicFilter.getBytes(StandardCharsets.UTF_8);
                byteBuf.writeShort(binary.length);
                byteBuf.writeBytes(binary);
                final MqttTopicFilterImpl mqtt5TopicFilter = MqttTopicFilterImpl.decode(byteBuf);
                byteBuf.release();
                return mqtt5TopicFilter;
            case STRING:
                return MqttTopicFilterImpl.of(sharedSubscriptionTopicFilter);
        }
        throw new IllegalStateException();
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    void from_simple(final @NotNull SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "abc/def";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter = (MqttSharedTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter().toString());
    }

    private static @NotNull List<Arguments> invalidSharedSubscriptions() {
        final List<Arguments> testSpecs = new LinkedList<>();
        // sharedTopicFilter, message
        testSpecs.add(Arguments.of("$share/sharename", "Topic filter must be at least one character long"));
        testSpecs.add(Arguments.of("$share/", "Share name must be at least one character long"));
        testSpecs.add(
                Arguments.of("$share/share#name", "Share name [share#name] must not contain multi level wildcard (#)"));
        testSpecs.add(Arguments.of("$share/share+name",
                "Share name [share+name] must not contain single level wildcard (+)"));
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("invalidSharedSubscriptions")
    void from_invalidSharedSubscriptionByteBuf_returnsNull(
            final @NotNull String sharedTopicFilter, @SuppressWarnings("unused") final @NotNull String message) {

        final MqttTopicFilterImpl mqtt5TopicFilter = from(SharedTopicFilterSource.BYTE_BUF, sharedTopicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("invalidSharedSubscriptions")
    void from_invalidSharedSubscriptionString_throws(
            final @NotNull String sharedTopicFilter, final @NotNull String message) {

        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> from(SharedTopicFilterSource.STRING, sharedTopicFilter));
        assertTrue(exception.getMessage().contains(message), "IllegalArgumentException must give hint that " + message);
    }

    private static @NotNull List<Arguments> invalidShareNameOrTopicFilter(
            final @NotNull SharedTopicFilterSource source) {

        final List<Arguments> testSpecs = new LinkedList<>();
        // source, testDescription, shareName, topicFilter, errorMsg
        testSpecs.add(Arguments.of(source, "group", "abc/def/ghi/#/",
                "Topic filter [abc/def/ghi/#/] contains misplaced wildcard characters. " +
                        "Multi level wildcard (#) must be the last character."));
        testSpecs.add(Arguments.of(source, "group", "abc/def/ghi#",
                "Topic filter [abc/def/ghi#] contains misplaced wildcard characters. " +
                        "Wildcard (#) at index 11 must follow a topic level separator."));
        testSpecs.add(Arguments.of(source, "group", "abc+/def/ghi",
                "Topic filter [abc+/def/ghi] contains misplaced wildcard characters. " +
                        "Wildcard (+) at index 3 must follow a topic level separator."));
        testSpecs.add(Arguments.of(source, "group", "abc/+def/ghi",
                "Topic filter [abc/+def/ghi] contains misplaced wildcard characters. " +
                        "Single level wildcard (+) at index 4 must be followed by a topic level separator."));
        testSpecs.add(Arguments.of(source, "group", "abc/def/ghi/+#",
                "Topic filter [abc/def/ghi/+#] contains misplaced wildcard characters. " +
                        "Single level wildcard (+) at index 12 must be followed by a topic level separator."));
        testSpecs.add(Arguments.of(source, "group", "abc/++/def/ghi",
                "Topic filter [abc/++/def/ghi] contains misplaced wildcard characters. " +
                        "Single level wildcard (+) at index 4 must be followed by a topic level separator."));
        testSpecs.add(Arguments.of(source, "group", "", "Topic filter must be at least one character long"));
        testSpecs.add(Arguments.of(source, "", "abc/def", "Share name must be at least one character long"));
        testSpecs.add(
                Arguments.of(source, "group#", "abc/def", "Share name [group#] must not contain multi level wildcard"));
        testSpecs.add(
                Arguments.of(source, "#group", "abc/def", "Share name [#group] must not contain multi level wildcard"));
        testSpecs.add(Arguments.of(source, "group+", "abc/def",
                "Share name [group+] must not contain single level wildcard"));
        testSpecs.add(Arguments.of(source, "+group", "abc/def",
                "Share name [+group] must not contain single level wildcard"));
        return testSpecs;
    }

    private static @NotNull List<Arguments> invalidShareNameOrTopicFilterFromStringAndFromShareNameAndTopicFilter() {
        final List<Arguments> testSpecs = invalidShareNameOrTopicFilter(SharedTopicFilterSource.STRING);
        testSpecs.addAll(invalidShareNameOrTopicFilter(SharedTopicFilterSource.SHARE_NAME_AND_TOPIC_FILTER));
        return testSpecs;
    }

    private static @NotNull List<Arguments> invalidShareNameOrTopicFilterFromByteBuf() {
        return invalidShareNameOrTopicFilter(SharedTopicFilterSource.BYTE_BUF);
    }

    @ParameterizedTest
    @MethodSource("invalidShareNameOrTopicFilterFromByteBuf")
    void from_invalidShareNameOrTopicFilterByteBuf_returnsNull(
            final @NotNull SharedTopicFilterSource source,
            final @NotNull String shareName,
            final @NotNull String topicFilter,
            @SuppressWarnings("unused") final @NotNull String message) {

        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("invalidShareNameOrTopicFilterFromStringAndFromShareNameAndTopicFilter")
    void from_invalidShareNameOrTopicFilterString_throws(
            final @NotNull SharedTopicFilterSource source,
            final @NotNull String shareName,
            final @NotNull String topicFilter,
            final @NotNull String message) {

        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> from(source, shareName, topicFilter));
        assertTrue(exception.getMessage().contains(message), "IllegalArgumentException must give hint that " + message);
    }

    @Test
    void from_shareNameWithTopicLevelSeparator_throws() {
        final String shareName = "gro/up";
        final String topicFilter = "abc/def/ghi";
        final String errorMsg = "Share name [gro/up] must not contain topic level separator";
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> from(SharedTopicFilterSource.SHARE_NAME_AND_TOPIC_FILTER, shareName, topicFilter));
        assertTrue(exception.getMessage().contains(errorMsg),
                "IllegalArgumentException must give hint that " + errorMsg);
    }

    @Test
    void test_shared_topic_filter_must_be_case_sensitive() {
        final String shareName = "group";
        final String topicFilter1 = "abc";
        final String topicFilter2 = "ABC";
        final MqttSharedTopicFilterImpl mqtt5TopicFilter1 = MqttSharedTopicFilterImpl.of(shareName, topicFilter1);
        final MqttSharedTopicFilterImpl mqtt5TopicFilter2 = MqttSharedTopicFilterImpl.of(shareName, topicFilter2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertTrue(mqtt5TopicFilter1.isShared());
        assertTrue(mqtt5TopicFilter2.isShared());
        assertNotEquals(mqtt5TopicFilter1, mqtt5TopicFilter2);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());

        assertEquals(shareName, mqtt5TopicFilter1.getShareName());
        assertEquals(shareName, mqtt5TopicFilter2.getShareName());
        assertEquals(topicFilter1, mqtt5TopicFilter1.getTopicFilter().toString());
        assertEquals(topicFilter2, mqtt5TopicFilter2.getTopicFilter().toString());
    }

    private static @NotNull List<Arguments> validShareNameAndTopicFilter() {
        final List<Arguments> testSpecs = new LinkedList<>();
        for (final SharedTopicFilterSource source : SharedTopicFilterSource.values()) {
            // source, testDescription, shareName, topicFilter
            testSpecs.add(Arguments.of(source, "topic filter with space", "group", "ab c/def"));
            testSpecs.add(Arguments.of(source, "topic filter is single space", "group", " "));
            testSpecs.add(Arguments.of(source, "topic filter contains multi level wildcard", "group", "abc/def/ghi/#"));
            testSpecs.add(Arguments.of(source, "topic filter is multi level wildcard", "group", "#"));
            testSpecs.add(Arguments.of(source, "topic filter with single level wildcard", "group", "abc/+/def/ghi"));
            testSpecs.add(Arguments.of(source, "topic filter is single level wildcard", "group", "+"));
            testSpecs.add(Arguments.of(source, "topic filter with multiple single level wildcards", "group",
                    "+/abc/+/def/+/+/ghi/+"));
            testSpecs.add(Arguments.of(source, "topic filter with multi and single level wildcards", "group",
                    "abc/+/def/+/ghi/#"));
        }
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("validShareNameAndTopicFilter")
    void from_validShareNameAndTopicFilter(
            final @NotNull SharedTopicFilterSource source,
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String shareName,
            final @NotNull String topicFilter) {

        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter = (MqttSharedTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter().toString());
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    void getLevels_onlyTopicLevelSeparator(final @NotNull SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("", ""));

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter = (MqttSharedTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter().toString());
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    void getLevels_simple(final @NotNull SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "abc/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("abc", "def", "ghi"));
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    void getLevels_multipleEmptyLevels(final @NotNull SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "/abc//def///ghi/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("", "abc", "", "def", "", "", "ghi", ""));
    }
}
/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.mqtt.datatypes;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class MqttSharedTopicFilterImplTest {

    enum SharedTopicFilterSource {
        STRING,
        BYTE_BUF,
        SHARE_NAME_AND_TOPIC_FILTER
    }

    private MqttTopicFilterImpl from(final String shareName, final String topicFilter) {
        return from(SharedTopicFilterSource.STRING, shareName, topicFilter);
    }

    private MqttTopicFilterImpl from(
            final SharedTopicFilterSource source, final String shareName, final String topicFilter) {
        if (source == SharedTopicFilterSource.SHARE_NAME_AND_TOPIC_FILTER) {
            return MqttSharedTopicFilterImpl.from(shareName, topicFilter);
        } else {
            final String sharedSubscriptionTopicFilter = "$share/" + shareName + "/" + topicFilter;
            return from(source, sharedSubscriptionTopicFilter);
        }
    }

    private MqttTopicFilterImpl from(final SharedTopicFilterSource source, final String sharedSubscriptionTopicFilter) {
        switch (source) {
            case BYTE_BUF:
                final ByteBuf byteBuf = Unpooled.buffer();
                final byte[] binary = sharedSubscriptionTopicFilter.getBytes(Charset.forName("UTF-8"));
                byteBuf.writeShort(binary.length);
                byteBuf.writeBytes(binary);
                final MqttTopicFilterImpl mqtt5TopicFilter = MqttTopicFilterImpl.from(byteBuf);
                byteBuf.release();
                return mqtt5TopicFilter;
            case STRING:
                return MqttTopicFilterImpl.from(sharedSubscriptionTopicFilter);
        }
        throw new IllegalStateException();
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    public void from_simple(SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "abc/def";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter = (MqttSharedTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    private static List<Arguments> invalidSharedSubscriptions(final SharedTopicFilterSource source) {
        final List<Arguments> testSpecs = new LinkedList<>();
        // source, testDescription, sharedSubscriptionTopicFilter, errorMsg
        testSpecs.add(
                Arguments.of(source, "missing topic filter", "$share/sharename", "Topic Filter must not be empty"));
        testSpecs.add(Arguments.of(source, "missing share name", "$share/", "Share Name must not be empty"));
        testSpecs.add(Arguments.of(source, "share name contains multi level wildcard", "$share/share#name",
                "Share Name must not contain multi level wildcard"));
        testSpecs.add(Arguments.of(source, "share name contains single level wildcard", "$share/share+name",
                "Share Name must not contain single level wildcard"));
        return testSpecs;
    }

    private static List<Arguments> invalidSharedSubscriptionsFromByteBuf() {
        return invalidSharedSubscriptions(SharedTopicFilterSource.BYTE_BUF);
    }

    private static List<Arguments> invalidSharedSubscriptionsFromString() {
        return invalidSharedSubscriptions(SharedTopicFilterSource.STRING);
    }

    @ParameterizedTest
    @MethodSource("invalidSharedSubscriptionsFromByteBuf")
    public void from_invalidSharedSubscriptionByteBuf_returnsNull(
            final SharedTopicFilterSource source, @SuppressWarnings("unused") final String testDescription,
            final String sharedSubscriptionTopicFilter) {
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, sharedSubscriptionTopicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("invalidSharedSubscriptionsFromString")
    public void from_invalidSharedSubscriptionString_throws(
            final SharedTopicFilterSource source, @SuppressWarnings("unused") final String testDescription,
            final String sharedSubscriptionTopicFilter, final String errorMsg) {
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> from(source, sharedSubscriptionTopicFilter));
        assertTrue("IllegalArgumentException must give hint that " + errorMsg,
                exception.getMessage().contains(errorMsg));
    }

    private static List<Arguments> invalidShareNameOrTopicFilter(SharedTopicFilterSource source) {
        final List<Arguments> testSpecs = new LinkedList<>();
        // source, testDescription, shareName, topicFilter, errorMsg
        testSpecs.add(Arguments.of(source, "empty topic filter", "group", "", "Topic Filter must not be empty"));
        testSpecs.add(
                Arguments.of(source, "topic filter with multi level wildcard not at end", "group", "abc/def/ghi/#/",
                        "misplaced wildcard characters"));
        testSpecs.add(Arguments.of(source, "topic filter with multi level wildcard after non separator", "group",
                "abc/def/ghi#", "misplaced wildcard characters"));
        testSpecs.add(Arguments.of(source, "topic filter with double single level wildcard", "group", "abc/++/def/ghi",
                "misplaced wildcard characters"));
        testSpecs.add(Arguments.of(source, "topic filter with single level wildcard after non separator", "group",
                "abc+/def/ghi", "misplaced wildcard characters"));
        testSpecs.add(Arguments.of(source, "topic filter with single level wildcard before non separator", "group",
                "abc/+def/ghi", "misplaced wildcard characters"));
        testSpecs.add(Arguments.of(source, "topic filter with single level before multi level wildcard", "group",
                "abc/def/ghi/+#", "misplaced wildcard characters"));
        testSpecs.add(Arguments.of(source, "empty share name", "", "abc/def", "Share Name must not be empty"));
        testSpecs.add(Arguments.of(source, "share name with multi level wildcard at end", "group#", "abc/def",
                "Share Name must not contain multi level wildcard"));
        testSpecs.add(Arguments.of(source, "share name with multi level wildcard at start", "#group", "abc/def",
                "Share Name must not contain multi level wildcard"));
        testSpecs.add(Arguments.of(source, "share name with single level wildcard at end", "group+", "abc/def",
                "Share Name must not contain single level wildcard"));
        testSpecs.add(Arguments.of(source, "share name with single level wildcard at start", "+group", "abc/def",
                "Share Name must not contain single level wildcard"));
        return testSpecs;
    }

    private static List<Arguments> invalidShareNameOrTopicFilterFromStringAndFromShareNameAndTopicFilter() {
        final List<Arguments> testSpecs = invalidShareNameOrTopicFilter(SharedTopicFilterSource.STRING);
        testSpecs.addAll(invalidShareNameOrTopicFilter(SharedTopicFilterSource.SHARE_NAME_AND_TOPIC_FILTER));
        return testSpecs;
    }

    private static List<Arguments> invalidShareNameOrTopicFilterFromByteBuf() {
        return invalidShareNameOrTopicFilter(SharedTopicFilterSource.BYTE_BUF);
    }

    @ParameterizedTest
    @MethodSource("invalidShareNameOrTopicFilterFromByteBuf")
    public void from_invalidShareNameOrTopicFilterByteBuf_returnsNull(
            final SharedTopicFilterSource source, @SuppressWarnings("unused") final String testDescription,
            final String shareName, final String topicFilter) {
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("invalidShareNameOrTopicFilterFromStringAndFromShareNameAndTopicFilter")
    public void from_invalidShareNameOrTopicFilterString_throws(
            final SharedTopicFilterSource source, @SuppressWarnings("unused") final String testDescription,
            final String shareName, final String topicFilter, final String errorMsg) {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> from(source, shareName, topicFilter));
        assertTrue("IllegalArgumentException must give hint that " + errorMsg,
                exception.getMessage().contains(errorMsg));
    }

    @Test
    public void from_shareNameWithTopicLevelSeparator_throws() {
        final String shareName = "gro/up";
        final String topicFilter = "abc/def/ghi";
        final String errorMsg = "Share Name must not contain topic level separator";
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> from(SharedTopicFilterSource.SHARE_NAME_AND_TOPIC_FILTER, shareName, topicFilter));
        assertTrue("IllegalArgumentException must give hint that " + errorMsg,
                exception.getMessage().contains(errorMsg));
    }

    @Test
    public void test_shared_topic_filter_must_be_case_sensitive() {
        final String shareName = "group";
        final String topicFilter1 = "abc";
        final String topicFilter2 = "ABC";
        final MqttTopicFilterImpl mqtt5TopicFilter1 = from(shareName, topicFilter1);
        final MqttTopicFilterImpl mqtt5TopicFilter2 = from(shareName, topicFilter2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertTrue(mqtt5TopicFilter1.isShared());
        assertTrue(mqtt5TopicFilter1 instanceof MqttSharedTopicFilterImpl);
        assertTrue(mqtt5TopicFilter2.isShared());
        assertTrue(mqtt5TopicFilter2 instanceof MqttSharedTopicFilterImpl);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter1 = (MqttSharedTopicFilterImpl) mqtt5TopicFilter1;
        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter2 = (MqttSharedTopicFilterImpl) mqtt5TopicFilter2;
        assertEquals(shareName, mqtt5SharedTopicFilter1.getShareName());
        assertEquals(shareName, mqtt5SharedTopicFilter2.getShareName());
        assertEquals(topicFilter1, mqtt5SharedTopicFilter1.getTopicFilter());
        assertEquals(topicFilter2, mqtt5SharedTopicFilter2.getTopicFilter());
    }

    private static List<Arguments> validShareNameAndTopicFilter() {
        final List<Arguments> testSpecs = new LinkedList<>();
        for (SharedTopicFilterSource source : SharedTopicFilterSource.values()) {
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
    public void from_validShareNameAndTopicFilter(
            final SharedTopicFilterSource source, @SuppressWarnings("unused") final String testDescription,
            final String shareName, final String topicFilter) {
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter = (MqttSharedTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    public void getLevels_onlyTopicLevelSeparator(final SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertThat(levels, CoreMatchers.is(Arrays.asList("", "")));

        final MqttSharedTopicFilterImpl mqtt5SharedTopicFilter = (MqttSharedTopicFilterImpl) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    public void getLevels_simple(final SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "abc/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertThat(levels, CoreMatchers.is(Arrays.asList("abc", "def", "ghi")));
    }

    @ParameterizedTest
    @EnumSource(SharedTopicFilterSource.class)
    public void getLevels_multipleEmptyLevels(final SharedTopicFilterSource source) {
        final String shareName = "group";
        final String topicFilter = "/abc//def///ghi/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(source, shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertThat(levels, CoreMatchers.is(Arrays.asList("", "abc", "", "def", "", "", "ghi", "")));
    }

}
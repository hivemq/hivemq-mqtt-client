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
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 * @author Christian Hoff
 */
class MqttTopicFilterImplTest {

    private static final @NotNull List<Function<String, MqttTopicFilterImpl>> topicFilterFactoryMethods =
            Arrays.asList(new NamedFunction<>("ByteBuf", MqttTopicFilterImplTest::createFromByteBuf),
                    new NamedFunction<>("String", MqttTopicFilterImpl::of));

    private static @Nullable MqttTopicFilterImpl createFromByteBuf(final @NotNull String string) {
        final ByteBuf byteBuf = Unpooled.buffer();
        final byte[] binary = string.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(binary.length);
        byteBuf.writeBytes(binary);
        final MqttTopicFilterImpl mqtt5TopicFilter = MqttTopicFilterImpl.decode(byteBuf);
        byteBuf.release();
        return mqtt5TopicFilter;
    }

    @Test
    void from_emptyByteBuf_returnsNull() {
        final String string = "";
        final MqttTopicFilterImpl mqtt5TopicFilter = createFromByteBuf(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    void from_emptyString_throws() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicFilterImpl.of(""));
        assertTrue(
                exception.getMessage().contains("must be at least one character long"),
                "IllegalArgumentException must give hint that string must not be empty.");
    }

    private static @NotNull List<Function<String, MqttTopicFilterImpl>> topicFilterFactoryMethodProvider() {
        return topicFilterFactoryMethods;
    }

    @ParameterizedTest
    @MethodSource("topicFilterFactoryMethodProvider")
    void from_mustBeCaseSensitive(final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod) {
        final String string1 = "abc";
        final String string2 = "ABC";
        final MqttTopicFilterImpl mqtt5TopicFilter1 = topicFilterFactoryMethod.apply(string1);
        final MqttTopicFilterImpl mqtt5TopicFilter2 = topicFilterFactoryMethod.apply(string2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());
    }

    private static @NotNull List<Arguments> validTopicFilterProvider() {
        final List<Arguments> testSpecs = new LinkedList<>();
        for (final Function<String, MqttTopicFilterImpl> method : topicFilterFactoryMethods) {
            testSpecs.add(Arguments.of(method, "containing space", "ab c/def"));
            testSpecs.add(Arguments.of(method, "containing single space only", " "));
            testSpecs.add(Arguments.of(method, "containing multi level wildcard at end", "abc/def/ghi/#"));
            testSpecs.add(Arguments.of(method, "containing multi level wildcard only", "#"));
            testSpecs.add(Arguments.of(method, "containing single level wildcard", "abc/+/def/ghi"));
            testSpecs.add(Arguments.of(method, "containing single level wildcard only", "+"));
            testSpecs.add(Arguments.of(method, "containing single and multi level wildcards", "abc/+/def/+/ghi/#"));
            testSpecs.add(Arguments.of(method, "containing multiple single level wildcards", "+/abc/+/def/+/+/ghi/+"));
        }
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("validTopicFilterProvider")
    void from(
            final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod,
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String topicFilterString) {
        final MqttTopicFilterImpl mqtt5TopicFilter = topicFilterFactoryMethod.apply(topicFilterString);
        assertNotNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("topicFilterFactoryMethodProvider")
    void from_topicLevelSeparatorOnly(final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod) {
        final String string = "/";
        final MqttTopicFilterImpl mqtt5TopicFilter = topicFilterFactoryMethod.apply(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));
    }

    private static @NotNull Stream<Arguments> misplacedWildcardsTopicFilterProvider() {
        return Stream.of(Arguments.of("multilevel wildcard not at end", "abc/def/ghi/#/"),
                Arguments.of("multilevel wildcard after non separator", "abc/def/ghi#"),
                Arguments.of("multiple single level wildcards one level", "abc/++/def/ghi"),
                Arguments.of("single level wildcard after non separator", "abc+/def/ghi"),
                Arguments.of("single level wildcard before non separator", "abc/+def/ghi"),
                Arguments.of("single level before multi level wildcard", "abc/def/ghi/+#"));
    }

    @ParameterizedTest
    @MethodSource("misplacedWildcardsTopicFilterProvider")
    void from_byteBufWithMisplacedWildcards_returnsNull(
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String topicFilterString) {
        final MqttTopicFilterImpl mqtt5TopicFilter = createFromByteBuf(topicFilterString);
        assertNull(mqtt5TopicFilter);
    }

    @ParameterizedTest
    @MethodSource("misplacedWildcardsTopicFilterProvider")
    void from_stringWithMisplacedWildcards_throws(
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String topicFilterString) {
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> MqttTopicFilterImpl.of(topicFilterString));
        assertTrue(
                exception.getMessage().contains("misplaced wildcard characters"),
                "IllegalArgumentException must give hint that string contains misplaced wildcard characters.");
    }

    @ParameterizedTest
    @MethodSource("topicFilterFactoryMethodProvider")
    void getLevels_simple(final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod) {
        final String string = "abc/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = topicFilterFactoryMethod.apply(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("abc", "def", "ghi"));
    }

    @ParameterizedTest
    @MethodSource("topicFilterFactoryMethodProvider")
    void getLevels_emptyLevels(final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod) {
        final String string = "/abc//def///ghi/";
        final MqttTopicFilterImpl mqtt5TopicFilter = topicFilterFactoryMethod.apply(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(levels, Arrays.asList("", "abc", "", "def", "", "", "ghi", ""));
    }

    private static @NotNull List<Arguments> wildcardTopicFilterProvider() {
        final List<Arguments> testSpecs = new LinkedList<>();
        for (final Function<String, MqttTopicFilterImpl> method : topicFilterFactoryMethods) {
            testSpecs.add(Arguments.of(method, "contains no wildcards", "abc/def/ghi", false, false, false));
            testSpecs.add(Arguments.of(method, "contains multi level wildcard", "abc/def/ghi/#", true, true, false));
            testSpecs.add(Arguments.of(method, "contains single level wildcard", "abc/+/def/ghi", true, false, true));
            testSpecs.add(
                    Arguments.of(method, "contains multi and single level wildcard", "abc/+/def/ghi/#", true, true,
                            true));
        }
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("wildcardTopicFilterProvider")
    void containsWildcards(
            final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod,
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String topicFilterString,
            final boolean containsWildcards,
            final boolean containsMultiLevelWildcard,
            final boolean containsSingleLevelWildcard) {
        final MqttTopicFilterImpl mqtt5TopicFilter = topicFilterFactoryMethod.apply(topicFilterString);
        assertNotNull(mqtt5TopicFilter);
        assertEquals(containsWildcards, mqtt5TopicFilter.containsWildcards());
        assertEquals(containsMultiLevelWildcard, mqtt5TopicFilter.containsMultiLevelWildcard());
        assertEquals(containsSingleLevelWildcard, mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    private static @NotNull List<Arguments> invalidSharedTopicFilterProvider() {
        final List<Arguments> testSpecs = new LinkedList<>();
        for (final Function<String, MqttTopicFilterImpl> method : topicFilterFactoryMethods) {
            testSpecs.add(Arguments.of(method, "$shared is valid topic prefix", "$shared/group/abc/def"));
            testSpecs.add(Arguments.of(method, "just $share does not define a shared topic", "$share"));
        }
        return testSpecs;
    }

    @ParameterizedTest
    @MethodSource("invalidSharedTopicFilterProvider")
    void isShared_false(
            final @NotNull Function<String, MqttTopicFilterImpl> topicFilterFactoryMethod,
            @SuppressWarnings("unused") final @NotNull String testDescription,
            final @NotNull String topicFilterString) {
        final MqttTopicFilterImpl mqtt5TopicFilter = topicFilterFactoryMethod.apply(topicFilterString);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.isShared());
        assertFalse(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
    }

    /**
     * Extension of Function&lt;T, R&gt; used to make test results more readable.
     */
    static class NamedFunction<T, R> implements Function<T, R> {

        private final @NotNull String name;
        private final @NotNull Function<T, R> function;

        NamedFunction(final @NotNull String name, final @NotNull Function<T, R> function) {
            this.name = name;
            this.function = function;
        }

        @Override
        public @Nullable R apply(final @NotNull T t) {
            return function.apply(t);
        }

        @Override
        public @NotNull String toString() {
            return name;
        }
    }
}
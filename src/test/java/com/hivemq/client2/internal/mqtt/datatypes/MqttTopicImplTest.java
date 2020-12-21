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
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 * @author Christian Hoff
 */
class MqttTopicImplTest {

    private static @NotNull Stream<Function<String, MqttTopicImpl>> topicFactoryMethodProvider() {
        return Stream.of(MqttTopicImplTest::createFromByteBuf, MqttTopicImpl::of);
    }

    private static @Nullable MqttTopicImpl createFromByteBuf(final @NotNull String string) {
        final ByteBuf byteBuf = Unpooled.buffer();
        final byte[] binary = string.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(binary.length);
        byteBuf.writeBytes(binary);
        final MqttTopicImpl mqtt5Topic = MqttTopicImpl.decode(byteBuf);
        byteBuf.release();
        return mqtt5Topic;
    }

    @Test
    void from_emptyByteBuf_returnsNull() {
        final String string = "";
        final MqttTopicImpl mqtt5Topic = createFromByteBuf(string);
        assertNull(mqtt5Topic);
    }

    @Test
    void from_emptyString_throws() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicImpl.of(""));
        assertTrue(
                exception.getMessage().contains("must be at least one character long"),
                "IllegalArgumentException must give hint that string must not be empty.");
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    void from_mustBeCaseSensitive(final @NotNull Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string1 = "abc";
        final String string2 = "ABC";
        final MqttTopicImpl mqtt5Topic1 = topicFactoryMethod.apply(string1);
        final MqttTopicImpl mqtt5Topic2 = topicFactoryMethod.apply(string2);
        assertNotNull(mqtt5Topic1);
        assertNotNull(mqtt5Topic2);
        assertNotEquals(mqtt5Topic1.toString(), mqtt5Topic2.toString());
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    void from_containsSpace(final @NotNull Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = "ab c/def";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    void from_singleSpaceOnly(final @NotNull Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = " ";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    void from_topicLevelSeparatorOnly(final @NotNull Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = "/";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
        final ImmutableList<String> levels = mqtt5Topic.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));
    }

    @Test
    void from_byteBufWithMultiLevelWildcard_returnsNull() {
        final String string = "abc/def/#";
        final MqttTopicImpl mqtt5Topic = createFromByteBuf(string);
        assertNull(mqtt5Topic);
    }

    @Test
    void from_stringWithMultiLevelWildcard_throws() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicImpl.of("abc/def/#"));
        assertTrue(
                exception.getMessage().contains("multi level wildcard"),
                "IllegalArgumentException must give hint that string contains a forbidden multi level wildcard character.");
    }

    @Test
    void from_byteBufWithSingleLevelWildcard_returnsNull() {
        final String string = "abc/+/def";
        final MqttTopicImpl mqtt5Topic = createFromByteBuf(string);
        assertNull(mqtt5Topic);
    }

    @Test
    void from_stringWithSingleLevelWildcard_throws() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicImpl.of("abc/+/def"));
        assertTrue(
                exception.getMessage().contains("single level wildcard"),
                "IllegalArgumentException must give hint that string contains a forbidden single level wildcard character.");
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    void getLevels_simple(final @NotNull Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = "abc/def/ghi";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
        final ImmutableList<String> levels = mqtt5Topic.getLevels();
        assertEquals(3, levels.size());
        assertEquals("abc", levels.get(0));
        assertEquals("def", levels.get(1));
        assertEquals("ghi", levels.get(2));
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    void getLevels_emptyLevels(final @NotNull Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = "/abc//def///ghi/";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
        final ImmutableList<String> levels = mqtt5Topic.getLevels();
        assertEquals(8, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("abc", levels.get(1));
        assertEquals("", levels.get(2));
        assertEquals("def", levels.get(3));
        assertEquals("", levels.get(4));
        assertEquals("", levels.get(5));
        assertEquals("ghi", levels.get(6));
        assertEquals("", levels.get(7));
    }
}
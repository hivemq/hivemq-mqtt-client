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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 * @author Christian Hoff
 */
public class MqttTopicImplTest {

    private static Stream<Function<String, MqttTopicImpl>> topicFactoryMethodProvider() {
        return Stream.of(MqttTopicImplTest::createFromByteBuf, MqttTopicImpl::from);
    }

    private static MqttTopicImpl createFromByteBuf(final String string) {
        final ByteBuf byteBuf = Unpooled.buffer();
        final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
        byteBuf.writeShort(binary.length);
        byteBuf.writeBytes(binary);
        final MqttTopicImpl mqtt5Topic = MqttTopicImpl.from(byteBuf);
        byteBuf.release();
        return mqtt5Topic;
    }

    @Test
    public void from_emptyByteBuf_returnsNull() {
        final String string = "";
        final MqttTopicImpl mqtt5Topic = createFromByteBuf(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void from_emptyString_throws() {
        IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicImpl.from(""));
        assertTrue(
                "IllegalArgumentException must give hint that string must not be empty.",
                exception.getMessage().contains("must not be empty"));
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    public void from_mustBeCaseSensitive(Function<String, MqttTopicImpl> topicFactoryMethod) {
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
    public void from_containsSpace(Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = "ab c/def";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    public void from_singleSpaceOnly(Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = " ";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    public void from_topicLevelSeparatorOnly(Function<String, MqttTopicImpl> topicFactoryMethod) {
        final String string = "/";
        final MqttTopicImpl mqtt5Topic = topicFactoryMethod.apply(string);
        assertNotNull(mqtt5Topic);
        final ImmutableList<String> levels = mqtt5Topic.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));
    }

    @Test
    public void from_byteBufWithMultiLevelWildcard_returnsNull() {
        final String string = "abc/def/#";
        final MqttTopicImpl mqtt5Topic = createFromByteBuf(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void from_stringWithMultiLevelWildcard_throws() {
        IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicImpl.from("abc/def/#"));
        assertTrue(
                "IllegalArgumentException must give hint that string contains a forbidden multi level wildcard character.",
                exception.getMessage().contains("multi level wildcard"));
    }

    @Test
    public void from_byteBufWithSingleLevelWildcard_returnsNull() {
        final String string = "abc/+/def";
        final MqttTopicImpl mqtt5Topic = createFromByteBuf(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void from_stringWithSingleLevelWildcard_throws() {
        IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class, () -> MqttTopicImpl.from("abc/+/def"));
        assertTrue(
                "IllegalArgumentException must give hint that string contains a forbidden single level wildcard character.",
                exception.getMessage().contains("single level wildcard"));
    }

    @ParameterizedTest
    @MethodSource("topicFactoryMethodProvider")
    public void getLevels_simple(Function<String, MqttTopicImpl> topicFactoryMethod) {
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
    public void getLevels_emptyLevels(Function<String, MqttTopicImpl> topicFactoryMethod) {
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
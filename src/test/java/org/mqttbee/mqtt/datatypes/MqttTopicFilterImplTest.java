/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.datatypes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.Charset;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
@RunWith(Parameterized.class)
public class MqttTopicFilterImplTest {

    @Parameterized.Parameters
    public static Collection<Boolean> parameters() {
        return ImmutableSet.of(false, true);
    }

    private final boolean isFromByteBuf;

    public MqttTopicFilterImplTest(final boolean isFromByteBuf) {
        this.isFromByteBuf = isFromByteBuf;
    }

    private MqttTopicFilterImpl from(final String string) {
        if (isFromByteBuf) {
            final ByteBuf byteBuf = Unpooled.buffer();
            final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
            byteBuf.writeShort(binary.length);
            byteBuf.writeBytes(binary);
            final MqttTopicFilterImpl mqtt5TopicFilter = MqttTopicFilterImpl.from(byteBuf);
            byteBuf.release();
            return mqtt5TopicFilter;
        } else {
            return MqttTopicFilterImpl.from(string);
        }
    }

    @Test
    public void test_must_not_be_zero_length() {
        final String string = "";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_be_case_sensitive() {
        final String string1 = "abc";
        final String string2 = "ABC";
        final MqttTopicFilterImpl mqtt5TopicFilter1 = from(string1);
        final MqttTopicFilterImpl mqtt5TopicFilter2 = from(string2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());
    }

    @Test
    public void test_can_contain_space() {
        final String string = "ab c/def";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_space() {
        final String string = " ";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_topic_level_separator() {
        final String string = "/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));
    }

    @Test
    public void test_can_contain_multi_level_wildcard() {
        final String string = "abc/def/ghi/#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_multi_level_wildcard() {
        final String string = "#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_multi_level_wildcard_not_at_end() {
        final String string = "abc/def/ghi/#/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_multi_level_wildcard_after_non_separator() {
        final String string = "abc/def/ghi#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_contain_single_level_wildcard() {
        final String string = "abc/+/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_single_level_wildcard() {
        final String string = "+";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_contain_multiple_single_level_wildcards() {
        final String string = "+/abc/+/def/+/+/ghi/+";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_double_single_level_wildcard() {
        final String string = "abc/++/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_single_level_wildcard_after_non_separator() {
        final String string = "abc+/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_single_level_wildcard_before_non_separator() {
        final String string = "abc/+def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_single_level_before_multi_level_wildcard() {
        final String string = "abc/def/ghi/+#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_contain_both_wildcards() {
        final String string = "abc/+/def/+/ghi/#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_getLevels() {
        final String string = "abc/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(3, levels.size());
        assertEquals("abc", levels.get(0));
        assertEquals("def", levels.get(1));
        assertEquals("ghi", levels.get(2));
    }

    @Test
    public void test_getLevels_empty_levels() {
        final String string = "/abc//def///ghi/";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
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

    @Test
    public void test_containsWildcards_false() {
        final String string = "abc/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.containsWildcards());
        assertFalse(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertFalse(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_containsWildcards_multi_level() {
        final String string = "abc/def/ghi/#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.containsWildcards());
        assertTrue(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertFalse(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_containsWildcards_single_level() {
        final String string = "abc/+/def/ghi";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.containsWildcards());
        assertFalse(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertTrue(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_containsWildcards_both() {
        final String string = "abc/+/def/ghi/#";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.containsWildcards());
        assertTrue(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertTrue(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_not_shared_just_prefix() {
        final String string = "$shared/group/abc/def";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.isShared());
        assertFalse(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
    }

    @Test
    public void test_not_shared_just_$share() {
        final String string = "$share";
        final MqttTopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.isShared());
        assertFalse(mqtt5TopicFilter instanceof MqttSharedTopicFilterImpl);
    }

}
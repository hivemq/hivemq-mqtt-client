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
public class MqttTopicImplTest {

    @Parameterized.Parameters
    public static Collection<Boolean> parameters() {
        return ImmutableSet.of(false, true);
    }

    private final boolean isFromByteBuf;

    public MqttTopicImplTest(final boolean isFromByteBuf) {
        this.isFromByteBuf = isFromByteBuf;
    }

    private MqttTopicImpl from(final String string) {
        if (isFromByteBuf) {
            final ByteBuf byteBuf = Unpooled.buffer();
            final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
            byteBuf.writeShort(binary.length);
            byteBuf.writeBytes(binary);
            final MqttTopicImpl mqtt5Topic = MqttTopicImpl.from(byteBuf);
            byteBuf.release();
            return mqtt5Topic;
        } else {
            return MqttTopicImpl.from(string);
        }
    }

    @Test
    public void test_must_not_be_zero_length() {
        final String string = "";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void test_must_be_case_sensitive() {
        final String string1 = "abc";
        final String string2 = "ABC";
        final MqttTopicImpl mqtt5Topic1 = from(string1);
        final MqttTopicImpl mqtt5Topic2 = from(string2);
        assertNotNull(mqtt5Topic1);
        assertNotNull(mqtt5Topic2);
        assertNotEquals(mqtt5Topic1.toString(), mqtt5Topic2.toString());
    }

    @Test
    public void test_can_contain_space() {
        final String string = "ab c/def";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNotNull(mqtt5Topic);
    }

    @Test
    public void test_can_be_only_space() {
        final String string = " ";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNotNull(mqtt5Topic);
    }

    @Test
    public void test_can_be_only_topic_level_separator() {
        final String string = "/";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNotNull(mqtt5Topic);
        final ImmutableList<String> levels = mqtt5Topic.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));
    }

    @Test
    public void test_must_not_contain_multi_level_wildcard() {
        final String string = "abc/def/#";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void test_must_not_contain_single_level_wildcard() {
        final String string = "abc/+/def";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void test_getLevels() {
        final String string = "abc/def/ghi";
        final MqttTopicImpl mqtt5Topic = from(string);
        assertNotNull(mqtt5Topic);
        final ImmutableList<String> levels = mqtt5Topic.getLevels();
        assertEquals(3, levels.size());
        assertEquals("abc", levels.get(0));
        assertEquals("def", levels.get(1));
        assertEquals("ghi", levels.get(2));
    }

    @Test
    public void test_getLevels_empty_levels() {
        final String string = "/abc//def///ghi/";
        final MqttTopicImpl mqtt5Topic = from(string);
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
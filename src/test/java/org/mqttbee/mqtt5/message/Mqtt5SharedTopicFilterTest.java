package org.mqttbee.mqtt5.message;

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
public class Mqtt5SharedTopicFilterTest {

    private static final int FROM_STRING = 0;
    private static final int FROM_BYTE_BUF = 1;
    private static final int FROM_SHARE_NAME_AND_TOPIC_FILTER = 2;

    @Parameterized.Parameters
    public static Collection<Integer> parameters() {
        return ImmutableSet.of(FROM_STRING, FROM_BYTE_BUF, FROM_SHARE_NAME_AND_TOPIC_FILTER);
    }

    private final int source;

    public Mqtt5SharedTopicFilterTest(final int source) {
        this.source = source;
    }

    private Mqtt5TopicFilter from(final String string) {
        switch (source) {
            case FROM_SHARE_NAME_AND_TOPIC_FILTER:
                break;
            case FROM_BYTE_BUF:
                final ByteBuf byteBuf = Unpooled.buffer();
                final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
                byteBuf.writeShort(binary.length);
                byteBuf.writeBytes(binary);
                final Mqtt5TopicFilter mqtt5TopicFilter = Mqtt5TopicFilter.from(byteBuf);
                byteBuf.release();
                return mqtt5TopicFilter;
            case FROM_STRING:
                return Mqtt5TopicFilter.from(string);
        }
        throw new IllegalStateException();
    }

    private Mqtt5TopicFilter from(final String shareName, final String topicFilter) {
        final String string = "$share/" + shareName + "/" + topicFilter;
        switch (source) {
            case FROM_SHARE_NAME_AND_TOPIC_FILTER:
                return Mqtt5SharedTopicFilter.from(shareName, topicFilter);
            case FROM_BYTE_BUF:
                final ByteBuf byteBuf = Unpooled.buffer();
                final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
                byteBuf.writeShort(binary.length);
                byteBuf.writeBytes(binary);
                final Mqtt5TopicFilter mqtt5TopicFilter = Mqtt5TopicFilter.from(byteBuf);
                byteBuf.release();
                return mqtt5TopicFilter;
            case FROM_STRING:
                return Mqtt5TopicFilter.from(string);
        }
        throw new IllegalStateException();
    }

    @Test
    public void test_shared() {
        final String shareName = "group";
        final String topicFilter = "abc/def";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_share_name_must_not_be_zero_length() {
        final String shareName = "";
        final String topicFilter = "abc/def";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_share_name_must_not_contain_wildcards() {
        final String shareName1 = "group#";
        final String shareName2 = "#group";
        final String shareName3 = "group+";
        final String shareName4 = "+group";
        final String topicFilter = "abc/def";
        final Mqtt5TopicFilter mqtt5TopicFilter1 = from(shareName1, topicFilter);
        final Mqtt5TopicFilter mqtt5TopicFilter2 = from(shareName2, topicFilter);
        final Mqtt5TopicFilter mqtt5TopicFilter3 = from(shareName3, topicFilter);
        final Mqtt5TopicFilter mqtt5TopicFilter4 = from(shareName4, topicFilter);
        assertNull(mqtt5TopicFilter1);
        assertNull(mqtt5TopicFilter2);
        assertNull(mqtt5TopicFilter3);
        assertNull(mqtt5TopicFilter4);
    }

    @Test
    public void test_shared_must_not_be_without_topic_filter() {
        if (source != FROM_SHARE_NAME_AND_TOPIC_FILTER) {
            final String string = "$share/group";
            final Mqtt5TopicFilter mqtt5TopicFilter = from(string);
            assertNull(mqtt5TopicFilter);
        }
    }

    @Test
    public void test_shared_topic_filter_must_not_be_zero_length() {
        final String shareName = "group";
        final String topicFilter = "";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_must_be_case_sensitive() {
        final String shareName = "group";
        final String topicFilter1 = "abc";
        final String topicFilter2 = "ABC";
        final Mqtt5TopicFilter mqtt5TopicFilter1 = from(shareName, topicFilter1);
        final Mqtt5TopicFilter mqtt5TopicFilter2 = from(shareName, topicFilter2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertTrue(mqtt5TopicFilter1.isShared());
        assertTrue(mqtt5TopicFilter1 instanceof Mqtt5SharedTopicFilter);
        assertTrue(mqtt5TopicFilter2.isShared());
        assertTrue(mqtt5TopicFilter2 instanceof Mqtt5SharedTopicFilter);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter1 = (Mqtt5SharedTopicFilter) mqtt5TopicFilter1;
        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter2 = (Mqtt5SharedTopicFilter) mqtt5TopicFilter2;
        assertEquals(shareName, mqtt5SharedTopicFilter1.getShareName());
        assertEquals(shareName, mqtt5SharedTopicFilter2.getShareName());
        assertEquals(topicFilter1, mqtt5SharedTopicFilter1.getTopicFilter());
        assertEquals(topicFilter2, mqtt5SharedTopicFilter2.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_contain_space() {
        final String shareName = "group";
        final String topicFilter = "ab c/def";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_be_only_space() {
        final String shareName = "group";
        final String topicFilter = " ";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_be_only_topic_level_separator() {
        final String shareName = "group";
        final String topicFilter = "/";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_contain_multi_level_wildcard() {
        final String shareName = "group";
        final String topicFilter = "/abc/def/ghi/#";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_be_only_multi_level_wildcard() {
        final String shareName = "group";
        final String topicFilter = "#";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_must_not_contain_multi_level_wildcard_not_at_end() {
        final String shareName = "group";
        final String topicFilter = "abc/def/ghi/#/";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_must_not_contain_multi_level_wildcard_after_non_separator() {
        final String shareName = "group";
        final String topicFilter = "abc/def/ghi#";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_can_contain_single_level_wildcard() {
        final String shareName = "group";
        final String topicFilter = "abc/+/def/ghi";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_be_only_single_level_wildcard() {
        final String shareName = "group";
        final String topicFilter = "+";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_can_contain_multiple_single_level_wildcards() {
        final String shareName = "group";
        final String topicFilter = "+/abc/+/def/+/+/ghi/+";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_topic_filter_must_not_contain_double_single_level_wildcard() {
        final String shareName = "group";
        final String topicFilter = "abc/++/def/ghi";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_must_not_contain_single_level_wildcard_after_non_separator() {
        final String shareName = "group";
        final String topicFilter = "abc+/def/ghi";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_must_not_contain_single_level_wildcard_before_non_separator() {
        final String shareName = "group";
        final String topicFilter = "abc/+def/ghi";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_must_not_contain_single_level_before_multi_level_wildcard() {
        final String shareName = "group";
        final String topicFilter = "abc/def/ghi/+#";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_shared_topic_filter_can_contain_both_wildcards() {
        final String shareName = "group";
        final String topicFilter = "abc/+/def/+/ghi/#";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);

        final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = (Mqtt5SharedTopicFilter) mqtt5TopicFilter;
        assertEquals(shareName, mqtt5SharedTopicFilter.getShareName());
        assertEquals(topicFilter, mqtt5SharedTopicFilter.getTopicFilter());
    }

    @Test
    public void test_shared_getLevels() {
        final String shareName = "group";
        final String topicFilter = "abc/def/ghi";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(3, levels.size());
        assertEquals("abc", levels.get(0));
        assertEquals("def", levels.get(1));
        assertEquals("ghi", levels.get(2));
    }

    @Test
    public void test_shared_getLevels_empty_levels() {
        final String shareName = "group";
        final String topicFilter = "/abc//def///ghi/";
        final Mqtt5TopicFilter mqtt5TopicFilter = from(shareName, topicFilter);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.isShared());
        assertTrue(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilter);
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
    public void test_shared_share_name_must_not_contain_topic_level_separator() {
        if (source == FROM_BYTE_BUF) {
            final String shareName = "gro/up";
            final String topicFilter = "abc/def/ghi";
            final Mqtt5SharedTopicFilter mqtt5SharedTopicFilter = Mqtt5SharedTopicFilter.from(shareName, topicFilter);
            assertNull(mqtt5SharedTopicFilter);
        }
    }

}
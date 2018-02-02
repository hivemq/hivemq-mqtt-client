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
public class Mqtt5TopicFilterImplTest {

    @Parameterized.Parameters
    public static Collection<Boolean> parameters() {
        return ImmutableSet.of(false, true);
    }

    private final boolean isFromByteBuf;

    public Mqtt5TopicFilterImplTest(final boolean isFromByteBuf) {
        this.isFromByteBuf = isFromByteBuf;
    }

    private Mqtt5TopicFilterImpl from(final String string) {
        if (isFromByteBuf) {
            final ByteBuf byteBuf = Unpooled.buffer();
            final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
            byteBuf.writeShort(binary.length);
            byteBuf.writeBytes(binary);
            final Mqtt5TopicFilterImpl mqtt5TopicFilter = Mqtt5TopicFilterImpl.from(byteBuf);
            byteBuf.release();
            return mqtt5TopicFilter;
        } else {
            return Mqtt5TopicFilterImpl.from(string);
        }
    }

    @Test
    public void test_must_not_be_zero_length() {
        final String string = "";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_be_case_sensitive() {
        final String string1 = "abc";
        final String string2 = "ABC";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter1 = from(string1);
        final Mqtt5TopicFilterImpl mqtt5TopicFilter2 = from(string2);
        assertNotNull(mqtt5TopicFilter1);
        assertNotNull(mqtt5TopicFilter2);
        assertNotEquals(mqtt5TopicFilter1.toString(), mqtt5TopicFilter2.toString());
    }

    @Test
    public void test_can_contain_space() {
        final String string = "ab c/def";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_space() {
        final String string = " ";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_topic_level_separator() {
        final String string = "/";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        final ImmutableList<String> levels = mqtt5TopicFilter.getLevels();
        assertEquals(2, levels.size());
        assertEquals("", levels.get(0));
        assertEquals("", levels.get(1));
    }

    @Test
    public void test_can_contain_multi_level_wildcard() {
        final String string = "abc/def/ghi/#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_multi_level_wildcard() {
        final String string = "#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_multi_level_wildcard_not_at_end() {
        final String string = "abc/def/ghi/#/";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_multi_level_wildcard_after_non_separator() {
        final String string = "abc/def/ghi#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_contain_single_level_wildcard() {
        final String string = "abc/+/def/ghi";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_be_only_single_level_wildcard() {
        final String string = "+";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_contain_multiple_single_level_wildcards() {
        final String string = "+/abc/+/def/+/+/ghi/+";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_double_single_level_wildcard() {
        final String string = "abc/++/def/ghi";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_single_level_wildcard_after_non_separator() {
        final String string = "abc+/def/ghi";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_single_level_wildcard_before_non_separator() {
        final String string = "abc/+def/ghi";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_must_not_contain_single_level_before_multi_level_wildcard() {
        final String string = "abc/def/ghi/+#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNull(mqtt5TopicFilter);
    }

    @Test
    public void test_can_contain_both_wildcards() {
        final String string = "abc/+/def/+/ghi/#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
    }

    @Test
    public void test_getLevels() {
        final String string = "abc/def/ghi";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
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
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
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
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.containsWildcards());
        assertFalse(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertFalse(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_containsWildcards_multi_level() {
        final String string = "abc/def/ghi/#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.containsWildcards());
        assertTrue(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertFalse(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_containsWildcards_single_level() {
        final String string = "abc/+/def/ghi";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.containsWildcards());
        assertFalse(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertTrue(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_containsWildcards_both() {
        final String string = "abc/+/def/ghi/#";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertTrue(mqtt5TopicFilter.containsWildcards());
        assertTrue(mqtt5TopicFilter.containsMultiLevelWildcard());
        assertTrue(mqtt5TopicFilter.containsSingleLevelWildcard());
    }

    @Test
    public void test_not_shared_just_prefix() {
        final String string = "$shared/group/abc/def";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.isShared());
        assertFalse(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilterImpl);
    }

    @Test
    public void test_not_shared_just_$share() {
        final String string = "$share";
        final Mqtt5TopicFilterImpl mqtt5TopicFilter = from(string);
        assertNotNull(mqtt5TopicFilter);
        assertFalse(mqtt5TopicFilter.isShared());
        assertFalse(mqtt5TopicFilter instanceof Mqtt5SharedTopicFilterImpl);
    }

}
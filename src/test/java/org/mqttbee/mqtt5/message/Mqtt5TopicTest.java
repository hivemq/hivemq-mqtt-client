package org.mqttbee.mqtt5.message;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicTest {

    @Test
    public void test_must_not_multi_level_wildcard() {
        final String string = "abc/def/#";
        final Mqtt5Topic mqtt5Topic = Mqtt5Topic.from(string);
        assertNull(mqtt5Topic);
    }

    @Test
    public void test_must_not_single_level_wildcard() {
        final String string = "abc/+/def";
        final Mqtt5Topic mqtt5Topic = Mqtt5Topic.from(string);
        assertNull(mqtt5Topic);
    }

}
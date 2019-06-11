/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3;

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import util.implementations.CustomMqttTopicFilter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Michael Walter
 */
class Mqtt3UnsubscribeViewBuilderTest {

    @Test
    void addTopicFilter_error_when_topic_string_is_null() {

        final String topic = null;

        assertThrows(NullPointerException.class, () -> Mqtt3Unsubscribe.builder().addTopicFilter(topic));
    }

    @Test
    void addTopicFilter_error_when_mqtt_topic_is_null() {

        final MqttTopicFilter topic = null;

        assertThrows(NullPointerException.class, () -> Mqtt3Unsubscribe.builder().addTopicFilter(topic));
    }

    @Test
    void addTopicFilter_error_when_topic_object_is_implemented() {

        assertThrows(IllegalArgumentException.class,
                () -> Mqtt3Unsubscribe.builder().addTopicFilter(new CustomMqttTopicFilter()));
    }

    @Test
    void addTopicFilter_correct_use() {

        final Mqtt3Unsubscribe subscribe =
                Mqtt3Unsubscribe.builder().addTopicFilter("test").addTopicFilter(MqttTopicFilter.of("topics")).build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_mqtttopic() {

        final Mqtt3Unsubscribe subscribe =
                Mqtt3Unsubscribe.builder().topicFilter("test").addTopicFilter(MqttTopicFilter.of("topics")).build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_string() {

        final Mqtt3Unsubscribe subscribe =
                Mqtt3Unsubscribe.builder().topicFilter("test").addTopicFilter("topics").build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_nested_topic_builder_is_used_with_and_then_addTopicFilter_mqtttopic() {

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addTopicFilter(MqttTopicFilter.of("topics"))
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_nested_topic_builder_is_used_and_then_addTopicFilter_string() {

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addTopicFilter("topics")
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_second_time_mqtttopic() {

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter("test")
                .addTopicFilter(MqttTopicFilter.of("firstAdd"))
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addTopicFilter(MqttTopicFilter.of("secondAdd"))
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("firstAdd", unsubscribeTopics.get(1).toString());
        assertEquals("nested", unsubscribeTopics.get(2).toString());
        assertEquals("secondAdd", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_second_time_string() {

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter("test")
                .addTopicFilter("firstAdd")
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addTopicFilter("secondAdd")
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("firstAdd", unsubscribeTopics.get(1).toString());
        assertEquals("nested", unsubscribeTopics.get(2).toString());
        assertEquals("secondAdd", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_fluent_addTopicFilter_is_used() {

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter("test")
                .addTopicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("first/second/#", unsubscribeTopics.get(1).toString());
    }

    @Test
    void fluent_topic_is_finished_when_building() {

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder().topicFilter("test").build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(1, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
    }

    @Test
    void addTopicFilters_error_when_topic_string_is_null() {

        assertThrows(IllegalArgumentException.class, () -> Mqtt3Unsubscribe.builder().addTopicFilters(null));
    }

    @Test
    void addTopicFilters_error_when_list_is_empty() {

        assertThrows(
                IllegalArgumentException.class, () -> Mqtt3Unsubscribe.builder().addTopicFilters(ImmutableList.of()));
    }

    @Test
    void addTopicFilters_correct_use() {

        final List<String> topics = new ArrayList<>();
        topics.add("test");
        topics.add("list");
        topics.add("element");

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder().addTopicFilters(topics).build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(3, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("list", unsubscribeTopics.get(1).toString());
        assertEquals("element", unsubscribeTopics.get(2).toString());
    }

    @Test
    void addTopicFilters_fluent_topic_is_finished_if_addTopicFilters_is_used() {

        final List<String> topics = new LinkedList<>();
        topics.add("test");
        topics.add("list");
        topics.add("element");

        final Mqtt3Unsubscribe subscribe =
                Mqtt3Unsubscribe.builder().topicFilter("fluent").addTopicFilters(topics).build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("fluent", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilters_nested_topic_builder_is_used_and_then_addTopicFilters() {

        final List<String> topics = new ArrayList<>();
        topics.add("test");
        topics.add("list");
        topics.add("element");

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addTopicFilters(topics)
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilters_fluent_topic_is_finished_if_addTopicFilters_is_used_second_time() {

        final ImmutableList<String> topics = ImmutableList.of("test", "list", "element");
        final ImmutableList<String> topics2 = ImmutableList.of("second");

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter("fluent")
                .addTopicFilters(topics)
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addTopicFilters(topics2)
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(6, unsubscribeTopics.size());

        assertEquals("fluent", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
        assertEquals("nested", unsubscribeTopics.get(4).toString());
        assertEquals("second", unsubscribeTopics.get(5).toString());
    }

    @Test
    void addMqttTopicFilters_error_when_topic_string_is_null() {

        assertThrows(IllegalArgumentException.class, () -> Mqtt3Unsubscribe.builder().addMqttTopicFilters(null));
    }

    @Test
    void addMqttTopicFilterss_error_when_list_is_empty() {

        assertThrows(IllegalArgumentException.class,
                () -> Mqtt3Unsubscribe.builder().addMqttTopicFilters(ImmutableList.of()));
    }

    @Test
    void addMqttTopicFilters_correct_use() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder().addMqttTopicFilters(topics).build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(3, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("list", unsubscribeTopics.get(1).toString());
        assertEquals("element", unsubscribeTopics.get(2).toString());
    }

    @Test
    void addMqttTopicFilters_fluent_topic_is_finished_if_addMqttTopicFilters_is_used() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt3Unsubscribe subscribe =
                Mqtt3Unsubscribe.builder().topicFilter("fluent").addMqttTopicFilters(topics).build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("fluent", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addMqttTopicFilters_nested_topic_builder_is_used_and_then_addMqttTopicFilters() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addMqttTopicFilters(topics)
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addMqttTopicFilters_fluent_topic_is_finished_if_addMqttTopicFilters_is_used_second_time() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));
        final ImmutableList<MqttTopicFilter> topics2 = ImmutableList.of(MqttTopicFilter.of("second"));

        final Mqtt3Unsubscribe subscribe = Mqtt3Unsubscribe.builder()
                .topicFilter("fluent")
                .addMqttTopicFilters(topics)
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addMqttTopicFilters(topics2)
                .build();

        final List<@NotNull ? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(6, unsubscribeTopics.size());

        assertEquals("fluent", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
        assertEquals("nested", unsubscribeTopics.get(4).toString());
        assertEquals("second", unsubscribeTopics.get(5).toString());
    }

    @Test
    void addMqttTopicFilters_error_when_topic_object_is_implemented() {

        final ImmutableList<MqttTopicFilter> topicFilters =
                ImmutableList.of(MqttTopicFilter.of("test"), new CustomMqttTopicFilter());

        assertThrows(
                IllegalArgumentException.class, () -> Mqtt3Unsubscribe.builder().addMqttTopicFilters(topicFilters));
    }

}
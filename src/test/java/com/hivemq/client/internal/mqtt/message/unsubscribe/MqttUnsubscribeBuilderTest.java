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

package com.hivemq.client.internal.mqtt.message.unsubscribe;

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * @author Michael Walter
 */
class MqttUnsubscribeBuilderTest {

    @Test
    void addTopicFilter_error_when_topic_string_is_null() {
        final String topic = null;
        assertThrows(NullPointerException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilter(topic));
    }

    @Test
    void addTopicFilter_error_when_mqtt_topic_is_null() {
        final MqttTopicFilter topic = null;
        assertThrows(NullPointerException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilter(topic));
    }

    @Test
    void addTopicFilter_error_when_topic_object_is_implemented() {
        assertThrows(IllegalArgumentException.class,
                () -> Mqtt5Unsubscribe.builder().addTopicFilter(mock(MqttTopicFilter.class)));
    }

    @Test
    void addTopicFilter_correct_use() {

        final Mqtt5Unsubscribe subscribe =
                Mqtt5Unsubscribe.builder().addTopicFilter("test").addTopicFilter(MqttTopicFilter.of("topics")).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_mqtttopic() {

        final Mqtt5Unsubscribe subscribe =
                Mqtt5Unsubscribe.builder().topicFilter("test").addTopicFilter(MqttTopicFilter.of("topics")).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_string() {

        final Mqtt5Unsubscribe subscribe =
                Mqtt5Unsubscribe.builder().topicFilter("test").addTopicFilter("topics").build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_nested_topic_builder_is_used_with_and_then_addTopicFilter_mqtttopic() {

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addTopicFilter(MqttTopicFilter.of("topics"))
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_nested_topic_builder_is_used_and_then_addTopicFilter_string() {

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addTopicFilter("topics")
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("topics", unsubscribeTopics.get(1).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_second_time_mqtttopic() {

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder()
                .topicFilter("test")
                .addTopicFilter(MqttTopicFilter.of("firstAdd"))
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addTopicFilter(MqttTopicFilter.of("secondAdd"))
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("firstAdd", unsubscribeTopics.get(1).toString());
        assertEquals("nested", unsubscribeTopics.get(2).toString());
        assertEquals("secondAdd", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_addTopicFilter_is_used_second_time_string() {

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder()
                .topicFilter("test")
                .addTopicFilter("firstAdd")
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addTopicFilter("secondAdd")
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("firstAdd", unsubscribeTopics.get(1).toString());
        assertEquals("nested", unsubscribeTopics.get(2).toString());
        assertEquals("secondAdd", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilter_fluent_topic_is_finished_if_fluent_addTopicFilter_is_used() {

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder()
                .topicFilter("test")
                .addTopicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(2, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("first/second/#", unsubscribeTopics.get(1).toString());
    }

    @Test
    void fluent_topic_is_finished_when_building() {

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder().topicFilter("test").build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(1, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
    }

    @Test
    void addTopicFilters_error_when_collection_is_null() {
        assertThrows(NullPointerException.class,
                () -> Mqtt5Unsubscribe.builder().addTopicFilters((ArrayList<MqttTopicFilter>) null));
    }

    @Test
    void addTopicFilters_error_when_varargs_is_null() {
        assertThrows(
                NullPointerException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilters((MqttTopicFilter[]) null));
    }

    @Test
    void addTopicFilters_error_when_stream_is_null() {
        assertThrows(NullPointerException.class,
                () -> Mqtt5Unsubscribe.builder().addTopicFilters((Stream<MqttTopicFilter>) null));
    }

    @Test
    void addTopicFilters_error_when_collection_is_empty() {
        assertThrows(IllegalStateException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilters(new ArrayList<>()));
    }

    @Test
    void addTopicFilters_error_when_varargs_is_empty() {
        assertThrows(IllegalStateException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilters());
    }

    @Test
    void addTopicFilters_error_when_stream_is_empty() {
        assertThrows(IllegalStateException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilters(Stream.of()));
    }

    @Test
    void addTopicFilters_correct_use() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder().addTopicFilters(topics).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(3, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("list", unsubscribeTopics.get(1).toString());
        assertEquals("element", unsubscribeTopics.get(2).toString());
    }

    @Test
    void addTopicFilters_correct_use_collection() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder().addTopicFilters(topics).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(3, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("list", unsubscribeTopics.get(1).toString());
        assertEquals("element", unsubscribeTopics.get(2).toString());
    }

    @Test
    void addTopicFilters_correct_use_varargs() {

        final MqttTopicFilter[] topics =
                {MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element")};

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder().addTopicFilters(topics).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(3, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("list", unsubscribeTopics.get(1).toString());
        assertEquals("element", unsubscribeTopics.get(2).toString());
    }

    @Test
    void addTopicFilters_correct_use_stream() {

        final ImmutableList<String> topics = ImmutableList.of("test", "list", "element");

        final Mqtt5Unsubscribe subscribe =
                Mqtt5Unsubscribe.builder().addTopicFilters(topics.stream().map(MqttTopicFilter::of)).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(3, unsubscribeTopics.size());

        assertEquals("test", unsubscribeTopics.get(0).toString());
        assertEquals("list", unsubscribeTopics.get(1).toString());
        assertEquals("element", unsubscribeTopics.get(2).toString());
    }

    @Test
    void addTopicFilters_fluent_topic_is_finished_if_addTopicFilters_is_used() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt5Unsubscribe subscribe =
                Mqtt5Unsubscribe.builder().topicFilter("fluent").addTopicFilters(topics).build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("fluent", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilters_nested_topic_builder_is_used_and_then_addTopicFilters() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder()
                .topicFilter()
                .addLevel("first")
                .addLevel("second")
                .multiLevelWildcard()
                .applyTopicFilter()
                .addTopicFilters(topics)
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(4, unsubscribeTopics.size());

        assertEquals("first/second/#", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
    }

    @Test
    void addTopicFilters_fluent_topic_is_finished_if_addTopicFilters_is_used_second_time() {

        final ImmutableList<MqttTopicFilter> topics =
                ImmutableList.of(MqttTopicFilter.of("test"), MqttTopicFilter.of("list"), MqttTopicFilter.of("element"));
        final ImmutableList<MqttTopicFilter> topics2 = ImmutableList.of(MqttTopicFilter.of("second"));

        final Mqtt5Unsubscribe subscribe = Mqtt5Unsubscribe.builder().topicFilter("fluent").addTopicFilters(topics)
                .addTopicFilter()
                .addLevel("nested")
                .applyTopicFilter()
                .addTopicFilters(topics2)
                .build();

        final List<? extends MqttTopicFilter> unsubscribeTopics = subscribe.getTopicFilters();
        assertEquals(6, unsubscribeTopics.size());

        assertEquals("fluent", unsubscribeTopics.get(0).toString());
        assertEquals("test", unsubscribeTopics.get(1).toString());
        assertEquals("list", unsubscribeTopics.get(2).toString());
        assertEquals("element", unsubscribeTopics.get(3).toString());
        assertEquals("nested", unsubscribeTopics.get(4).toString());
        assertEquals("second", unsubscribeTopics.get(5).toString());
    }

    @Test
    void addTopicFilters_error_when_topic_object_is_implemented() {

        final ImmutableList<MqttTopicFilter> topicFilters =
                ImmutableList.of(MqttTopicFilter.of("test"), mock(MqttTopicFilter.class));

        assertThrows(IllegalArgumentException.class, () -> Mqtt5Unsubscribe.builder().addTopicFilters(topicFilters));
    }
}
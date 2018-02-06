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

package org.mqttbee.mqtt.message.publish;


import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS;


class MqttTopicAliasMappingTest {

    @Test
    void set_may() {
        final MqttTopicAliasMapping topicAliasMapping = new MqttTopicAliasMapping(100);
        final MqttTopicImpl topic = requireNonNull(MqttTopicImpl.from("topic"));
        final boolean topicAlias = topicAliasMapping.createIfAbsent(topic, TopicAliasUsage.MAY);
        assertTrue(topicAlias);
        assertThat(topicAliasMapping.get(topic), is(not(DEFAULT_NO_TOPIC_ALIAS)));
    }

    @Test
    void set_mustNot() {
        final MqttTopicAliasMapping topicAliasMapping = new MqttTopicAliasMapping(100);
        final MqttTopicImpl topic = requireNonNull(MqttTopicImpl.from("topic"));
        final boolean topicAliasSet = topicAliasMapping.createIfAbsent(topic, TopicAliasUsage.MUST_NOT);
        assertEquals(false, topicAliasSet);
        assertThat(topicAliasMapping.get(topic), is(DEFAULT_NO_TOPIC_ALIAS));
    }

    @Test
    void set_multipleTimesReturnsSameMapping() {
        final MqttTopicAliasMapping topicAliasMapping = new MqttTopicAliasMapping(100);
        final MqttTopic topic = requireNonNull(MqttTopic.from("topic"));
        boolean topicAlias1Set = topicAliasMapping.createIfAbsent(topic, TopicAliasUsage.MAY);
        assertTrue(topicAlias1Set);
        int topicAlias = topicAliasMapping.get(topic);

        topicAlias1Set = topicAliasMapping.createIfAbsent(topic, TopicAliasUsage.MAY);
        assertFalse(topicAlias1Set);
        int topicAliasAgain = topicAliasMapping.get(topic);

        assertEquals(topicAlias, topicAliasAgain);
    }

    @Test
    void set_overwrite() {
        final MqttTopicAliasMapping topicAliasMapping = new MqttTopicAliasMapping(1);
        final MqttTopic topic1 = requireNonNull(MqttTopic.from("topic1"));
        final MqttTopic topic2 = requireNonNull(MqttTopic.from("topic2"));
        final boolean topicAlias1Set = topicAliasMapping.createIfAbsent(topic1, TopicAliasUsage.MAY);
        assertTrue(topicAlias1Set);
        final boolean topicAlias2Set = topicAliasMapping.createIfAbsent(topic2, TopicAliasUsage.MAY_OVERWRITE);
        assertTrue(topicAlias2Set);
        assertThat(topicAliasMapping.get(topic1), is(DEFAULT_NO_TOPIC_ALIAS));
        assertThat(topicAliasMapping.get(topic2), is(not(DEFAULT_NO_TOPIC_ALIAS)));
    }

    @Test
    void set_maximumReached_ReturnsNoTopicAlias() {
        final MqttTopicAliasMapping topicAliasMapping = new MqttTopicAliasMapping(1);
        final MqttTopic topic1 = requireNonNull(MqttTopic.from("topic1"));
        final MqttTopic topic2 = requireNonNull(MqttTopic.from("topic2"));
        final boolean topicAlias1Set = topicAliasMapping.createIfAbsent(topic1, TopicAliasUsage.MAY);
        final boolean topicAlias2Set = topicAliasMapping.createIfAbsent(topic2, TopicAliasUsage.MAY);
        assertTrue(topicAlias1Set);
        assertFalse(topicAlias2Set);
        assertThat(topicAliasMapping.get(topic1), is(not(DEFAULT_NO_TOPIC_ALIAS)));
        assertThat(topicAliasMapping.get(topic2), is(DEFAULT_NO_TOPIC_ALIAS));
    }

    @Test
    void maximumTopicAlias() {
        final int maximumTopicAlias = 200;
        final MqttTopicAliasMapping topicAliasMapping = new MqttTopicAliasMapping(maximumTopicAlias);
        assertEquals(maximumTopicAlias, topicAliasMapping.getTopicAliasMaximum());
    }
}

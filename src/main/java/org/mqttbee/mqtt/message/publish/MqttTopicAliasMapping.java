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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;

import java.util.HashMap;
import java.util.Random;

import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS;

/**
 * @author Silvio Giebl
 */
public class MqttTopicAliasMapping {

    private static final Random random = new Random();

    private final int topicAliasMaximum;
    private final HashMap<String, Integer> hashMap;
    private int nextTopicAlias;

    public MqttTopicAliasMapping(final int topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
        hashMap = new HashMap<>(topicAliasMaximum);
        nextTopicAlias = 1;
    }

    public boolean createIfAbsent(@NotNull final MqttTopic topic, @NotNull final TopicAliasUsage topicAliasUsage) {
        if (topicAliasUsage == TopicAliasUsage.MUST_NOT) {
            return false;
        }

        // we have run out of topic aliases and cannot overwrite
        if (nextTopicAlias > topicAliasMaximum && topicAliasUsage != TopicAliasUsage.MAY_OVERWRITE) {
            return false;
        }

        // look for an existing one
        int topicAlias = get(topic);
        if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
            return false;
        }

        // assign next available topic alias
        if (nextTopicAlias <= topicAliasMaximum) {
            topicAlias = nextTopicAlias;
            nextTopicAlias++;
        } else {
            assert topicAliasUsage == TopicAliasUsage.MAY_OVERWRITE;
            // used up all allowed topic aliases. Overwrite an existing one
            topicAlias = random.nextInt(topicAliasMaximum) + 1;
            // first remove the old mapping
            hashMap.values().remove(topicAlias);
        }
        hashMap.put(topic.toString(), topicAlias);
        return true;
    }

    public int get(@NotNull final MqttTopic topic) {
        final Integer topicAlias = hashMap.get(topic.toString());
        return (topicAlias == null) ? DEFAULT_NO_TOPIC_ALIAS : topicAlias;
    }

    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

}

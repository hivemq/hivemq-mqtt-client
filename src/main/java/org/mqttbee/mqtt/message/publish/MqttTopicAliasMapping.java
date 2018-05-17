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
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;

import java.util.HashMap;
import java.util.Random;

/**
 * @author Silvio Giebl
 */
public class MqttTopicAliasMapping {

    private static final Random random = new Random();

    private final int size;
    private final HashMap<String, Integer> hashMap;
    private int nextTopicAlias;

    public MqttTopicAliasMapping(final int size) {
        this.size = size;
        hashMap = new HashMap<>(size);
        nextTopicAlias = 1;
    }

    public int set(@NotNull final MqttTopicImpl topic, @NotNull final TopicAliasUsage topicAliasUsage) {
        int topicAlias = MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;
        if (topicAliasUsage != TopicAliasUsage.MUST_NOT) {
            if (nextTopicAlias == size) {
                if (topicAliasUsage == TopicAliasUsage.MAY_OVERWRITE) {
                    topicAlias = random.nextInt(size) + 1;
                    hashMap.put(topic.toString(), topicAlias);
                }
            } else {
                topicAlias = nextTopicAlias;
                hashMap.put(topic.toString(), topicAlias);
                nextTopicAlias++;
            }
        }
        return topicAlias;
    }

    public int get(@NotNull final MqttTopicImpl topic) {
        final Integer topicAlias = hashMap.get(topic.toString());
        return (topicAlias == null) ? MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS : topicAlias;
    }

    public int size() {
        return size;
    }

}

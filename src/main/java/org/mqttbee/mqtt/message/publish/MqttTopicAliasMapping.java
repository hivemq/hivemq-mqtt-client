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

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Random;

import static org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage.IF_AVAILABLE;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage.YES;

/**
 * @author Silvio Giebl
 * @author Christian Hoff
 */
@NotThreadSafe
public class MqttTopicAliasMapping {

    private static final Random random = new Random();

    private int topicAliasMaximum;
    private final HashMap<String, Integer> hashMap;
    private int nextTopicAlias;

    public MqttTopicAliasMapping(final int topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
        hashMap = new HashMap<>();
        nextTopicAlias = 1;
    }

    public int set(@NotNull final MqttTopicImpl topic, @NotNull final TopicAliasUsage topicAliasUsage) {
        int topicAlias = MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS;
        if (nextTopicAlias <= topicAliasMaximum) { // unused topic alias available
            if (topicAliasUsage == YES || topicAliasUsage == IF_AVAILABLE) { // use next free topic alias
                topicAlias = nextTopicAlias;
                hashMap.put(topic.toString(), topicAlias);
                nextTopicAlias++;
            }
        } else {
            if (topicAliasUsage == YES) { // override existing topic alias
                topicAlias = random.nextInt(topicAliasMaximum) + 1;
                hashMap.values().remove(topicAlias);
                hashMap.put(topic.toString(), topicAlias);
            }
        }
        return topicAlias;
    }

    public int get(@NotNull final MqttTopicImpl topic) {
        final Integer topicAlias = hashMap.get(topic.toString());
        return (topicAlias == null) ? MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS : topicAlias;
    }

    public void reset(final int topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
        hashMap.clear();
        nextTopicAlias = 1;
    }

    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

}

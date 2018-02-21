package org.mqttbee.mqtt.message.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.TopicAliasUsage;
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

    int set(@NotNull final MqttTopicImpl topic, @NotNull final TopicAliasUsage topicAliasUsage) {
        int topicAlias = MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS;
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

    int get(@NotNull final MqttTopicImpl topic) {
        final Integer topicAlias = hashMap.get(topic.toString());
        return (topicAlias == null) ? MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS : topicAlias;
    }

    public int size() {
        return size;
    }

}

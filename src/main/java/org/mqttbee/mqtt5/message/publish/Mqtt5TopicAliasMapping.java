package org.mqttbee.mqtt5.message.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Publish;
import org.mqttbee.mqtt5.message.Mqtt5Topic;

import java.util.HashMap;
import java.util.Random;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicAliasMapping {

    private static final Random random = new Random();

    private final int size;
    private final HashMap<String, Integer> hashMap;
    private int nextTopicAlias;

    public Mqtt5TopicAliasMapping(final int size) {
        this.size = size;
        hashMap = new HashMap<>(size);
        nextTopicAlias = 1;
    }

    int set(@NotNull final Mqtt5Topic topic, @NotNull final Mqtt5Publish.TopicAliasUse topicAliasUse) {
        int topicAlias = Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS;
        if (topicAliasUse != Mqtt5Publish.TopicAliasUse.MUST_NOT) {
            if (nextTopicAlias == size) {
                if (topicAliasUse == Mqtt5Publish.TopicAliasUse.MAY_OVERWRITE) {
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

    int get(@NotNull final Mqtt5Topic topic) {
        final Integer topicAlias = hashMap.get(topic.toString());
        return (topicAlias == null) ? Mqtt5PublishInternal.DEFAULT_NO_TOPIC_ALIAS : topicAlias;
    }

    public boolean contains(@NotNull final Mqtt5Topic topic) {
        return hashMap.get(topic.toString()) != null;
    }

    public int size() {
        return size;
    }

}

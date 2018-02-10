package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicBuilder {

    private final StringBuilder stringBuilder;

    Mqtt5TopicBuilder(@NotNull final String base) {
        stringBuilder = new StringBuilder(base);
    }

    @NotNull
    public Mqtt5TopicBuilder sub(@NotNull final String subTopic) {
        Preconditions.checkNotNull(subTopic);
        stringBuilder.append(Mqtt5Topic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    @NotNull
    public Mqtt5TopicFilterBuilder filter() {
        return new Mqtt5TopicFilterBuilder(stringBuilder.toString());
    }

    @NotNull
    public Mqtt5SharedTopicFilterBuilder share(@NotNull final String shareName) {
        return new Mqtt5SharedTopicFilterBuilder(shareName, stringBuilder.toString());
    }

    @NotNull
    public Mqtt5Topic build() {
        return Mqtt5Topic.from(stringBuilder.toString());
    }

}

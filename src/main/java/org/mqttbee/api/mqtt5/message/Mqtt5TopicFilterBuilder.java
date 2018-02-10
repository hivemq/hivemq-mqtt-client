package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5TopicFilterBuilder {

    final StringBuilder stringBuilder;

    Mqtt5TopicFilterBuilder(@NotNull final String base) {
        stringBuilder = new StringBuilder(base);
    }

    @NotNull
    public Mqtt5TopicFilterBuilder sub(@NotNull final String subTopic) {
        Preconditions.checkNotNull(subTopic);
        stringBuilder.append(Mqtt5Topic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    @NotNull
    public Mqtt5TopicFilterBuilder singleLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(Mqtt5Topic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(Mqtt5TopicFilter.SINGLE_LEVEL_WILDCARD);
        return this;
    }

    @NotNull
    public Mqtt5TopicFilter multiLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(Mqtt5Topic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(Mqtt5TopicFilter.MULTI_LEVEL_WILDCARD);
        return build();
    }

    @NotNull
    public Mqtt5SharedTopicFilterBuilder share(@NotNull final String shareName) {
        return new Mqtt5SharedTopicFilterBuilder(shareName, stringBuilder.toString());
    }

    @NotNull
    public Mqtt5TopicFilter build() {
        return Mqtt5TopicFilter.from(stringBuilder.toString());
    }

}

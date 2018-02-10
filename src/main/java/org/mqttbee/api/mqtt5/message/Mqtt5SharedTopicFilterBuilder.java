package org.mqttbee.api.mqtt5.message;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SharedTopicFilterBuilder extends Mqtt5TopicFilterBuilder {

    private final String shareName;

    Mqtt5SharedTopicFilterBuilder(@NotNull final String shareName, @NotNull final String base) {
        super(base);
        this.shareName = shareName;
    }

    @NotNull
    @Override
    public Mqtt5SharedTopicFilterBuilder sub(@NotNull final String subTopic) {
        return (Mqtt5SharedTopicFilterBuilder) super.sub(subTopic);
    }

    @NotNull
    @Override
    public Mqtt5SharedTopicFilterBuilder singleLevelWildcard() {
        return (Mqtt5SharedTopicFilterBuilder) super.singleLevelWildcard();
    }

    @NotNull
    @Override
    public Mqtt5SharedTopicFilter multiLevelWildcard() {
        return (Mqtt5SharedTopicFilter) super.multiLevelWildcard();
    }

    @NotNull
    public Mqtt5SharedTopicFilter build() {
        return Mqtt5SharedTopicFilter.from(shareName, stringBuilder.toString());
    }

}

package org.mqttbee.api.mqtt.datatypes;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttSharedTopicFilterBuilder extends MqttTopicFilterBuilder {

    private final String shareName;

    MqttSharedTopicFilterBuilder(@NotNull final String shareName, @NotNull final String base) {
        super(base);
        this.shareName = shareName;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder sub(@NotNull final String subTopic) {
        return (MqttSharedTopicFilterBuilder) super.sub(subTopic);
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder singleLevelWildcard() {
        return (MqttSharedTopicFilterBuilder) super.singleLevelWildcard();
    }

    @NotNull
    @Override
    public MqttSharedTopicFilter multiLevelWildcard() {
        return (MqttSharedTopicFilter) super.multiLevelWildcard();
    }

    @NotNull
    public MqttSharedTopicFilter build() {
        return MqttSharedTopicFilter.from(shareName, stringBuilder.toString());
    }

}

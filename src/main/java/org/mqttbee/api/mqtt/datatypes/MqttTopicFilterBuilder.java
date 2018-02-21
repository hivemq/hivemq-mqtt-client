package org.mqttbee.api.mqtt.datatypes;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttTopicFilterBuilder {

    final StringBuilder stringBuilder;

    MqttTopicFilterBuilder(@NotNull final String base) {
        stringBuilder = new StringBuilder(base);
    }

    @NotNull
    public MqttTopicFilterBuilder sub(@NotNull final String subTopic) {
        Preconditions.checkNotNull(subTopic);
        stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    @NotNull
    public MqttTopicFilterBuilder singleLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
        return this;
    }

    @NotNull
    public MqttTopicFilter multiLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.MULTI_LEVEL_WILDCARD);
        return build();
    }

    @NotNull
    public MqttSharedTopicFilterBuilder share(@NotNull final String shareName) {
        return new MqttSharedTopicFilterBuilder(shareName, stringBuilder.toString());
    }

    @NotNull
    public MqttTopicFilter build() {
        return MqttTopicFilter.from(stringBuilder.toString());
    }

}

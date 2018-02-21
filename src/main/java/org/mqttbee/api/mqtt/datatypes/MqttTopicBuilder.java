package org.mqttbee.api.mqtt.datatypes;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttTopicBuilder {

    private final StringBuilder stringBuilder;

    MqttTopicBuilder(@NotNull final String base) {
        stringBuilder = new StringBuilder(base);
    }

    @NotNull
    public MqttTopicBuilder sub(@NotNull final String subTopic) {
        Preconditions.checkNotNull(subTopic);
        stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    @NotNull
    public MqttTopicFilterBuilder filter() {
        return new MqttTopicFilterBuilder(stringBuilder.toString());
    }

    @NotNull
    public MqttSharedTopicFilterBuilder share(@NotNull final String shareName) {
        return new MqttSharedTopicFilterBuilder(shareName, stringBuilder.toString());
    }

    @NotNull
    public MqttTopic build() {
        return MqttTopic.from(stringBuilder.toString());
    }

}

package org.mqttbee.api.mqtt.datatypes;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT Topic Name according to the MQTT specification.
 * <p>
 * A Topic Name has the restrictions from {@link MqttUTF8String}, must be at least 1 character long and mut not contain
 * wildcards ({@link MqttTopicFilter#MULTI_LEVEL_WILDCARD}, {@link MqttTopicFilter#SINGLE_LEVEL_WILDCARD}).
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttTopic extends MqttUTF8String {

    /**
     * The topic level separator character.
     */
    char TOPIC_LEVEL_SEPARATOR = '/';

    /**
     * Validates and creates a Topic Name from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Name.
     * @throws IllegalArgumentException if the string is not a valid Topic Name.
     */
    @NotNull
    static MqttTopic from(@NotNull final String string) {
        return MqttBuilderUtil.topic(string);
    }

    @NotNull
    static MqttTopicBuilder builder(@NotNull final String topTopic) {
        return new MqttTopicBuilder(topTopic);
    }

    @NotNull
    static MqttTopicBuilder extend(@NotNull final MqttTopic topic) {
        return new MqttTopicBuilder(topic.toString());
    }

    /**
     * @return the levels of this Topic Name.
     */
    @NotNull
    ImmutableList<String> getLevels();

}

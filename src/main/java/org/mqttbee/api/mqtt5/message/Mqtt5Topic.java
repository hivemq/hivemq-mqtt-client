package org.mqttbee.api.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttBuilderUtil;

/**
 * MQTT Topic Name according to the MQTT 5 specification.
 * <p>
 * A Topic Name has the restrictions from {@link Mqtt5UTF8String}, must be at least 1 character long and mut not contain
 * wildcards ({@link Mqtt5TopicFilter#MULTI_LEVEL_WILDCARD}, {@link Mqtt5TopicFilter#SINGLE_LEVEL_WILDCARD}).
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Topic extends Mqtt5UTF8String {

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
    static Mqtt5Topic from(@NotNull final String string) {
        return MqttBuilderUtil.topic(string);
    }

    @NotNull
    static Mqtt5TopicBuilder builder(@NotNull final String topTopic) {
        return new Mqtt5TopicBuilder(topTopic);
    }

    @NotNull
    static Mqtt5TopicBuilder extend(@NotNull final Mqtt5Topic topic) {
        return new Mqtt5TopicBuilder(topic.toString());
    }

    /**
     * @return the levels of this Topic Name.
     */
    @NotNull
    ImmutableList<String> getLevels();

}

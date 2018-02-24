package org.mqttbee.api.mqtt.datatypes;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT Topic Filter according to the MQTT specification.
 * <p>
 * A Topic Filter has the restrictions from {@link MqttUTF8String}, must be at least one character long, may contain
 * one {@link #MULTI_LEVEL_WILDCARD} at the end and may contain multiple {@link #SINGLE_LEVEL_WILDCARD}s.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttTopicFilter extends MqttUTF8String {

    /**
     * The multi-level wildcard character.
     */
    char MULTI_LEVEL_WILDCARD = '#';
    /**
     * The single-level wildcard character.
     */
    char SINGLE_LEVEL_WILDCARD = '+';

    /**
     * Validates and creates a Topic Filter from the given string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created Topic Filter.
     * @throws IllegalArgumentException if the string is not a valid Topic Filter.
     */
    @NotNull
    static MqttTopicFilter from(@NotNull final String string) {
        return MqttBuilderUtil.topicFilter(string);
    }

    @NotNull
    static MqttTopicFilterBuilder builder(@NotNull final String topTopic) {
        return new MqttTopicFilterBuilder(topTopic);
    }

    @NotNull
    static MqttTopicFilterBuilder extend(@NotNull final MqttTopicFilter topicFilter) {
        return new MqttTopicFilterBuilder(topicFilter.toString());
    }

    @NotNull
    static MqttTopicFilterBuilder filter(@NotNull final MqttTopic topic) {
        return new MqttTopicFilterBuilder(topic.toString());
    }

    /**
     * @return the levels of this Topic Filter.
     */
    @NotNull
    ImmutableList<String> getLevels();

    /**
     * @return whether this Topic Filter contains wildcards.
     */
    boolean containsWildcards();

    /**
     * @return whether this Topic Filter contains a multi-level wildcard.
     */
    boolean containsMultiLevelWildcard();

    /**
     * @return whether this Topic Filter contains one ore more single-level wildcards.
     */
    boolean containsSingleLevelWildcard();

    /**
     * @return whether this Topic Filter is shared.
     */
    boolean isShared();

}

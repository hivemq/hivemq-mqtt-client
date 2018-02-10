package org.mqttbee.api.mqtt5.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;

/**
 * MQTT Topic Filter according to the MQTT 5 specification.
 * <p>
 * A Topic Filter has the restrictions from {@link Mqtt5UTF8String}, must be at least one character long, may contain
 * one {@link #MULTI_LEVEL_WILDCARD} at the end and may contain multiple {@link #SINGLE_LEVEL_WILDCARD}s.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5TopicFilter extends Mqtt5UTF8String {

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
    static Mqtt5TopicFilter from(@NotNull final String string) {
        Preconditions.checkNotNull(string);

        final Mqtt5TopicFilter topicFilter = Mqtt5TopicFilterImpl.from(string);
        if (topicFilter == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Name.");
        }
        return topicFilter;
    }

    @NotNull
    static Mqtt5TopicFilterBuilder builder(@NotNull final String topTopic) {
        return new Mqtt5TopicFilterBuilder(topTopic);
    }

    @NotNull
    static Mqtt5TopicFilterBuilder extend(@NotNull final Mqtt5TopicFilter topicFilter) {
        return new Mqtt5TopicFilterBuilder(topicFilter.toString());
    }

    @NotNull
    static Mqtt5TopicFilterBuilder filter(@NotNull final Mqtt5Topic topic) {
        return new Mqtt5TopicFilterBuilder(topic.toString());
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

/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.api.mqtt.datatypes;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT Topic Filter according to the MQTT specification.
 * <p>
 * A Topic Filter has the restrictions from {@link MqttUTF8String}, must be at least one character long, may contain one
 * {@link #MULTI_LEVEL_WILDCARD} at the end and may contain multiple {@link #SINGLE_LEVEL_WILDCARD}s.
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
    static @NotNull MqttTopicFilter from(final @NotNull String string) {
        return MqttBuilderUtil.topicFilter(string);
    }

    static @NotNull MqttTopicFilterBuilder builder() {
        return new MqttTopicFilterImplBuilder.Default();
    }

    static @NotNull MqttTopicFilterBuilder.Complete extend(final @NotNull MqttTopicFilter topicFilter) {
        return new MqttTopicFilterImplBuilder.Default(topicFilter.toString());
    }

    static @NotNull MqttTopicFilterBuilder.Complete filter(final @NotNull MqttTopic topic) {
        return new MqttTopicFilterImplBuilder.Default(topic.toString());
    }

    /**
     * @return the levels of this Topic Filter.
     */
    @NotNull ImmutableList<String> getLevels();

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

    /**
     * Matches this Topic Filter with the given Topic Name.
     *
     * @param topic the Topic Name to match.
     * @return true if this Topic Filter matches the Topic Name, otherwise false.
     */
    boolean matches(@NotNull MqttTopic topic);

    /**
     * Matches this Topic Filter with the given Topic Filter.
     *
     * @param topicFilter the Topic Filter to match.
     * @return true if this Topic Filter matches the given Topic Filter, otherwise false.
     */
    boolean matches(@NotNull MqttTopicFilter topicFilter);
}

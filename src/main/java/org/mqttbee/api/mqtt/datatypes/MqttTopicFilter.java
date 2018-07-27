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
import org.mqttbee.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT Topic Filter according to the MQTT specification.
 * <p>
 * A Topic Filter has the restrictions from {@link MqttUtf8String}, must be at least one character long, may contain
 * one {@link #MULTI_LEVEL_WILDCARD} at the end and may contain multiple {@link #SINGLE_LEVEL_WILDCARD}s.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttTopicFilter extends MqttUtf8String {

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
    static MqttTopicFilterBuilder<Void> builder(@NotNull final String topTopic) {
        return new MqttTopicFilterBuilder<>(topTopic, null);
    }

    @NotNull
    static MqttTopicFilterBuilder<Void> extend(@NotNull final MqttTopicFilter topicFilter) {
        return new MqttTopicFilterBuilder<>(topicFilter.toString(), null);
    }

    @NotNull
    static MqttTopicFilterBuilder<Void> filter(@NotNull final MqttTopic topic) {
        return new MqttTopicFilterBuilder<>(topic.toString(), null);
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

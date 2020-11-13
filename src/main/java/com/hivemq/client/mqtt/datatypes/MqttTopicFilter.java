/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.mqtt.datatypes;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MQTT Topic Filter according to the MQTT specification.
 * <p>
 * A Topic Filter has the same requirements as an {@link MqttUtf8String UTF-8 encoded string}. Additionally it
 * <ul>
 *   <li>must be at least one character long,
 *   <li>may contain one multi-level wildcard character ({@value #MULTI_LEVEL_WILDCARD}) at the end and
 *   <li>may contain multiple single-level wildcards ({@value #SINGLE_LEVEL_WILDCARD}).
 * </ul>
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
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
     * Validates and creates a Topic Filter of the given string.
     *
     * @param string the string representation of the Topic Filter.
     * @return the created Topic Filter.
     * @throws IllegalArgumentException if the string is not a valid Topic Filter.
     */
    static @NotNull MqttTopicFilter of(final @NotNull String string) {
        return MqttTopicFilterImpl.of(string);
    }

    /**
     * Creates a builder for a Topic Filter.
     *
     * @return the created builder.
     */
    static @NotNull MqttTopicFilterBuilder builder() {
        return new MqttTopicFilterImplBuilder.Default();
    }

    /**
     * @return the levels of this Topic Filter.
     */
    @Immutable @NotNull List<@NotNull String> getLevels();

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
     * @param shareName the string representation of the Share Name.
     * @return a Shared Topic Filter which shares this Topic Filter with the given Share Name.
     */
    @NotNull MqttSharedTopicFilter share(@NotNull String shareName);

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

    /**
     * Creates a builder for extending this Topic Filter.
     *
     * @return the created builder.
     */
    MqttTopicFilterBuilder.@NotNull Complete extend();
}

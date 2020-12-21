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

package com.hivemq.client2.mqtt.datatypes;

import com.hivemq.client2.internal.mqtt.datatypes.MqttSharedTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT Shared Topic Filter according to the MQTT specification.
 * <p>
 * A Shared Topic Filter consists of the share prefix ({@value #SHARE_PREFIX}), a Share Name and a Topic Filter.
 * <p>
 * The Share Name has the same requirements as an {@link MqttUtf8String UTF-8 encoded string}. Additionally it
 * <ul>
 *   <li>must be at least 1 character long,
 *   <li>must not contain wildcard characters ({@value MqttTopicFilter#MULTI_LEVEL_WILDCARD}, {@value
 *     MqttTopicFilter#SINGLE_LEVEL_WILDCARD}) and
 *   <li>must not contain a topic level separator ({@value MqttTopic#TOPIC_LEVEL_SEPARATOR})
 * </ul>
 * <p>
 * The Topic Filter has the same restrictions as a {@link MqttTopicFilter Topic Filter}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface MqttSharedTopicFilter extends MqttTopicFilter {

    /**
     * The prefix of a Shared Topic Filter.
     */
    @NotNull String SHARE_PREFIX = "$share" + MqttTopic.TOPIC_LEVEL_SEPARATOR;

    /**
     * Validates and creates a Shared Topic Filter of the given Share Name and Topic Filter.
     *
     * @param shareName   the string representation of the Share Name.
     * @param topicFilter the string representation of the Topic Filter.
     * @return the created Shared Topic Filter.
     * @throws IllegalArgumentException if the Share Name string is not a valid Share Name or the Topic Filter string is
     *                                  not a valid Topic Filter.
     */
    static @NotNull MqttSharedTopicFilter of(final @NotNull String shareName, final @NotNull String topicFilter) {
        return MqttSharedTopicFilterImpl.of(shareName, topicFilter);
    }

    /**
     * Creates a builder for a Shared Topic Filter.
     *
     * @param shareName the string representation of the Share Name.
     * @return the created builder.
     */
    static @NotNull MqttSharedTopicFilterBuilder builder(final @NotNull String shareName) {
        return new MqttTopicFilterImplBuilder.SharedDefault(shareName);
    }

    /**
     * @return the Share Name of this Shared Topic Filter as a string.
     */
    @NotNull String getShareName();

    /**
     * @return the Topic Filter of this Shared Topic Filter.
     */
    @NotNull MqttTopicFilter getTopicFilter();

    /**
     * Creates a builder for extending this Shared Topic Filter.
     *
     * @return the created builder.
     */
    MqttSharedTopicFilterBuilder.@NotNull Complete extendShared();
}

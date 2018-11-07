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

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImplBuilder;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT Shared Topic Filter according to the MQTT specification.
 * <p>
 * A Shared Topic Filter consists of the {@link #SHARE_PREFIX} a Share Name and a Topic Filter. The Share Name has the
 * restrictions from {@link MqttUTF8String}, must be at least 1 character long and mut not contain wildcards ({@link
 * MqttTopicFilter#MULTI_LEVEL_WILDCARD}, {@link MqttTopicFilter#SINGLE_LEVEL_WILDCARD}) and the {@link
 * MqttTopic#TOPIC_LEVEL_SEPARATOR}. The Topic Filter has the same restrictions from {@link MqttTopicFilter}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttSharedTopicFilter extends MqttTopicFilter {

    /**
     * The prefix of a Shared Topic Filter.
     */
    @NotNull String SHARE_PREFIX = "$share" + MqttTopic.TOPIC_LEVEL_SEPARATOR;

    /**
     * Validates and creates a Shared Topic Filter from the given Share Name and the given Topic Filter.
     *
     * @param shareName   the Share Name.
     * @param topicFilter the Topic Filter.
     * @return the created Shared Topic Filter.
     * @throws IllegalArgumentException if the Share Name is not a valid Share Name or the Topic Filter is not a valid
     *                                  Topic Filter.
     */
    static @NotNull MqttSharedTopicFilter from(final @NotNull String shareName, final @NotNull String topicFilter) {
        return MqttBuilderUtil.sharedTopicFilter(shareName, topicFilter);
    }

    static @NotNull MqttSharedTopicFilterBuilder builder(final @NotNull String shareName) {
        return new MqttTopicFilterImplBuilder.SharedDefault(shareName);
    }

    static @NotNull MqttSharedTopicFilterBuilder.Complete extend(
            final @NotNull MqttSharedTopicFilter sharedTopicFilter) {

        return new MqttTopicFilterImplBuilder.SharedDefault(
                sharedTopicFilter.getShareName(), sharedTopicFilter.getTopicFilter());
    }

    static @NotNull MqttSharedTopicFilterBuilder.Complete share(
            final @NotNull String shareName, final @NotNull MqttTopicFilter topicFilter) {

        return new MqttTopicFilterImplBuilder.SharedDefault(shareName, topicFilter.toString());
    }

    static @NotNull MqttSharedTopicFilterBuilder.Complete share(
            final @NotNull String shareName, final @NotNull MqttTopic topic) {

        return new MqttTopicFilterImplBuilder.SharedDefault(shareName, topic.toString());
    }

    /**
     * @return the Share Name of this Shared Topic Filter as a string.
     */
    @NotNull String getShareName();

    /**
     * @return the Topic Filter of this Shared Topic Filter as a string.
     */
    @NotNull String getTopicFilter();
}

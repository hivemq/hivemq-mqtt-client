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

import org.mqttbee.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT Shared Topic Filter according to the MQTT specification.
 * <p>
 * A Shared Topic Filter consists of the {@link #SHARE_PREFIX} a Share Name and a Topic Filter. The Share Name has the
 * restrictions from {@link MqttUtf8String}, must be at least 1 character long and mut not contain wildcards ({@link
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
    String SHARE_PREFIX = "$share" + MqttTopic.TOPIC_LEVEL_SEPARATOR;

    /**
     * Validates and creates a Shared Topic Filter from the given Share Name and the given Topic Filter.
     *
     * @param shareName   the Share Name.
     * @param topicFilter the Topic Filter.
     * @return the created Shared Topic Filter.
     * @throws IllegalArgumentException if the Share Name is not a valid Share Name or the Topic Filter is not a valid
     *                                  Topic Filter.
     */
    @NotNull
    static MqttSharedTopicFilter from(@NotNull final String shareName, @NotNull final String topicFilter) {
        return MqttBuilderUtil.sharedTopicFilter(shareName, topicFilter);
    }

    @NotNull
    static MqttSharedTopicFilterBuilder<Void> builder(@NotNull final String shareName, @NotNull final String topTopic) {
        return new MqttSharedTopicFilterBuilder<>(shareName, topTopic, null);
    }

    @NotNull
    static MqttSharedTopicFilterBuilder<Void> extend(@NotNull final MqttSharedTopicFilter sharedTopicFilter) {
        return new MqttSharedTopicFilterBuilder<>(
                sharedTopicFilter.getShareName(), sharedTopicFilter.getTopicFilter(), null);
    }

    @NotNull
    static MqttSharedTopicFilterBuilder<Void> share(
            @NotNull final String shareName, @NotNull final MqttTopicFilter topicFilter) {

        return new MqttSharedTopicFilterBuilder<>(shareName, topicFilter.toString(), null);
    }

    @NotNull
    static MqttSharedTopicFilterBuilder<Void> share(@NotNull final String shareName, @NotNull final MqttTopic topic) {
        return new MqttSharedTopicFilterBuilder<>(shareName, topic.toString(), null);
    }

    /**
     * @return the Share Name of this Shared Topic Filter as a string.
     */
    String getShareName();

    /**
     * @return the Topic Filter of this Shared Topic Filter as a string.
     */
    String getTopicFilter();

}

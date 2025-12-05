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

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.datatypes.MqttQueueTopicFilterImpl;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT Queue Topic Filter according to the MQTT specification.
 * <p>
 * A Queue Topic Filter consists of the Queue prefix ({@value #QUEUE_PREFIX}) and a Topic Filter.
 * <p>
 * The Topic Filter has the same restrictions as a {@link MqttTopicFilter Topic Filter}.
 *
 * @author Jean-François Côté
 * @since 1.0
 */
@DoNotImplement
public interface MqttQueueTopicFilter extends MqttTopicFilter {

    /**
     * The prefix of a Queue Topic Filter.
     */
    @NotNull String QUEUE_PREFIX = "$q" + MqttTopic.TOPIC_LEVEL_SEPARATOR;

    /**
     * Validates and creates a Queue Topic Filter of the given Topic Filter.
     *
     * @param topicFilter the string representation of the Topic Filter.
     * @return the created Queue Topic Filter.
     * @throws IllegalArgumentException if the Topic Filter string is
     *                                  not a valid Topic Filter.
     */
    static @NotNull MqttQueueTopicFilter of(final @NotNull String topicFilter) {
        return MqttQueueTopicFilterImpl.of(topicFilter);
    }

    /**
     * @return the Topic Filter of this Queue Topic Filter.
     */
    @NotNull MqttTopicFilter getTopicFilter();

    /**
     * Creates a builder for extending this Queue Topic Filter.
     *
     * @return the created builder.
     */
    MqttTopicFilterBuilder.@NotNull Complete extendQueue();
}

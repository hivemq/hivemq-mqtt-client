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
import org.mqttbee.mqtt.datatypes.MqttTopicImplBuilder;
import org.mqttbee.mqtt.util.MqttChecks;

/**
 * MQTT Topic Name according to the MQTT specification.
 * <p>
 * A Topic Name has the restrictions from {@link MqttUtf8String}, must be at least 1 character long and mut not contain
 * wildcards ({@link MqttTopicFilter#MULTI_LEVEL_WILDCARD}, {@link MqttTopicFilter#SINGLE_LEVEL_WILDCARD}).
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttTopic extends MqttUtf8String {

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
    static @NotNull MqttTopic from(final @NotNull String string) {
        return MqttChecks.topicNotNull(string);
    }

    static @NotNull MqttTopicBuilder builder() {
        return new MqttTopicImplBuilder.Default();
    }

    static @NotNull MqttTopicBuilder.Complete extend(final @NotNull MqttTopic topic) {
        return new MqttTopicImplBuilder.Default(topic.toString());
    }

    /**
     * @return the levels of this Topic Name.
     */
    @NotNull ImmutableList<String> getLevels();
}

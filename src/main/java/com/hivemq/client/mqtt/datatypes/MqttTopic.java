/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.mqtt.datatypes;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImplBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MQTT Topic Name according to the MQTT specification.
 * <p>
 * A Topic Name has the same requirements as an {@link MqttUtf8String UTF-8 encoded string}. Additionally it
 * <ul>
 * <li>must be at least 1 character long and</li>
 * <li>must not contain wildcard characters ({@value MqttTopicFilter#MULTI_LEVEL_WILDCARD}, {@value
 * MqttTopicFilter#SINGLE_LEVEL_WILDCARD}).</li>
 * </ul>
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttTopic extends MqttUtf8String {

    /**
     * The topic level separator character.
     */
    char TOPIC_LEVEL_SEPARATOR = '/';

    /**
     * Validates and creates a Topic Name of the given string.
     *
     * @param string the string representation of the Topic Name.
     * @return the created Topic Name.
     * @throws IllegalArgumentException if the string is not a valid Topic Name.
     */
    static @NotNull MqttTopic of(final @NotNull String string) {
        return MqttTopicImpl.of(string);
    }

    /**
     * Creates a builder for a Topic Name.
     *
     * @return the created builder.
     */
    static @NotNull MqttTopicBuilder builder() {
        return new MqttTopicImplBuilder.Default();
    }

    /**
     * @return the levels of this Topic Name.
     */
    @Immutable @NotNull List<@NotNull String> getLevels();

    /**
     * @return a Topic Filter matching only this Topic Name.
     */
    @NotNull MqttTopicFilter filter();

    /**
     * Creates a builder for extending this Topic Name.
     *
     * @return the created builder.
     */
    @NotNull MqttTopicBuilder.Complete extend();
}

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

package util.implementations;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.mqtt.datatypes.*;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Michael Walter
 */
public class CustomMqttTopicFilter implements MqttTopicFilter {

    @Override
    public @Immutable @NotNull List<@NotNull String> getLevels() {
        return null;
    }

    @Override
    public boolean containsWildcards() {
        return false;
    }

    @Override
    public boolean containsMultiLevelWildcard() {
        return false;
    }

    @Override
    public boolean containsSingleLevelWildcard() {
        return false;
    }

    @Override
    public boolean isShared() {
        return false;
    }

    @Override
    public @NotNull MqttSharedTopicFilter share(@NotNull String shareName) {
        return null;
    }

    @Override
    public boolean matches(@NotNull MqttTopic topic) {
        return false;
    }

    @Override
    public boolean matches(@NotNull MqttTopicFilter topicFilter) {
        return false;
    }

    @Override
    public @NotNull MqttTopicFilterBuilder.Complete extend() {
        return null;
    }

    @Override
    public boolean containsShouldNotCharacters() {
        return false;
    }

    @Override
    public @NotNull ByteBuffer toByteBuffer() {
        return null;
    }

    @Override
    public int compareTo(@NotNull MqttUtf8String o) {
        return 0;
    }
}
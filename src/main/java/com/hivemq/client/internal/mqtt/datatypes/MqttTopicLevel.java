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

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.util.ByteArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Single topic or topic filter level. May be the single level wildcard but must not be the multi level wildcard (the
 * multi level wildcard does not represent a topic level).
 *
 * @author Silvio Giebl
 */
public class MqttTopicLevel extends ByteArray {

    private static final @NotNull MqttTopicLevel SINGLE_LEVEL_WILDCARD =
            new MqttTopicLevel(new byte[]{MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD});

    static @NotNull MqttTopicLevel of(final @NotNull byte[] array, final int start, final int end) {
        if (isSingleLevelWildcard(array, start, end)) {
            return MqttTopicLevel.SINGLE_LEVEL_WILDCARD;
        }
        return new MqttTopicLevel(Arrays.copyOfRange(array, start, end));
    }

    private static boolean isSingleLevelWildcard(final @NotNull byte[] array, final int start, final int end) {
        return ((end - start) == 1) && (array[start] == MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD);
    }

    MqttTopicLevel(final @NotNull byte[] array) {
        super(array);
    }

    @NotNull byte[] getArray() {
        return array;
    }

    public boolean isSingleLevelWildcard() {
        return isSingleLevelWildcard(array, getStart(), getEnd());
    }

    public @NotNull MqttTopicLevel trim() {
        return this;
    }

    public @Nullable MqttTopicFilterImpl toFilter(final @Nullable byte[] prefix, final boolean multiLevelWildcard) {
        final byte[] bytes;
        if (prefix != null) {
            if (multiLevelWildcard) {
                bytes = new byte[prefix.length + 1 + array.length + 2];
                bytes[bytes.length - 2] = MqttTopicImpl.TOPIC_LEVEL_SEPARATOR;
                bytes[bytes.length - 1] = MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD;
            } else {
                bytes = new byte[prefix.length + 1 + array.length];
            }
            System.arraycopy(prefix, 0, bytes, 0, prefix.length);
            bytes[prefix.length] = MqttTopicImpl.TOPIC_LEVEL_SEPARATOR;
            System.arraycopy(array, 0, bytes, prefix.length + 1, array.length);
        } else if (multiLevelWildcard) {
            bytes = new byte[array.length + 2];
            System.arraycopy(array, 0, bytes, 0, array.length);
            bytes[bytes.length - 2] = MqttTopicImpl.TOPIC_LEVEL_SEPARATOR;
            bytes[bytes.length - 1] = MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD;
        } else {
            bytes = array;
        }
        return MqttTopicFilterImpl.of(bytes);
    }
}

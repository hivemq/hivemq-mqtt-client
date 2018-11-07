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

package org.mqttbee.mqtt.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.*;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.util.Checks;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttBuilderUtil {

    public static @NotNull MqttUTF8StringImpl string(final @NotNull String string) {
        Checks.notNull(string, "String");
        final MqttUTF8StringImpl from = MqttUTF8StringImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid UTF-8 encoded String.");
        }
        return from;
    }

    public static @NotNull MqttUTF8StringImpl string(final @NotNull MqttUTF8String string) {
        Checks.notNull(string, "String");
        return MustNotBeImplementedUtil.checkNotImplemented(string, MqttUTF8StringImpl.class);
    }

    public static @Nullable MqttUTF8StringImpl stringOrNull(final @Nullable String string) {
        return (string == null) ? null : string(string);
    }

    public static @Nullable MqttUTF8StringImpl stringOrNull(final @Nullable MqttUTF8String string) {
        return MustNotBeImplementedUtil.checkNullOrNotImplemented(string, MqttUTF8StringImpl.class);
    }

    public static @NotNull MqttTopicImpl topic(final @NotNull String string) {
        Checks.notNull(string, "String");
        final MqttTopicImpl from = MqttTopicImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Name.");
        }
        return from;
    }

    public static @NotNull MqttTopicImpl topic(final @NotNull MqttTopic topic) {
        Checks.notNull(topic, "Topic");
        return MustNotBeImplementedUtil.checkNotImplemented(topic, MqttTopicImpl.class);
    }

    public static @Nullable MqttTopicImpl topicOrNull(final @Nullable String string) {
        return (string == null) ? null : topic(string);
    }

    public static @Nullable MqttTopicImpl topicOrNull(final @Nullable MqttTopic topic) {
        return MustNotBeImplementedUtil.checkNullOrNotImplemented(topic, MqttTopicImpl.class);
    }

    public static @NotNull MqttTopicFilterImpl topicFilter(final @NotNull String string) {
        Checks.notNull(string, "String");
        final MqttTopicFilterImpl from = MqttTopicFilterImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Filter.");
        }
        return from;
    }

    public static @NotNull MqttTopicFilterImpl topicFilter(final @NotNull MqttTopicFilter topicFilter) {
        Checks.notNull(topicFilter, "Topic filter");
        return MustNotBeImplementedUtil.checkNotImplemented(topicFilter, MqttTopicFilterImpl.class);
    }

    public static @NotNull MqttSharedTopicFilterImpl sharedTopicFilter(
            final @NotNull String shareName, final @NotNull String topicFilter) {

        Checks.notNull(shareName, "Share name");
        Checks.notNull(topicFilter, "Topic filter");
        final MqttSharedTopicFilterImpl sharedTopicFilter = MqttSharedTopicFilterImpl.from(shareName, topicFilter);
        if (sharedTopicFilter == null) {
            throw new IllegalArgumentException(
                    "The string: [" + MqttSharedTopicFilter.SHARE_PREFIX + shareName + MqttTopic.TOPIC_LEVEL_SEPARATOR +
                            topicFilter + "] is not a valid Shared Topic Filter.");
        }
        return sharedTopicFilter;
    }

    public static @NotNull MqttClientIdentifierImpl clientIdentifier(final @NotNull String string) {
        Checks.notNull(string, "String");
        final MqttClientIdentifierImpl from = MqttClientIdentifierImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Client Identifier.");
        }
        return from;
    }

    public static @NotNull MqttClientIdentifierImpl clientIdentifier(
            final @NotNull MqttClientIdentifier clientIdentifier) {

        Checks.notNull(clientIdentifier, "Client identifier");
        return MustNotBeImplementedUtil.checkNotImplemented(clientIdentifier, MqttClientIdentifierImpl.class);
    }

    public static @Nullable ByteBuffer binaryDataOrNull(final @Nullable byte[] binary) {
        if (binary == null) {
            return null;
        }
        Preconditions.checkArgument(MqttBinaryData.isInRange(binary),
                "Cannot encode given byte array as binary data. Byte array to long. Found: %s bytes. Maximum length is %s.",
                binary.length, MqttBinaryData.MAX_LENGTH);
        return ByteBuffer.wrap(binary);
    }

    public static @Nullable ByteBuffer binaryDataOrNull(final @Nullable ByteBuffer binary) {
        if (binary == null) {
            return null;
        }
        Preconditions.checkArgument(MqttBinaryData.isInRange(binary),
                "Cannot encode given byte buffer as binary data. Too many remaining bytes in byte buffer. Found: %s bytes. Maximum length is %s.",
                binary.remaining(), MqttBinaryData.MAX_LENGTH);

        return binary.slice();
    }

    public static @NotNull MqttUserPropertiesImpl userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        return MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
    }

    public static @NotNull MqttUserPropertyImpl userProperty(final @NotNull String name, final @NotNull String value) {
        return MqttUserPropertyImpl.of(string(name), string(value));
    }

    public static @NotNull MqttUserPropertyImpl userProperty(
            final @NotNull MqttUTF8String name, final @NotNull MqttUTF8String value) {

        return MqttUserPropertyImpl.of(string(name), string(value));
    }

    public static @NotNull MqttUserPropertyImpl userProperty(final @NotNull Mqtt5UserProperty userProperty) {
        return MustNotBeImplementedUtil.checkNotImplemented(userProperty, MqttUserPropertyImpl.class);
    }

    public static @Nullable MqttWillPublish willPublish(final @Nullable MqttPublish publish) {
        if (publish == null) {
            return null;
        }
        if (publish instanceof MqttWillPublish) {
            return (MqttWillPublish) publish;
        }
        return (MqttWillPublish) Mqtt5WillPublish.extend(publish).build();
    }
}

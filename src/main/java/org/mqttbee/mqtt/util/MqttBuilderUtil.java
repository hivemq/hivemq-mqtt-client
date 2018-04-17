/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.util;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.*;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttBuilderUtil {

    @NotNull
    public static MqttUTF8StringImpl string(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final MqttUTF8StringImpl from = MqttUTF8StringImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid UTF-8 encoded String.");
        }
        return from;
    }

    @NotNull
    public static MqttUTF8StringImpl string(@NotNull final MqttUTF8String string) {
        Preconditions.checkNotNull(string);
        return MustNotBeImplementedUtil.checkNotImplemented(string, MqttUTF8StringImpl.class);
    }

    @Nullable
    public static MqttUTF8StringImpl stringOrNull(@Nullable final String string) {
        return (string == null) ? null : string(string);
    }

    @Nullable
    public static MqttUTF8StringImpl stringOrNull(@Nullable final MqttUTF8String string) {
        return MustNotBeImplementedUtil.checkNullOrNotImplemented(string, MqttUTF8StringImpl.class);
    }

    @NotNull
    public static MqttTopicImpl topic(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final MqttTopicImpl from = MqttTopicImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Name.");
        }
        return from;
    }

    @NotNull
    public static MqttTopicImpl topic(@NotNull final MqttTopic topic) {
        Preconditions.checkNotNull(topic);
        return MustNotBeImplementedUtil.checkNotImplemented(topic, MqttTopicImpl.class);
    }

    @Nullable
    public static MqttTopicImpl topicOrNull(@Nullable final String string) {
        return (string == null) ? null : topic(string);
    }

    @Nullable
    public static MqttTopicImpl topicOrNull(@Nullable final MqttTopic topic) {
        return MustNotBeImplementedUtil.checkNullOrNotImplemented(topic, MqttTopicImpl.class);
    }

    @NotNull
    public static MqttTopicFilterImpl topicFilter(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final MqttTopicFilterImpl from = MqttTopicFilterImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Topic Filter.");
        }
        return from;
    }

    @NotNull
    public static MqttTopicFilterImpl topicFilter(@NotNull final MqttTopicFilter topic) {
        Preconditions.checkNotNull(topic);
        return MustNotBeImplementedUtil.checkNotImplemented(topic, MqttTopicFilterImpl.class);
    }

    @NotNull
    public static MqttSharedTopicFilterImpl sharedTopicFilter(
            @NotNull final String shareName, @NotNull final String topicFilter) {

        Preconditions.checkNotNull(shareName);
        Preconditions.checkNotNull(topicFilter);
        final MqttSharedTopicFilterImpl sharedTopicFilter = MqttSharedTopicFilterImpl.from(shareName, topicFilter);
        if (sharedTopicFilter == null) {
            throw new IllegalArgumentException(
                    "The string: [" + MqttSharedTopicFilter.SHARE_PREFIX + shareName + topicFilter +
                            "] is not a valid Shared Topic Filter.");
        }
        return sharedTopicFilter;
    }

    @NotNull
    public static MqttClientIdentifierImpl clientIdentifier(@NotNull final String string) {
        Preconditions.checkNotNull(string);
        final MqttClientIdentifierImpl from = MqttClientIdentifierImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + string + "] is not a valid Client Identifier.");
        }
        return from;
    }

    @NotNull
    public static MqttClientIdentifierImpl clientIdentifier(@NotNull final MqttClientIdentifier clientIdentifier) {
        Preconditions.checkNotNull(clientIdentifier);
        return MustNotBeImplementedUtil.checkNotImplemented(clientIdentifier, MqttClientIdentifierImpl.class);
    }

    @Nullable
    public static ByteBuffer binaryDataOrNull(@Nullable final byte[] binary) {
        if (binary == null) {
            return null;
        }
        Preconditions.checkArgument(MqttBinaryData.isInRange(binary));
        return ByteBufferUtil.wrap(binary);
    }

    @Nullable
    public static ByteBuffer binaryDataOrNull(@Nullable final ByteBuffer binary) {
        if (binary == null) {
            return null;
        }
        Preconditions.checkArgument(MqttBinaryData.isInRange(binary));
        return ByteBufferUtil.slice(binary);
    }

    @NotNull
    public static MqttUserPropertiesImpl userProperties(@NotNull final Mqtt5UserProperties userProperties) {
        return MustNotBeImplementedUtil.checkNotImplemented(userProperties, MqttUserPropertiesImpl.class);
    }

}

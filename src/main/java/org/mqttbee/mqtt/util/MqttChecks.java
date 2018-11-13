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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.*;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishBuilder;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.util.Checks;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class MqttChecks {

    private MqttChecks() {}

    private static @NotNull MqttUTF8StringImpl string(
            final @NotNull String string, final @NotNull String name) {

        final MqttUTF8StringImpl from = MqttUTF8StringImpl.from(string);
        if (from == null) {
            throw new IllegalArgumentException(name + " [" + string + "] is not a valid UTF-8 encoded String.");
        }
        return from;
    }

    public static @NotNull MqttUTF8StringImpl stringNotNull(final @Nullable String string, final @NotNull String name) {
        return string(Checks.notNull(string, name), name);
    }

    public static @NotNull MqttUTF8StringImpl stringNotNull(
            final @Nullable MqttUTF8String string, final @NotNull String name) {

        return Checks.notImplemented(string, MqttUTF8StringImpl.class, name);
    }

    public static @Nullable MqttUTF8StringImpl stringOrNull(final @Nullable String string, final @NotNull String name) {
        return (string == null) ? null : string(string, name);
    }

    public static @Nullable MqttUTF8StringImpl stringOrNull(
            final @Nullable MqttUTF8String string, final @NotNull String name) {

        return Checks.notImplementedOrNull(string, MqttUTF8StringImpl.class, name);
    }

    public static @Nullable MqttUTF8StringImpl reasonString(final @Nullable String reasonString) {
        return stringOrNull(reasonString, "Reason string");
    }

    public static @Nullable MqttUTF8StringImpl reasonString(final @Nullable MqttUTF8String reasonString) {
        return stringOrNull(reasonString, "Reason string");
    }

    public static @NotNull MqttTopicImpl topic(final @NotNull String topic, final @NotNull String name) {
        final MqttTopicImpl from = MqttTopicImpl.from(topic);
        if (from == null) {
            throw new IllegalArgumentException(name + " [" + topic + "] is not a valid Topic Name.");
        }
        return from;
    }

    public static @NotNull MqttTopicImpl topic(final @NotNull String topic) {
        return topic(topic, "Topic");
    }

    public static @NotNull MqttTopicImpl topicNotNull(final @Nullable String topic) {
        return topic(Checks.notNull(topic, "Topic"), "Topic");
    }

    public static @NotNull MqttTopicImpl topicNotNull(final @Nullable MqttTopic topic) {
        return Checks.notImplemented(topic, MqttTopicImpl.class, "Topic");
    }

    public static @NotNull MqttTopicFilterImpl topicFilter(final @NotNull String topicFilter) {
        final MqttTopicFilterImpl from = MqttTopicFilterImpl.from(topicFilter);
        if (from == null) {
            throw new IllegalArgumentException("The string: [" + topicFilter + "] is not a valid Topic Filter.");
        }
        return from;
    }

    public static @NotNull MqttTopicFilterImpl topicFilterNotNull(final @Nullable String topicFilter) {
        return topicFilter(Checks.notNull(topicFilter, "Topic Filter"));
    }

    public static @NotNull MqttTopicFilterImpl topicFilterNotNull(final @Nullable MqttTopicFilter topicFilter) {
        return Checks.notImplemented(topicFilter, MqttTopicFilterImpl.class, "Topic filter");
    }

    public static @NotNull MqttSharedTopicFilterImpl sharedTopicFilter(
            final @NotNull String shareName, final @NotNull String topicFilter) {

        final MqttSharedTopicFilterImpl sharedTopicFilter = MqttSharedTopicFilterImpl.from(shareName, topicFilter);
        if (sharedTopicFilter == null) {
            throw new IllegalArgumentException(
                    "The string: [" + MqttSharedTopicFilter.SHARE_PREFIX + shareName + MqttTopic.TOPIC_LEVEL_SEPARATOR +
                            topicFilter + "] is not a valid Shared Topic Filter.");
        }
        return sharedTopicFilter;
    }

    public static @NotNull MqttSharedTopicFilterImpl sharedTopicFilterNotNull(
            final @Nullable String shareName, final @Nullable String topicFilter) {

        return sharedTopicFilter(Checks.notNull(shareName, "Share name"), Checks.notNull(topicFilter, "Topic filter"));
    }

    public static @NotNull MqttClientIdentifierImpl clientIdentifier(final @Nullable String clientIdentifier) {
        Checks.notNull(clientIdentifier, "Client identifier");
        final MqttClientIdentifierImpl from = MqttClientIdentifierImpl.from(clientIdentifier);
        if (from == null) {
            throw new IllegalArgumentException(
                    "The string: [" + clientIdentifier + "] is not a valid Client Identifier.");
        }
        return from;
    }

    public static @NotNull MqttClientIdentifierImpl clientIdentifier(
            final @Nullable MqttClientIdentifier clientIdentifier) {

        return Checks.notImplemented(clientIdentifier, MqttClientIdentifierImpl.class, "Client identifier");
    }

    private static @NotNull ByteBuffer binaryData(final @NotNull byte[] binary, final @NotNull String name) {
        if (!MqttBinaryData.isInRange(binary)) {
            throw new IllegalArgumentException(
                    name + " can not be encoded as binary data. Maximum length is: " + MqttBinaryData.MAX_LENGTH +
                            " bytes, but was: " + binary.length + " bytes");
        }
        return ByteBuffer.wrap(binary);
    }

    public static @NotNull ByteBuffer binaryDataNotNull(final @Nullable byte[] binary, final @NotNull String name) {
        return binaryData(Checks.notNull(binary, name), name);
    }

    public static @Nullable ByteBuffer binaryDataOrNull(final @Nullable byte[] binary, final @NotNull String name) {
        return (binary == null) ? null : binaryData(binary, name);
    }

    private static @NotNull ByteBuffer binaryData(final @NotNull ByteBuffer binary, final @NotNull String name) {
        if (!MqttBinaryData.isInRange(binary)) {
            throw new IllegalArgumentException(
                    name + " can not be encoded as binary data. Maximum length is: " + MqttBinaryData.MAX_LENGTH +
                            " bytes, but was: " + binary.remaining() + " bytes");
        }
        return binary.slice();
    }

    public static @NotNull ByteBuffer binaryDataNotNull(final @Nullable ByteBuffer binary, final @NotNull String name) {
        return binaryData(Checks.notNull(binary, name), name);
    }

    public static @Nullable ByteBuffer binaryDataOrNull(final @Nullable ByteBuffer binary, final @NotNull String name) {
        return (binary == null) ? null : binaryData(binary, name);
    }

    public static @NotNull MqttUserPropertiesImpl userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        return Checks.notImplemented(userProperties, MqttUserPropertiesImpl.class, "User properties");
    }

    public static @NotNull MqttUserPropertyImpl userProperty(
            final @Nullable String name, final @Nullable String value) {
        return MqttUserPropertyImpl.of(
                stringNotNull(name, "User property name"), stringNotNull(value, "User property value"));
    }

    public static @NotNull MqttUserPropertyImpl userProperty(
            final @Nullable MqttUTF8String name, final @Nullable MqttUTF8String value) {

        return MqttUserPropertyImpl.of(
                stringNotNull(name, "User property name"), stringNotNull(value, "User property value"));
    }

    public static @NotNull MqttUserPropertyImpl userProperty(final @Nullable Mqtt5UserProperty userProperty) {
        return Checks.notImplemented(userProperty, MqttUserPropertyImpl.class, "User property");
    }

    public static @NotNull MqttConnect connect(final @Nullable Mqtt5Connect connect) {
        return Checks.notImplemented(connect, MqttConnect.class, "Connect");
    }

    public static @NotNull MqttConnect connect(final @Nullable Mqtt3Connect connect) {
        return Checks.notImplemented(connect, Mqtt3ConnectView.class, "Connect").getDelegate();
    }

    public static @NotNull MqttWillPublish willPublish(final @NotNull MqttPublish publish) {
        if (publish instanceof MqttWillPublish) {
            return (MqttWillPublish) publish;
        }
        return new MqttPublishBuilder.WillDefault(publish).build();
    }

    public static @NotNull MqttPublish publish(final @Nullable Mqtt5Publish publish) {
        return Checks.notImplemented(publish, MqttPublish.class, "Publish");
    }

    public static @NotNull MqttPublish publish(final @Nullable Mqtt3Publish publish) {
        return Checks.notImplemented(publish, Mqtt3PublishView.class, "Publish").getDelegate();
    }

    public static @NotNull MqttSubscribe subscribe(final @Nullable Mqtt5Subscribe subscribe) {
        return Checks.notImplemented(subscribe, MqttSubscribe.class, "Subscribe");
    }

    public static @NotNull MqttSubscribe subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        return Checks.notImplemented(subscribe, Mqtt3SubscribeView.class, "Subscribe").getDelegate();
    }

    public static @NotNull MqttUnsubscribe unsubscribe(final @Nullable Mqtt5Unsubscribe unsubscribe) {
        return Checks.notImplemented(unsubscribe, MqttUnsubscribe.class, "Unsubscribe");
    }

    public static @NotNull MqttUnsubscribe unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        return Checks.notImplemented(unsubscribe, Mqtt3UnsubscribeView.class, "Unsubscribe").getDelegate();
    }

    public static @NotNull MqttDisconnect disconnect(final @Nullable Mqtt5Disconnect disconnect) {
        return Checks.notImplemented(disconnect, MqttDisconnect.class, "Disconnect");
    }
}

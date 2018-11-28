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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
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
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.util.Checks;
import org.mqttbee.util.collections.ImmutableList;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * @author Silvio Giebl
 */
public class MqttChecks {

    private MqttChecks() {}

    @Contract("null, _ -> fail")
    public static @NotNull MqttUtf8StringImpl string(
            final @Nullable MqttUtf8String string, final @NotNull String name) {

        return Checks.notImplemented(string, MqttUtf8StringImpl.class, name);
    }

    @Contract("null, _ -> null")
    public static @Nullable MqttUtf8StringImpl stringOrNull(final @Nullable String string, final @NotNull String name) {
        return (string == null) ? null : MqttUtf8StringImpl.of(string, name);
    }

    @Contract("null, _ -> null")
    public static @Nullable MqttUtf8StringImpl stringOrNull(
            final @Nullable MqttUtf8String string, final @NotNull String name) {

        return Checks.notImplementedOrNull(string, MqttUtf8StringImpl.class, name);
    }

    @Contract("null -> null")
    public static @Nullable MqttUtf8StringImpl reasonString(final @Nullable String reasonString) {
        return stringOrNull(reasonString, "Reason string");
    }

    @Contract("null -> null")
    public static @Nullable MqttUtf8StringImpl reasonString(final @Nullable MqttUtf8String reasonString) {
        return stringOrNull(reasonString, "Reason string");
    }

    @Contract("null -> fail")
    public static @NotNull MqttTopicImpl topic(final @Nullable MqttTopic topic) {
        return Checks.notImplemented(topic, MqttTopicImpl.class, "Topic");
    }

    @Contract("null -> fail")
    public static @NotNull MqttTopicFilterImpl topicFilter(final @Nullable MqttTopicFilter topicFilter) {
        return Checks.notImplemented(topicFilter, MqttTopicFilterImpl.class, "Topic filter");
    }

    @Contract("null -> fail")
    public static @NotNull MqttClientIdentifierImpl clientIdentifier(
            final @Nullable MqttClientIdentifier clientIdentifier) {

        return Checks.notImplemented(clientIdentifier, MqttClientIdentifierImpl.class, "Client identifier");
    }

    private static @NotNull ByteBuffer binaryDataInternal(final @NotNull byte[] binary, final @NotNull String name) {
        if (!MqttBinaryData.isInRange(binary)) {
            throw new IllegalArgumentException(
                    name + " can not be encoded as binary data. Maximum length is " + MqttBinaryData.MAX_LENGTH +
                            " bytes, but was " + binary.length + " bytes.");
        }
        return ByteBuffer.wrap(binary);
    }

    @Contract("null, _ -> fail")
    public static @NotNull ByteBuffer binaryData(final @Nullable byte[] binary, final @NotNull String name) {
        return binaryDataInternal(Checks.notNull(binary, name), name);
    }

    @Contract("null, _ -> null")
    public static @Nullable ByteBuffer binaryDataOrNull(final @Nullable byte[] binary, final @NotNull String name) {
        return (binary == null) ? null : binaryDataInternal(binary, name);
    }

    private static @NotNull ByteBuffer binaryDataInternal(
            final @NotNull ByteBuffer binary, final @NotNull String name) {

        if (!MqttBinaryData.isInRange(binary)) {
            throw new IllegalArgumentException(
                    name + " can not be encoded as binary data. Maximum length is " + MqttBinaryData.MAX_LENGTH +
                            " bytes, but was " + binary.remaining() + " bytes.");
        }
        return binary.slice();
    }

    @Contract("null, _ -> fail")
    public static @NotNull ByteBuffer binaryData(final @Nullable ByteBuffer binary, final @NotNull String name) {
        return binaryDataInternal(Checks.notNull(binary, name), name);
    }

    @Contract("null, _ -> null")
    public static @Nullable ByteBuffer binaryDataOrNull(final @Nullable ByteBuffer binary, final @NotNull String name) {
        return (binary == null) ? null : binaryDataInternal(binary, name);
    }

    @Contract("null -> fail")
    public static @NotNull MqttUserPropertiesImpl userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        return Checks.notImplemented(userProperties, MqttUserPropertiesImpl.class, "User properties");
    }

    @Contract("null -> fail")
    public static @NotNull MqttUserPropertiesImpl userProperties(
            final @Nullable Mqtt5UserProperty @Nullable ... userProperties) {

        final ImmutableList<Mqtt5UserProperty> immutable = ImmutableList.copyOf(userProperties, "User properties");
        return MqttUserPropertiesImpl.of(
                Checks.elementsNotImplemented(immutable, MqttUserPropertyImpl.class, "User property"));
    }

    @Contract("null -> fail")
    public static @NotNull MqttUserPropertiesImpl userProperties(
            final @Nullable Collection<@Nullable Mqtt5UserProperty> userProperties) {

        final ImmutableList<Mqtt5UserProperty> immutable = ImmutableList.copyOf(userProperties, "User properties");
        return MqttUserPropertiesImpl.of(
                Checks.elementsNotImplemented(immutable, MqttUserPropertyImpl.class, "User property"));
    }

    @Contract("null -> fail")
    public static @NotNull MqttUserPropertyImpl userProperty(final @Nullable Mqtt5UserProperty userProperty) {
        return Checks.notImplemented(userProperty, MqttUserPropertyImpl.class, "User property");
    }

    @Contract("null, _ -> fail; _, null -> fail")
    public static @NotNull MqttUserPropertyImpl userProperty(
            final @Nullable MqttUtf8String name, final @Nullable MqttUtf8String value) {

        return MqttUserPropertyImpl.of(
                MqttChecks.string(name, "User property name"), MqttChecks.string(value, "User property value"));
    }

    @Contract("null -> fail")
    public static @NotNull MqttConnect connect(final @Nullable Mqtt5Connect connect) {
        return Checks.notImplemented(connect, MqttConnect.class, "Connect");
    }

    @Contract("null -> fail")
    public static @NotNull MqttConnect connect(final @Nullable Mqtt3Connect connect) {
        return Checks.notImplemented(connect, Mqtt3ConnectView.class, "Connect").getDelegate();
    }

    @Contract("null -> fail")
    public static @NotNull MqttPublish publish(final @Nullable Mqtt5Publish publish) {
        return Checks.notImplemented(publish, MqttPublish.class, "Publish");
    }

    @Contract("null -> fail")
    public static @NotNull MqttPublish publish(final @Nullable Mqtt3Publish publish) {
        return Checks.notImplemented(publish, Mqtt3PublishView.class, "Publish").getDelegate();
    }

    @Contract("null -> fail")
    public static @NotNull MqttSubscribe subscribe(final @Nullable Mqtt5Subscribe subscribe) {
        return Checks.notImplemented(subscribe, MqttSubscribe.class, "Subscribe");
    }

    @Contract("null -> fail")
    public static @NotNull MqttSubscribe subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        return Checks.notImplemented(subscribe, Mqtt3SubscribeView.class, "Subscribe").getDelegate();
    }

    @Contract("null -> fail")
    public static @NotNull MqttUnsubscribe unsubscribe(final @Nullable Mqtt5Unsubscribe unsubscribe) {
        return Checks.notImplemented(unsubscribe, MqttUnsubscribe.class, "Unsubscribe");
    }

    @Contract("null -> fail")
    public static @NotNull MqttUnsubscribe unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        return Checks.notImplemented(unsubscribe, Mqtt3UnsubscribeView.class, "Unsubscribe").getDelegate();
    }

    @Contract("null -> fail")
    public static @NotNull MqttDisconnect disconnect(final @Nullable Mqtt5Disconnect disconnect) {
        return Checks.notImplemented(disconnect, MqttDisconnect.class, "Disconnect");
    }
}

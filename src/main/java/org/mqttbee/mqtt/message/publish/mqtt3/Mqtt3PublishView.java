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

package org.mqttbee.mqtt.message.publish.mqtt3;

import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.publish.MqttWillPublish;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PublishView implements Mqtt3Publish {

    public static final @NotNull Function<Mqtt5Publish, Mqtt3Publish> MAPPER = Mqtt3PublishView::of;
    public static final @NotNull java.util.function.Function<Mqtt5Publish, Mqtt3Publish> JAVA_MAPPER =
            Mqtt3PublishView::of;

    public static @NotNull MqttPublish delegate(
            final @NotNull MqttTopicImpl topic, final @Nullable ByteBuffer payload, final @NotNull MqttQos qos,
            final boolean isRetain) {

        return new MqttPublish(topic, payload, qos, isRetain, MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null,
                TopicAliasUsage.NO, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    public static @NotNull MqttStatefulPublish statefulDelegate(
            final @NotNull MqttPublish publish, final int packetIdentifier, final boolean isDup) {

        return publish.createStateful(packetIdentifier, isDup, MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS, false,
                MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    static @NotNull Mqtt3PublishView of(
            final @NotNull MqttTopicImpl topic, final @Nullable ByteBuffer payload, final @NotNull MqttQos qos,
            final boolean isRetain) {

        return new Mqtt3PublishView(delegate(topic, payload, qos, isRetain));
    }

    static @NotNull Mqtt3PublishView willOf(
            final @NotNull MqttTopicImpl topic, final @Nullable ByteBuffer payload, final @NotNull MqttQos qos,
            final boolean isRetain) {

        return new Mqtt3PublishView(
                new MqttWillPublish(topic, payload, qos, isRetain, MqttPublish.NO_MESSAGE_EXPIRY, null, null, null,
                        null, MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL));
    }

    public static @NotNull Mqtt3PublishView of(final @NotNull Mqtt5Publish publish) {
        return new Mqtt3PublishView((MqttPublish) publish);
    }

    public static @NotNull Mqtt3PublishView of(final @NotNull MqttPublish publish) {
        return new Mqtt3PublishView(publish);
    }

    private final @NotNull MqttPublish delegate;

    private Mqtt3PublishView(final @NotNull MqttPublish delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull MqttTopic getTopic() {
        return delegate.getTopic();
    }

    @Override
    public @NotNull Optional<ByteBuffer> getPayload() {
        return delegate.getPayload();
    }

    @Override
    public @NotNull byte[] getPayloadAsBytes() {
        return delegate.getPayloadAsBytes();
    }

    @Override
    public @NotNull MqttQos getQos() {
        return delegate.getQos();
    }

    @Override
    public boolean isRetain() {
        return delegate.isRetain();
    }

    public @NotNull MqttPublish getDelegate() {
        return delegate;
    }
}

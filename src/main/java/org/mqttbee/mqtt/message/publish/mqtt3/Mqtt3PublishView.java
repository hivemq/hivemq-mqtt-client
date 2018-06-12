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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
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
import org.mqttbee.util.MustNotBeImplementedUtil;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PublishView implements Mqtt3Publish {

    @NotNull
    public static MqttPublish delegate(
            @NotNull final MqttTopicImpl topic, @Nullable final ByteBuffer payload, @NotNull final MqttQoS qos,
            final boolean isRetain) {

        return new MqttPublish(topic, payload, qos, isRetain, MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, null, null,
                null, null, TopicAliasUsage.MUST_NOT, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    public static MqttStatefulPublish statefulDelegate(
            @NotNull final MqttPublish publish, final int packetIdentifier, final boolean isDup) {

        return publish.createStateful(packetIdentifier, isDup, MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS, false,
                MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    @NotNull
    public static MqttPublish delegate(@NotNull final Mqtt3Publish publish) {
        return MustNotBeImplementedUtil.checkNotImplemented(publish, Mqtt3PublishView.class).getDelegate();
    }

    @NotNull
    public static Mqtt3PublishView of(
            @NotNull final MqttTopicImpl topic, @Nullable final ByteBuffer payload, @NotNull final MqttQoS qos,
            final boolean isRetain) {

        return new Mqtt3PublishView(delegate(topic, payload, qos, isRetain));
    }

    @NotNull
    public static Mqtt3PublishView of(@NotNull final Mqtt5Publish publish) {
        return new Mqtt3PublishView(MustNotBeImplementedUtil.checkNotImplemented(publish, MqttPublish.class));
    }

    private final MqttPublish delegate;

    private Mqtt3PublishView(@NotNull final MqttPublish delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public MqttTopic getTopic() {
        return delegate.getTopic();
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPayload() {
        return delegate.getPayload();
    }

    @NotNull
    @Override
    public byte[] getPayloadAsBytes() {
        return delegate.getPayloadAsBytes();
    }

    @NotNull
    @Override
    public MqttQoS getQos() {
        return delegate.getQos();
    }

    @Override
    public boolean isRetain() {
        return delegate.isRetain();
    }

    @NotNull
    public MqttPublish getDelegate() {
        return delegate;
    }

    @NotNull
    public MqttWillPublish getWillDelegate() {
        if (delegate instanceof MqttWillPublish) {
            return (MqttWillPublish) delegate;
        }
        return (MqttWillPublish) Mqtt5WillPublish.extend(delegate).build();
    }

}

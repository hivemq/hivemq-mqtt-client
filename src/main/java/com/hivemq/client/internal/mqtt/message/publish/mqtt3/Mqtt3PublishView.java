/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.message.publish.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class Mqtt3PublishView implements Mqtt3Publish {

    public static final @NotNull Function<Mqtt5Publish, Mqtt3Publish> MAPPER = Mqtt3PublishView::of;
    public static final @NotNull java.util.function.Function<Mqtt5Publish, Mqtt3Publish> JAVA_MAPPER =
            Mqtt3PublishView::of;

    public static @NotNull MqttPublish delegate(
            final @NotNull MqttTopicImpl topic,
            final @Nullable ByteBuffer payload,
            final @NotNull MqttQos qos,
            final boolean retain) {

        return new MqttPublish(topic, payload, qos, retain, MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES, null);
    }

    public static @NotNull MqttStatefulPublish statefulDelegate(
            final @NotNull MqttPublish publish, final int packetIdentifier, final boolean dup) {

        return publish.createStateful(packetIdentifier, dup, MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS,
                MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    static @NotNull Mqtt3PublishView of(
            final @NotNull MqttTopicImpl topic,
            final @Nullable ByteBuffer payload,
            final @NotNull MqttQos qos,
            final boolean retain) {

        return new Mqtt3PublishView(delegate(topic, payload, qos, retain));
    }

    static @NotNull Mqtt3PublishView willOf(
            final @NotNull MqttTopicImpl topic,
            final @Nullable ByteBuffer payload,
            final @NotNull MqttQos qos,
            final boolean retain) {

        return new Mqtt3PublishView(
                new MqttWillPublish(topic, payload, qos, retain, MqttPublish.NO_MESSAGE_EXPIRY, null, null, null, null,
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL));
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
    public byte @NotNull [] getPayloadAsBytes() {
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

    @Override
    public void acknowledge() {
        delegate.acknowledge();
    }

    @Override
    public Mqtt3PublishViewBuilder.@NotNull Default extend() {
        return new Mqtt3PublishViewBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        return "topic=" + getTopic() + ((delegate.getRawPayload() == null) ? "" :
                ", payload=" + delegate.getRawPayload().remaining() + "byte") + ", qos=" + getQos() + ", retain=" +
                isRetain();
    }

    @Override
    public @NotNull String toString() {
        return "MqttPublish{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt3PublishView)) {
            return false;
        }
        final Mqtt3PublishView that = (Mqtt3PublishView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}

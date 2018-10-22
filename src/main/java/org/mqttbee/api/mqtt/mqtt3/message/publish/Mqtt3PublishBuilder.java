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

package org.mqttbee.api.mqtt.mqtt3.message.publish;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicBuilder;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.ByteBufferUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3PublishBuilder<P> extends FluentBuilder<Mqtt3Publish, P> {

    @Nullable MqttTopicImpl topic;
    @Nullable ByteBuffer payload;
    @NotNull MqttQos qos = Mqtt3Publish.DEFAULT_QOS;
    boolean retain;

    public Mqtt3PublishBuilder(final @Nullable Function<? super Mqtt3Publish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt3PublishBuilder(final @NotNull Mqtt3Publish publish) {
        super(null);
        final Mqtt3PublishView publishView =
                MustNotBeImplementedUtil.checkNotImplemented(publish, Mqtt3PublishView.class);
        topic = publishView.getDelegate().getTopic();
        payload = publishView.getDelegate().getRawPayload();
        qos = publishView.getQos();
        retain = publishView.isRetain();
    }

    public @NotNull Mqtt3PublishBuilder<P> topic(final @NotNull String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    public @NotNull Mqtt3PublishBuilder<P> topic(final @NotNull MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    public @NotNull MqttTopicBuilder<? extends Mqtt3PublishBuilder<P>> topic() {
        return new MqttTopicBuilder<>("", this::topic);
    }

    public @NotNull Mqtt3PublishBuilder<P> payload(final @Nullable byte[] payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.wrap(payload);
        return this;
    }

    public @NotNull Mqtt3PublishBuilder<P> payload(final @Nullable ByteBuffer payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.slice(payload);
        return this;
    }

    public @NotNull Mqtt3PublishBuilder<P> qos(final @NotNull MqttQos qos) {
        this.qos = Preconditions.checkNotNull(qos, "QoS must not be null.");
        return this;
    }

    public @NotNull Mqtt3PublishBuilder<P> retain(final boolean retain) {
        this.retain = retain;
        return this;
    }

    @Override
    public @NotNull Mqtt3Publish build() {
        Preconditions.checkNotNull(topic, "Topic must not be null.");
        return Mqtt3PublishView.of(topic, payload, qos, retain);
    }

    public @NotNull P applyPublish() {
        return apply();
    }

}

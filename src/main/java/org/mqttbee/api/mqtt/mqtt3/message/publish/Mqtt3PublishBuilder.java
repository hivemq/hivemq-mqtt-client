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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
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

    private MqttTopicImpl topic;
    private ByteBuffer payload;
    private MqttQoS qos;
    private boolean retain;

    public Mqtt3PublishBuilder(@Nullable final Function<? super Mqtt3Publish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt3PublishBuilder(@NotNull final Mqtt3Publish publish) {
        super(null);
        final Mqtt3PublishView publishView =
                MustNotBeImplementedUtil.checkNotImplemented(publish, Mqtt3PublishView.class);
        topic = publishView.getDelegate().getTopic();
        payload = publishView.getDelegate().getRawPayload();
        qos = publishView.getQos();
        retain = publishView.isRetain();
    }

    @NotNull
    public Mqtt3PublishBuilder<P> topic(@NotNull final String topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    @NotNull
    public Mqtt3PublishBuilder<P> topic(@NotNull final MqttTopic topic) {
        this.topic = MqttBuilderUtil.topic(topic);
        return this;
    }

    @NotNull
    public MqttTopicBuilder<? extends Mqtt3PublishBuilder<P>> topic() {
        return new MqttTopicBuilder<>("", this::topic);
    }

    @NotNull
    public Mqtt3PublishBuilder<P> payload(@Nullable final byte[] payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.wrap(payload);
        return this;
    }

    @NotNull
    public Mqtt3PublishBuilder<P> payload(@Nullable final ByteBuffer payload) {
        this.payload = (payload == null) ? null : ByteBufferUtil.slice(payload);
        return this;
    }

    @NotNull
    public Mqtt3PublishBuilder<P> qos(@NotNull final MqttQoS qos) {
        this.qos = Preconditions.checkNotNull(qos);
        return this;
    }

    @NotNull
    public Mqtt3PublishBuilder<P> retain(final boolean retain) {
        this.retain = retain;
        return this;
    }

    @NotNull
    @Override
    public Mqtt3Publish build() {
        Preconditions.checkNotNull(topic);
        Preconditions.checkNotNull(qos);
        return Mqtt3PublishView.of(topic, payload, qos, retain);
    }

}

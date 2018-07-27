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

package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicBuilder;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;

import java.nio.ByteBuffer;

/**
 * @author Bilvio Giebl
 */
@DoNotImplement
public interface Mqtt5PublishBuilderBase<B extends Mqtt5PublishBuilderBase<B, C>, C extends B> {

    @NotNull C topic(@NotNull String topic);

    @NotNull C topic(@NotNull MqttTopic topic);

    @NotNull MqttTopicBuilder.Nested<? extends C> topic();

    @NotNull B payload(@Nullable byte[] payload);

    @NotNull B payload(@Nullable ByteBuffer payload);

    @NotNull B qos(@NotNull MqttQos qos);

    @NotNull B retain(boolean retain);

    @NotNull B messageExpiryInterval(long messageExpiryInterval);

    @NotNull B noMessageExpiry();

    @NotNull B payloadFormatIndicator(@Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator);

    @NotNull B contentType(@Nullable String contentType);

    @NotNull B contentType(@Nullable MqttUtf8String contentType);

    @NotNull B responseTopic(@Nullable String responseTopic);

    @NotNull B responseTopic(@Nullable MqttTopic responseTopic);

    @NotNull MqttTopicBuilder.Nested<? extends B> responseTopic();

    @NotNull B correlationData(@Nullable byte[] correlationData);

    @NotNull B correlationData(@Nullable ByteBuffer correlationData);

    @NotNull B userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends B> userProperties();

    @DoNotImplement
    interface Complete<B extends Mqtt5PublishBuilderBase<B, C>, C extends B> extends Mqtt5PublishBuilderBase<B, C> {

        @Override
        @NotNull C payload(@Nullable byte[] payload);

        @Override
        @NotNull C payload(@Nullable ByteBuffer payload);

        @Override
        @NotNull C qos(@NotNull MqttQos qos);

        @Override
        @NotNull C retain(boolean retain);

        @Override
        @NotNull C messageExpiryInterval(long messageExpiryInterval);

        @Override
        @NotNull C noMessageExpiry();

        @Override
        @NotNull C payloadFormatIndicator(@Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator);

        @Override
        @NotNull C contentType(@Nullable String contentType);

        @Override
        @NotNull C contentType(@Nullable MqttUtf8String contentType);

        @Override
        @NotNull C responseTopic(@Nullable String responseTopic);

        @Override
        @NotNull C responseTopic(@Nullable MqttTopic responseTopic);

        @Override
        @NotNull MqttTopicBuilder.Nested<? extends C> responseTopic();

        @Override
        @NotNull C correlationData(@Nullable byte[] correlationData);

        @Override
        @NotNull C correlationData(@Nullable ByteBuffer correlationData);

        @Override
        @NotNull C userProperties(@NotNull Mqtt5UserProperties userProperties);

        @Override
        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends C> userProperties();
    }

    @DoNotImplement
    interface Base<B extends Mqtt5PublishBuilderBase<B, C>, C extends B> extends Mqtt5PublishBuilderBase<B, C> {

        @NotNull B useTopicAlias(@NotNull TopicAliasUsage topicAliasUsage);

        @DoNotImplement
        interface Complete<B extends Mqtt5PublishBuilderBase<B, C>, C extends B>
                extends Mqtt5PublishBuilderBase.Base<B, C>, Mqtt5PublishBuilderBase.Complete<B, C> {

            @Override
            @NotNull C useTopicAlias(@NotNull TopicAliasUsage topicAliasUsage);
        }
    }

    @DoNotImplement
    interface WillBase<B extends Mqtt5PublishBuilderBase<B, C>, C extends B> extends Mqtt5PublishBuilderBase<B, C> {

        @NotNull B delayInterval(long delayInterval);

        @DoNotImplement
        interface Complete<B extends Mqtt5PublishBuilderBase<B, C>, C extends B>
                extends Mqtt5PublishBuilderBase.WillBase<B, C>, Mqtt5PublishBuilderBase.Complete<B, C> {

            @Override
            @NotNull C delayInterval(long delayInterval);
        }
    }
}

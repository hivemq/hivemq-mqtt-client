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
public interface Mqtt5PublishBuilderBase<C extends Mqtt5PublishBuilderBase.Complete<C>> {

    @NotNull C topic(@NotNull String topic);

    @NotNull C topic(@NotNull MqttTopic topic);

    @NotNull MqttTopicBuilder.Nested<? extends C> topic();

    @DoNotImplement
    interface Complete<C extends Mqtt5PublishBuilderBase.Complete<C>> extends Mqtt5PublishBuilderBase<C> {

        @NotNull C payload(@Nullable byte[] payload);

        @NotNull C payload(@Nullable ByteBuffer payload);

        @NotNull C qos(@NotNull MqttQos qos);

        @NotNull C retain(boolean retain);

        @NotNull C messageExpiryInterval(long messageExpiryInterval);

        @NotNull C noMessageExpiry();

        @NotNull C payloadFormatIndicator(@Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator);

        @NotNull C contentType(@Nullable String contentType);

        @NotNull C contentType(@Nullable MqttUtf8String contentType);

        @NotNull C responseTopic(@Nullable String responseTopic);

        @NotNull C responseTopic(@Nullable MqttTopic responseTopic);

        @NotNull MqttTopicBuilder.Nested<? extends C> responseTopic();

        @NotNull C correlationData(@Nullable byte[] correlationData);

        @NotNull C correlationData(@Nullable ByteBuffer correlationData);

        @NotNull C userProperties(@NotNull Mqtt5UserProperties userProperties);

        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends C> userProperties();
    }

    @DoNotImplement
    interface Base<C extends Base.Complete<C>> extends Mqtt5PublishBuilderBase<C> {

        @DoNotImplement
        interface Complete<C extends Base.Complete<C>> extends Mqtt5PublishBuilderBase.Complete<C>, Base<C> {

            @NotNull C useTopicAlias(@NotNull TopicAliasUsage topicAliasUsage);
        }
    }

    @DoNotImplement
    interface WillBase<C extends WillBase.Complete<C>> extends Mqtt5PublishBuilderBase<C> {

        @DoNotImplement
        interface Complete<C extends WillBase.Complete<C>> extends Mqtt5PublishBuilderBase.Complete<C>, WillBase<C> {

            @NotNull C delayInterval(long delayInterval);
        }
    }
}

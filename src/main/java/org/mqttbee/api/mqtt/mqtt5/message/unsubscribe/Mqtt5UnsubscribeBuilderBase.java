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

package org.mqttbee.api.mqtt.mqtt5.message.unsubscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt5UnsubscribeBuilderBase<
            B extends Mqtt5UnsubscribeBuilderBase<B, C>,
            C extends B> {
// @formatter:on

    @NotNull C addTopicFilter(@NotNull String topicFilter);

    @NotNull C addTopicFilter(@NotNull MqttTopicFilter topicFilter);

    @NotNull MqttTopicFilterBuilder.Nested<? extends C> addTopicFilter();

    @NotNull C reverse(@NotNull Mqtt5Subscribe subscribe);

    @NotNull B userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends B> userProperties();

    // @formatter:off
    @DoNotImplement
    interface Complete<
                B extends Mqtt5UnsubscribeBuilderBase<B, C>,
                C extends B>
            extends Mqtt5UnsubscribeBuilderBase<B, C> {
    // @formatter:on

        @NotNull C userProperties(@NotNull Mqtt5UserProperties userProperties);

        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends C> userProperties();
    }

    // @formatter:off
    @DoNotImplement
    interface Start<
                B extends Mqtt5UnsubscribeBuilderBase<B, C>,
                C extends B,
                S extends B>
            extends Mqtt5UnsubscribeBuilderBase<B, C> {
    // @formatter:on

        @NotNull C topicFilter(@NotNull String topicFilter);

        @NotNull C topicFilter(@NotNull MqttTopicFilter topicFilter);

        @NotNull MqttTopicFilterBuilder.Nested<? extends C> topicFilter();

        @Override
        @NotNull S userProperties(@NotNull Mqtt5UserProperties userProperties);

        @Override
        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends S> userProperties();
    }
}

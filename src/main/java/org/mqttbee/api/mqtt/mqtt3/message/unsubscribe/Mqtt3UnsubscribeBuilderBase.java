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

package org.mqttbee.api.mqtt.mqtt3.message.unsubscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt3UnsubscribeBuilderBase<
            B extends Mqtt3UnsubscribeBuilderBase<B, C>,
            C extends B> {
// @formatter:on

    @NotNull C addTopicFilter(final @NotNull String topicFilter);

    @NotNull C addTopicFilter(final @NotNull MqttTopicFilter topicFilter);

    @NotNull MqttTopicFilterBuilder<? extends C> addTopicFilter();

    @NotNull C reverse(final @NotNull Mqtt3Subscribe subscribe);

    // @formatter:off
    @DoNotImplement
    interface Complete<
                B extends Mqtt3UnsubscribeBuilderBase<B, C>,
                C extends B>
            extends Mqtt3UnsubscribeBuilderBase<B, C> {
    // @formatter:on
    }

    // @formatter:off
    @DoNotImplement
    interface Start<
                B extends Mqtt3UnsubscribeBuilderBase<B, C>,
                C extends B>
            extends Mqtt3UnsubscribeBuilderBase<B, C> {
    // @formatter:on

        @NotNull C topicFilter(final @NotNull String topicFilter);

        @NotNull C topicFilter(final @NotNull MqttTopicFilter topicFilter);

        @NotNull MqttTopicFilterBuilder<? extends C> topicFilter();
    }
}

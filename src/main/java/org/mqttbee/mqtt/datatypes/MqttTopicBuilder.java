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

package org.mqttbee.mqtt.datatypes;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttTopicBuilder extends MqttTopicBuilderBase<MqttTopicBuilder.Complete> {

    @NotNull MqttTopicFilterBuilder filter();

    @NotNull MqttSharedTopicFilterBuilder share(@NotNull String shareName);

    @DoNotImplement
    interface Complete extends MqttTopicBuilder, MqttTopicBuilderBase<MqttTopicBuilder.Complete> {

        @NotNull MqttTopic build();
    }

    @DoNotImplement
    interface Nested<P> extends MqttTopicBuilderBase<Nested.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Nested<P>, MqttTopicBuilderBase<Nested.Complete<P>> {

            @NotNull P applyTopic();
        }
    }
}

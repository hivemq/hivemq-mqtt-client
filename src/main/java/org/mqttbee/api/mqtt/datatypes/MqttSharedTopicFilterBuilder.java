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

package org.mqttbee.api.mqtt.datatypes;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface MqttSharedTopicFilterBuilder extends
        MqttTopicFilterBuilderBase.SharedBase<
                MqttSharedTopicFilterBuilder, MqttSharedTopicFilterBuilder.Complete, MqttSharedTopicFilterBuilder.End> {
// @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Complete extends
            MqttSharedTopicFilterBuilder, MqttSharedTopicFilterBuilder.End,
            MqttTopicFilterBuilderBase.SharedBase.Complete<
                    MqttSharedTopicFilterBuilder.Complete, MqttSharedTopicFilterBuilder.End> {}
    // @formatter:on

    @DoNotImplement
    interface End extends MqttTopicFilterBuilderBase.SharedBase.End {

        @NotNull MqttSharedTopicFilter build();
    }

    // @formatter:off
    @DoNotImplement
    interface Nested<P> extends
            MqttTopicFilterBuilderBase.SharedBase<Nested<P>, Nested.Complete<P>, Nested.End<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Nested<P>, Nested.End<P>,
                MqttTopicFilterBuilderBase.SharedBase.Complete<Nested.Complete<P>, Nested.End<P>> {
        // @formatter:on
        }

        @DoNotImplement
        interface End<P> extends MqttTopicFilterBuilderBase.SharedBase.End {

            @NotNull P applyTopicFilter();
        }
    }
}

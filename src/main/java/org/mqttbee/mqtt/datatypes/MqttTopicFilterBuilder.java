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
 */
// @formatter:off
@DoNotImplement
public interface MqttTopicFilterBuilder extends
        MqttTopicFilterBuilderBase<
                MqttTopicFilterBuilder.Complete, MqttTopicFilterBuilder.End, MqttSharedTopicFilterBuilder,
                MqttSharedTopicFilterBuilder.Complete, MqttSharedTopicFilterBuilder.End> {
// @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Complete extends
            MqttTopicFilterBuilder, MqttTopicFilterBuilder.End,
            MqttTopicFilterBuilderBase.Complete<
                    MqttTopicFilterBuilder.Complete, MqttTopicFilterBuilder.End, MqttSharedTopicFilterBuilder,
                    MqttSharedTopicFilterBuilder.Complete, MqttSharedTopicFilterBuilder.End> {}
    // @formatter:on

    @DoNotImplement
    interface End extends MqttTopicFilterBuilderBase.End {

        @NotNull MqttTopicFilter build();
    }

    // @formatter:off
    @DoNotImplement
    interface Nested<P> extends
            MqttTopicFilterBuilderBase<
                    Nested.Complete<P>, Nested.End<P>, MqttSharedTopicFilterBuilder.Nested<P>,
                    MqttSharedTopicFilterBuilder.Nested.Complete<P>, MqttSharedTopicFilterBuilder.Nested.End<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Nested<P>, Nested.End<P>,
                MqttTopicFilterBuilderBase.Complete<
                        Nested.Complete<P>, Nested.End<P>, MqttSharedTopicFilterBuilder.Nested<P>,
                        MqttSharedTopicFilterBuilder.Nested.Complete<P>, MqttSharedTopicFilterBuilder.Nested.End<P>> {}
        // @formatter:on

        @DoNotImplement
        interface End<P> extends MqttTopicFilterBuilderBase.End {

            @NotNull P applyTopicFilter();
        }
    }
}

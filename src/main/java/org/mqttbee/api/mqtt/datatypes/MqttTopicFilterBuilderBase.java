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
public interface MqttTopicFilterBuilderBase<
        C extends MqttTopicFilterBuilderBase.Complete<C, E, SC, SE>,
        E extends MqttTopicFilterBuilderBase.End,
        S extends MqttTopicFilterBuilderBase.SharedBase<S, SC, SE>,
        SC extends MqttTopicFilterBuilderBase.SharedBase.Complete<SC, SE>,
        SE extends MqttTopicFilterBuilderBase.SharedBase.End> {
// @formatter:on

    @NotNull C addLevel(@NotNull String topicLevel);

    @NotNull C singleLevelWildcard();

    @NotNull E multiLevelWildcard();

    @NotNull S share(@NotNull String shareName);

    // @formatter:off
    @DoNotImplement
    interface Complete<
            C extends MqttTopicFilterBuilderBase.Complete<C, E, SC, SE>,
            E extends MqttTopicFilterBuilderBase.End,
            SC extends MqttTopicFilterBuilderBase.SharedBase.Complete<SC, SE>,
            SE extends MqttTopicFilterBuilderBase.SharedBase.End> {
    // @formatter:on

        @NotNull C addLevel(@NotNull String topicLevel);

        @NotNull C singleLevelWildcard();

        @NotNull E multiLevelWildcard();

        @NotNull SC share(@NotNull String shareName);
    }

    @DoNotImplement
    interface End {}

    // @formatter:off
    @DoNotImplement
    interface SharedBase<
            S extends SharedBase<S, SC, SE>,
            SC extends SharedBase.Complete<SC, SE>,
            SE extends SharedBase.End>
            extends MqttTopicFilterBuilderBase<SC, SE, S, SC, SE> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<
                SC extends SharedBase.Complete<SC, SE>,
                SE extends SharedBase.End>
                extends MqttTopicFilterBuilderBase.Complete<SC, SE, SC, SE> {}
        // @formatter:on

        @DoNotImplement
        interface End extends MqttTopicFilterBuilderBase.End {}
    }
}

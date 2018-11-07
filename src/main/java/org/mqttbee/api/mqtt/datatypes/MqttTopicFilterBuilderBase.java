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
            B extends MqttTopicFilterBuilderBase<B, C, E, S>,
            C extends B,
            E extends MqttTopicFilterBuilderBase.End,
            S> {
// @formatter:on

    @NotNull C addLevel(final @NotNull String topicLevel);

    @NotNull C singleLevelWildcard();

    @NotNull E multiLevelWildcard();

    @NotNull S share(final @NotNull String shareName);

    // @formatter:off
    @DoNotImplement
    interface Complete<
                B extends MqttTopicFilterBuilderBase<B, C, E, S>,
                C extends B,
                E extends MqttTopicFilterBuilderBase.End,
                S,
                SC extends S>
            extends MqttTopicFilterBuilderBase<B, C, E, S>, End {
    // @formatter:on

        @NotNull SC share(final @NotNull String shareName);
    }

    @DoNotImplement
    interface End {}
}

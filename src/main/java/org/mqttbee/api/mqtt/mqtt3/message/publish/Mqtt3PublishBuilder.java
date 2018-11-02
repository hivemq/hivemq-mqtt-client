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

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt3PublishBuilder extends
        Mqtt3PublishBuilderBase<
            Mqtt3PublishBuilder,
            Mqtt3PublishBuilder.Complete> {
// @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Complete extends
            Mqtt3PublishBuilder,
            Mqtt3PublishBuilderBase.Complete<
                Mqtt3PublishBuilder,
                Mqtt3PublishBuilder.Complete> {
    // @formatter:on

        @NotNull Mqtt3Publish build();
    }

    // @formatter:off
    @DoNotImplement
    interface Nested<P> extends
            Mqtt3PublishBuilderBase<
                Nested<P>,
                Nested.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Nested<P>,
                Mqtt3PublishBuilderBase.Complete<
                    Nested<P>,
                    Nested.Complete<P>> {
        // @formatter:on

            @NotNull P applyPublish();
        }
    }

    // @formatter:off
    @DoNotImplement
    interface Send<P> extends
            Mqtt3PublishBuilderBase<
                Send<P>,
                Send.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Send<P>,
                Mqtt3PublishBuilderBase.Complete<
                    Send<P>,
                    Send.Complete<P>> {
        // @formatter:on

            @NotNull P send();
        }
    }
}

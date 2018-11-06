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

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt5UnsubscribeBuilder extends
        Mqtt5UnsubscribeBuilderBase<
            Mqtt5UnsubscribeBuilder,
            Mqtt5UnsubscribeBuilder.Complete> {
// @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Complete extends
            Mqtt5UnsubscribeBuilder,
            Mqtt5UnsubscribeBuilderBase.Complete<
                Mqtt5UnsubscribeBuilder,
                Mqtt5UnsubscribeBuilder.Complete> {
    // @formatter:on

        @NotNull Mqtt5Unsubscribe build();
    }

    // @formatter:off
    @DoNotImplement
    interface Start extends
            Mqtt5UnsubscribeBuilder,
            Mqtt5UnsubscribeBuilderBase.Start<
                Mqtt5UnsubscribeBuilder,
                Mqtt5UnsubscribeBuilder.Complete,
                Mqtt5UnsubscribeBuilder.Start> {
    // @formatter:on
    }

    // @formatter:off
    @DoNotImplement
    interface Nested<P> extends
            Mqtt5UnsubscribeBuilderBase<
                Nested<P>,
                Nested.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Nested<P>,
                Mqtt5UnsubscribeBuilderBase.Complete<
                    Nested<P>,
                    Nested.Complete<P>> {
        // @formatter:on

            @NotNull P applyUnsubscribe();
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Nested<P>,
                Mqtt5UnsubscribeBuilderBase.Start<
                    Nested<P>,
                    Nested.Complete<P>,
                    Nested.Start<P>> {
        // @formatter:on
        }
    }

    // @formatter:off
    @DoNotImplement
    interface Send<P> extends
            Mqtt5UnsubscribeBuilderBase<
                Send<P>,
                Send.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Send<P>,
                Mqtt5UnsubscribeBuilderBase.Complete<
                    Send<P>,
                    Send.Complete<P>> {
        // @formatter:on

            @NotNull P send();
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Send<P>,
                Mqtt5UnsubscribeBuilderBase.Start<
                    Send<P>,
                    Send.Complete<P>,
                    Send.Start<P>> {
        // @formatter:on
        }
    }
}

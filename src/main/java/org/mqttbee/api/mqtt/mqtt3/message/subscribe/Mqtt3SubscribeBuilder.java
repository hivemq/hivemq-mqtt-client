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

package org.mqttbee.api.mqtt.mqtt3.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt3SubscribeBuilder extends
        Mqtt3SubscribeBuilderBase<
                Mqtt3SubscribeBuilder,
                Mqtt3SubscribeBuilder.Complete> {
// @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Complete extends
            Mqtt3SubscribeBuilder,
            Mqtt3SubscribeBuilderBase.Complete<
                Mqtt3SubscribeBuilder,
                Mqtt3SubscribeBuilder.Complete> {
    // @formatter:on

        @NotNull Mqtt3Subscribe build();
    }

    // @formatter:off
    @DoNotImplement
    interface First extends
            Mqtt3SubscribeBuilderBase.First<
                Mqtt3SubscribeBuilder.First,
                Mqtt3SubscribeBuilder.Start.Complete> {
    // @formatter:on
    }

    // @formatter:off
    @DoNotImplement
    interface Start extends
            Mqtt3SubscribeBuilder,
            Mqtt3SubscribeBuilder.First,
            Mqtt3SubscribeBuilderBase.Start<
                Mqtt3SubscribeBuilder,
                Mqtt3SubscribeBuilder.Complete,
                Mqtt3SubscribeBuilder.First,
                Mqtt3SubscribeBuilder.Start.Complete> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete extends
                Mqtt3SubscribeBuilder.Start,
                Mqtt3SubscribeBuilder.Complete,
                Mqtt3SubscribeBuilderBase.Start.Complete<
                    Mqtt3SubscribeBuilder,
                    Mqtt3SubscribeBuilder.Complete,
                    Mqtt3SubscribeBuilder.First,
                    Mqtt3SubscribeBuilder.Start.Complete> {
        // @formatter:on
        }
    }

    // @formatter:off
    @DoNotImplement
    interface Nested<P> extends
            Mqtt3SubscribeBuilderBase<
                Nested<P>,
                Nested.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Nested<P>,
                Mqtt3SubscribeBuilderBase.Complete<
                    Nested<P>,
                    Nested.Complete<P>> {
        // @formatter:on

            @NotNull P applySubscribe();
        }

        // @formatter:off
        @DoNotImplement
        interface First<P> extends
                Mqtt3SubscribeBuilderBase.First<
                    Nested.First<P>,
                    Nested.Start.Complete<P>> {
        // @formatter:on
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Nested<P>,
                Nested.First<P>,
                Mqtt3SubscribeBuilderBase.Start<
                    Nested<P>,
                    Nested.Complete<P>,
                    Nested.First<P>,
                    Nested.Start.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Nested.Start<P>,
                    Nested.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<
                        Nested<P>,
                        Nested.Complete<P>,
                        Nested.First<P>,
                        Nested.Start.Complete<P>> {
            // @formatter:on
            }
        }
    }

    // @formatter:off
    @DoNotImplement
    interface Send<P> extends
            Mqtt3SubscribeBuilderBase<
                Send<P>,
                Send.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Send<P>,
                Mqtt3SubscribeBuilderBase.Complete<
                    Send<P>,
                    Send.Complete<P>> {
        // @formatter:on

            @NotNull P send();
        }

        // @formatter:off
        @DoNotImplement
        interface First<P> extends
                Mqtt3SubscribeBuilderBase.First<
                    Send.First<P>,
                    Send.Start.Complete<P>> {
        // @formatter:on
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Send<P>,
                Send.First<P>,
                Mqtt3SubscribeBuilderBase.Start<
                    Send<P>,
                    Send.Complete<P>,
                    Send.First<P>,
                    Send.Start.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Send.Start<P>,
                    Send.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<
                        Send<P>,
                        Send.Complete<P>,
                        Send.First<P>,
                        Send.Start.Complete<P>> {
            // @formatter:on
            }
        }
    }
}

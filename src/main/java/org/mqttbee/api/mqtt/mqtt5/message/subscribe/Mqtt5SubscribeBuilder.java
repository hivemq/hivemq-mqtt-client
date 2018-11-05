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

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt5SubscribeBuilder extends
        Mqtt5SubscribeBuilderBase<
            Mqtt5SubscribeBuilder,
            Mqtt5SubscribeBuilder.Complete> {
// @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Complete extends
            Mqtt5SubscribeBuilder,
            Mqtt5SubscribeBuilderBase.Complete<
                Mqtt5SubscribeBuilder,
                Mqtt5SubscribeBuilder.Complete> {
    // @formatter:on

        @NotNull Mqtt5Subscribe build();
    }

    // @formatter:off
    @DoNotImplement
    interface First extends
            Mqtt5SubscribeBuilderBase.First<
                Mqtt5SubscribeBuilder.First,
                Mqtt5SubscribeBuilder.Start.Complete> {
    // @formatter:on
    }

    // @formatter:off
    @DoNotImplement
    interface Start extends
            Mqtt5SubscribeBuilder,
            Mqtt5SubscribeBuilder.First,
            Mqtt5SubscribeBuilderBase.Start<
                Mqtt5SubscribeBuilder,
                Mqtt5SubscribeBuilder.Complete,
                Mqtt5SubscribeBuilder.Start,
                Mqtt5SubscribeBuilder.First,
                Mqtt5SubscribeBuilder.Start.Complete> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete extends
                Mqtt5SubscribeBuilder.Start,
                Mqtt5SubscribeBuilder.Complete,
                Mqtt5SubscribeBuilderBase.Start.Complete<
                    Mqtt5SubscribeBuilder,
                    Mqtt5SubscribeBuilder.Complete,
                    Mqtt5SubscribeBuilder.Start,
                    Mqtt5SubscribeBuilder.Start.Complete,
                    Mqtt5SubscribeBuilder.First,
                    Mqtt5SubscribeBuilder.Start.Complete> {
        // @formatter:on
        }
    }

    // @formatter:off
    @DoNotImplement
    interface Nested<P> extends
        Mqtt5SubscribeBuilderBase<
            Nested<P>,
            Nested.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Nested<P>,
                Mqtt5SubscribeBuilderBase.Complete<
                    Nested<P>,
                    Nested.Complete<P>> {
        // @formatter:on

            @NotNull P applySubscribe();
        }

        // @formatter:off
        @DoNotImplement
        interface First<P> extends
                Mqtt5SubscribeBuilderBase.First<
                    Nested.First<P>,
                    Nested.Start.Complete<P>> {
        // @formatter:on
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Nested<P>,
                Nested.First<P>,
                Mqtt5SubscribeBuilderBase.Start<
                    Nested<P>,
                    Nested.Complete<P>,
                    Nested.Start<P>,
                    Nested.First<P>,
                    Nested.Start.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Nested.Start<P>,
                    Nested.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<
                        Nested<P>,
                        Nested.Complete<P>,
                        Nested.Start<P>,
                        Nested.Start.Complete<P>,
                        Nested.First<P>,
                        Nested.Start.Complete<P>> {
            // @formatter:on
            }
        }
    }

    // @formatter:off
    @DoNotImplement
    interface Send<P> extends
        Mqtt5SubscribeBuilderBase<
            Send<P>,
            Send.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Send<P>,
                Mqtt5SubscribeBuilderBase.Complete<
                    Send<P>,
                    Send.Complete<P>> {
        // @formatter:on

            @NotNull P send();
        }

        // @formatter:off
        @DoNotImplement
        interface First<P> extends
                Mqtt5SubscribeBuilderBase.First<
                    Send.First<P>,
                    Send.Start.Complete<P>> {
        // @formatter:on
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Send<P>,
                Send.First<P>,
                Mqtt5SubscribeBuilderBase.Start<
                    Send<P>,
                    Send.Complete<P>,
                    Send.Start<P>,
                    Send.First<P>,
                    Send.Start.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Send.Start<P>,
                    Send.Complete<P>,
                    Mqtt5SubscribeBuilderBase.Start.Complete<
                        Send<P>,
                        Send.Complete<P>,
                        Send.Start<P>,
                        Send.Start.Complete<P>,
                        Send.First<P>,
                        Send.Start.Complete<P>> {
            // @formatter:on
            }
        }
    }
}

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

package org.mqttbee.mqtt.mqtt3.message.subscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3SubscribeBuilder extends Mqtt3SubscribeBuilderBase<Mqtt3SubscribeBuilder.Complete> {

    @DoNotImplement
    interface Complete extends Mqtt3SubscribeBuilder, Mqtt3SubscribeBuilderBase<Mqtt3SubscribeBuilder.Complete> {

        @NotNull Mqtt3Subscribe build();
    }

    // @formatter:off
    @DoNotImplement
    interface Start extends
            Mqtt3SubscribeBuilder,
            Mqtt3SubscribeBuilderBase.Start<Mqtt3SubscribeBuilder.Complete, Mqtt3SubscribeBuilder.Start.Complete> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete extends
                Mqtt3SubscribeBuilder.Start, Mqtt3SubscribeBuilder.Complete,
                Mqtt3SubscribeBuilderBase.Start.Complete<
                        Mqtt3SubscribeBuilder.Complete, Mqtt3SubscribeBuilder.Start.Complete> {}
        // @formatter:on
    }

    @DoNotImplement
    interface Nested<P> extends Mqtt3SubscribeBuilderBase<Nested.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Nested<P>, Mqtt3SubscribeBuilderBase<Nested.Complete<P>> {

            @NotNull P applySubscribe();
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Nested<P>,
                Mqtt3SubscribeBuilderBase.Start<Nested.Complete<P>, Nested.Start.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Nested.Start<P>, Nested.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<Nested.Complete<P>, Nested.Start.Complete<P>> {}
            // @formatter:on
        }
    }

    @DoNotImplement
    interface Send<P> extends Mqtt3SubscribeBuilderBase<Send.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Send<P>, Mqtt3SubscribeBuilderBase<Send.Complete<P>> {

            @NotNull P send();
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Send<P>,
                Mqtt3SubscribeBuilderBase.Start<Send.Complete<P>, Send.Start.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Send.Start<P>, Send.Complete<P>,
                    Mqtt3SubscribeBuilderBase.Start.Complete<Send.Complete<P>, Send.Start.Complete<P>> {}
            // @formatter:on
        }
    }
}

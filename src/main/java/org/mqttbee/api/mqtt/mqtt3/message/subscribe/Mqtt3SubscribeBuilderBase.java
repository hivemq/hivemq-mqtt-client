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
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscriptionBuilderImpl;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt3SubscribeBuilderBase<
            B extends Mqtt3SubscribeBuilderBase<B, C>,
            C extends B> {
// @formatter:on

    @NotNull C addSubscription(@NotNull Mqtt3Subscription subscription);

    default @NotNull Mqtt3SubscriptionBuilder.Nested<? extends C> addSubscription() {
        return new Mqtt3SubscriptionBuilderImpl.NestedImpl<>(this::addSubscription);
    }

    // @formatter:off
    @DoNotImplement
    interface Complete<
                B extends Mqtt3SubscribeBuilderBase<B, C>,
                C extends B>
            extends Mqtt3SubscribeBuilderBase<B, C> {
    // @formatter:on
    }

    // @formatter:off
    @DoNotImplement
    interface Start<
                B extends Mqtt3SubscribeBuilderBase<B, C>,
                C extends B,
                S extends Mqtt3SubscribeBuilderBase.Start<B, C, S, SC>,
                SC extends S>
            extends Mqtt3SubscribeBuilderBase<B, C>,
                    Mqtt3SubscriptionBuilderBase<S, SC> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<
                    B extends Mqtt3SubscribeBuilderBase<B, C>,
                    C extends B,
                    S extends Mqtt3SubscribeBuilderBase.Start<B, C, S, SC>,
                    SC extends S>
                extends Mqtt3SubscribeBuilderBase.Start<B, C, S, SC>,
                        Mqtt3SubscribeBuilderBase.Complete<B, C>,
                        Mqtt3SubscriptionBuilderBase.Complete<S, SC> {
        // @formatter:on
        }
    }
}

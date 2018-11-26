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
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;

/**
 * @author Silvio Giebl
 */
// @formatter:off
@DoNotImplement
public interface Mqtt5SubscribeBuilderBase<
        B extends Mqtt5SubscribeBuilderBase<B, C>,
        C extends Mqtt5SubscribeBuilderBase.Complete<C>> {
// @formatter:on

    @NotNull C addSubscription(@NotNull Mqtt5Subscription subscription);

    @NotNull Mqtt5SubscriptionBuilder.Nested<? extends C> addSubscription();

    @NotNull B userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends B> userProperties();

    @DoNotImplement
    interface Complete<C extends Mqtt5SubscribeBuilderBase.Complete<C>> {

        @NotNull C addSubscription(@NotNull Mqtt5Subscription subscription);

        @NotNull Mqtt5SubscriptionBuilder.Nested<? extends C> addSubscription();

        @NotNull C userProperties(@NotNull Mqtt5UserProperties userProperties);

        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends C> userProperties();
    }

    // @formatter:off
    @DoNotImplement
    interface First<
            C extends Mqtt5SubscribeBuilderBase.Complete<C>,
            F extends Mqtt5SubscribeBuilderBase.First<C, F, SC>,
            SC extends Mqtt5SubscribeBuilderBase.Start.Complete<C, SC>>
            extends Mqtt5SubscriptionBuilderBase<F, SC> {}
    // @formatter:on

    // @formatter:off
    @DoNotImplement
    interface Start<
            C extends Mqtt5SubscribeBuilderBase.Complete<C>,
            S extends Mqtt5SubscribeBuilderBase.Start<C, S, F, SC>,
            F extends Mqtt5SubscribeBuilderBase.First<C, F, SC>,
            SC extends Mqtt5SubscribeBuilderBase.Start.Complete<C, SC>>
            extends Mqtt5SubscriptionBuilderBase<F, SC> {
    // @formatter:on

        @NotNull C addSubscription(@NotNull Mqtt5Subscription subscription);

        @NotNull Mqtt5SubscriptionBuilder.Nested<? extends C> addSubscription();

        @NotNull S userProperties(@NotNull Mqtt5UserProperties userProperties);

        @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends S> userProperties();

        @DoNotImplement
        interface Complete<C extends Mqtt5SubscribeBuilderBase.Complete<C>, SC extends Mqtt5SubscribeBuilderBase.Start.Complete<C, SC>>
                extends Mqtt5SubscriptionBuilderBase.Complete<SC> {

            @NotNull C addSubscription(@NotNull Mqtt5Subscription subscription);

            @NotNull Mqtt5SubscriptionBuilder.Nested<? extends C> addSubscription();

            @NotNull SC userProperties(@NotNull Mqtt5UserProperties userProperties);

            @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends SC> userProperties();
        }
    }
}

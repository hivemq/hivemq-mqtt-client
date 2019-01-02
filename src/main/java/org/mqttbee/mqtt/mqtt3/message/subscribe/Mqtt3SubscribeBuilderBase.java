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
 */
@DoNotImplement
public interface Mqtt3SubscribeBuilderBase<C extends Mqtt3SubscribeBuilderBase<C>> {

    @NotNull C addSubscription(@NotNull Mqtt3Subscription subscription);

    @NotNull Mqtt3SubscriptionBuilder.Nested<? extends C> addSubscription();

    // @formatter:off
    @DoNotImplement
    interface Start<
            C extends Mqtt3SubscribeBuilderBase<C>,
            SC extends Mqtt3SubscribeBuilderBase.Start.Complete<C, SC>>
            extends Mqtt3SubscribeBuilderBase<C>, Mqtt3SubscriptionBuilderBase<SC> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<
                C extends Mqtt3SubscribeBuilderBase<C>,
                SC extends Mqtt3SubscribeBuilderBase.Start.Complete<C, SC>>
                extends Mqtt3SubscribeBuilderBase.Start<C, SC>, Mqtt3SubscriptionBuilderBase.Complete<SC> {}
        // @formatter:on
    }
}

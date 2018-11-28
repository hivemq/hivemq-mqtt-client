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
@DoNotImplement
public interface Mqtt3SubscriptionBuilder extends Mqtt3SubscriptionBuilderBase<Mqtt3SubscriptionBuilder.Complete> {

    @DoNotImplement
    interface Complete
            extends Mqtt3SubscriptionBuilder, Mqtt3SubscriptionBuilderBase.Complete<Mqtt3SubscriptionBuilder.Complete> {

        @NotNull Mqtt3Subscription build();
    }

    @DoNotImplement
    interface Nested<P> extends Mqtt3SubscriptionBuilderBase<Nested.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Nested<P>, Mqtt3SubscriptionBuilderBase.Complete<Nested.Complete<P>> {

            @NotNull P applySubscription();
        }
    }
}

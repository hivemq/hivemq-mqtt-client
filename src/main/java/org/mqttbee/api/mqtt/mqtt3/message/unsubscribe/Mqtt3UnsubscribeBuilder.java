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

package org.mqttbee.api.mqtt.mqtt3.message.unsubscribe;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt3UnsubscribeBuilder extends Mqtt3UnsubscribeBuilderBase<Mqtt3UnsubscribeBuilder.Complete> {

    @DoNotImplement
    interface Complete extends Mqtt3UnsubscribeBuilder, Mqtt3UnsubscribeBuilderBase<Mqtt3UnsubscribeBuilder.Complete> {

        @NotNull Mqtt3Unsubscribe build();
    }

    @DoNotImplement
    interface Start
            extends Mqtt3UnsubscribeBuilder, Mqtt3UnsubscribeBuilderBase.Start<Mqtt3UnsubscribeBuilder.Complete> {}

    @DoNotImplement
    interface Nested<P> extends Mqtt3UnsubscribeBuilderBase<Nested.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Nested<P>, Mqtt3UnsubscribeBuilderBase<Nested.Complete<P>> {

            @NotNull P applyUnsubscribe();
        }

        @DoNotImplement
        interface Start<P> extends Nested<P>, Mqtt3UnsubscribeBuilderBase.Start<Nested.Complete<P>> {}
    }

    @DoNotImplement
    interface Send<P> extends Mqtt3UnsubscribeBuilderBase<Send.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Send<P>, Mqtt3UnsubscribeBuilderBase<Send.Complete<P>> {

            @NotNull P send();
        }

        @DoNotImplement
        interface Start<P> extends Send<P>, Mqtt3UnsubscribeBuilderBase.Start<Send.Complete<P>> {}
    }
}

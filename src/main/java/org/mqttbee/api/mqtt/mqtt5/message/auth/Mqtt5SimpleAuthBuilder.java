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

package org.mqttbee.api.mqtt.mqtt5.message.auth;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5SimpleAuthBuilder extends Mqtt5SimpleAuthBuilderBase<Mqtt5SimpleAuthBuilder.Complete> {

    @DoNotImplement
    interface Complete extends Mqtt5SimpleAuthBuilder, Mqtt5SimpleAuthBuilderBase<Mqtt5SimpleAuthBuilder.Complete> {

        @NotNull Mqtt5SimpleAuth build();
    }

    @DoNotImplement
    interface Nested<P> extends Mqtt5SimpleAuthBuilderBase<Nested.Complete<P>> {

        @DoNotImplement
        interface Complete<P> extends Nested<P>, Mqtt5SimpleAuthBuilderBase<Nested.Complete<P>> {

            @NotNull P applySimpleAuth();
        }
    }
}

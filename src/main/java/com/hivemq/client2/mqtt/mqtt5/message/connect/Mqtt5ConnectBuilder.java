/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.mqtt.mqtt5.message.connect;

import com.hivemq.client2.annotations.CheckReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5Connect}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ConnectBuilder extends Mqtt5ConnectBuilderBase<Mqtt5ConnectBuilder> {

    /**
     * Builds the {@link Mqtt5Connect}.
     *
     * @return the built {@link Mqtt5Connect}.
     */
    @CheckReturnValue
    @NotNull Mqtt5Connect build();

    /**
     * Builder for a {@link Mqtt5Connect} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Connect} is applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt5ConnectBuilderBase<Nested<P>> {

        /**
         * Builds the {@link Mqtt5Connect} and applies it to the parent.
         *
         * @return the result when the built {@link Mqtt5Connect} is applied to the parent.
         */
        @NotNull P applyConnect();
    }

    /**
     * Builder for a {@link Mqtt5Connect} that is applied to a parent {@link com.hivemq.client2.mqtt.mqtt5.Mqtt5Client}
     * which then sends the Connect message.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Connect} is sent by the parent.
     */
    @ApiStatus.NonExtendable
    interface Send<P> extends Mqtt5ConnectBuilderBase<Send<P>> {

        /**
         * Builds the {@link Mqtt5Connect} and applies it to the parent which then sends the Connect message.
         *
         * @return the result when the built {@link Mqtt5Connect} is sent by the parent.
         */
        @NotNull P send();
    }
}

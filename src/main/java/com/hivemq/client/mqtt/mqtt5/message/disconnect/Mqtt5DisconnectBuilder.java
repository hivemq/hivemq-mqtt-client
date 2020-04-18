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

package com.hivemq.client.mqtt.mqtt5.message.disconnect;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt5Disconnect}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5DisconnectBuilder extends Mqtt5DisconnectBuilderBase<Mqtt5DisconnectBuilder> {

    /**
     * Builds the {@link Mqtt5Disconnect}.
     *
     * @return the built {@link Mqtt5Disconnect}.
     */
    @CheckReturnValue
    @NotNull Mqtt5Disconnect build();

    /**
     * Builder for a {@link Mqtt5Disconnect} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Disconnect} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends Mqtt5DisconnectBuilderBase<Nested<P>> {

        /**
         * Builds the {@link Mqtt5Disconnect} and applies it to the parent.
         *
         * @return the result when the built {@link Mqtt5Disconnect} is applied to the parent.
         */
        @NotNull P applyDisconnect();
    }

    /**
     * Builder for a {@link Mqtt5Disconnect} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client}
     * which then sends the Disconnect message.
     *
     * @param <P> the type of the result when the built {@link Mqtt5Disconnect} is sent by the parent.
     */
    @DoNotImplement
    interface Send<P> extends Mqtt5DisconnectBuilderBase<Send<P>> {

        /**
         * Builds the {@link Mqtt5Disconnect} and applies it to the parent which then sends the Disconnect message.
         *
         * @return the result when the built {@link Mqtt5Disconnect} is sent by the parent.
         */
        @NotNull P send();
    }

    /**
     * Builder for a {@link Mqtt5Disconnect} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client}
     * which then sends the Disconnect message without returning a result.
     */
    @DoNotImplement
    interface SendVoid extends Mqtt5DisconnectBuilderBase<SendVoid> {

        /**
         * Builds the {@link Mqtt5Disconnect} and applies it to the parent which then sends the Disconnect message
         * without returning a result.
         */
        void send();
    }
}

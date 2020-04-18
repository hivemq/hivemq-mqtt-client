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

package com.hivemq.client.mqtt.mqtt3.message.connect;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for a {@link Mqtt3Connect}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3ConnectBuilder extends Mqtt3ConnectBuilderBase<Mqtt3ConnectBuilder> {

    /**
     * Builds the {@link Mqtt3Connect}.
     *
     * @return the built {@link Mqtt3Connect}.
     */
    @CheckReturnValue
    @NotNull Mqtt3Connect build();

    /**
     * Builder for a {@link Mqtt3Connect} that is applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Connect} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends Mqtt3ConnectBuilderBase<Nested<P>> {

        /**
         * Builds the {@link Mqtt3Connect} and applies it to the parent.
         *
         * @return the result when the built {@link Mqtt3Connect} is applied to the parent.
         */
        @NotNull P applyConnect();
    }

    /**
     * Builder for a {@link Mqtt3Connect} that is applied to a parent {@link com.hivemq.client.mqtt.mqtt3.Mqtt3Client}
     * which then sends the Connect message.
     *
     * @param <P> the type of the result when the built {@link Mqtt3Connect} is sent by the parent.
     */
    @DoNotImplement
    interface Send<P> extends Mqtt3ConnectBuilderBase<Send<P>> {

        /**
         * Builds the {@link Mqtt3Connect} and applies it to the parent which then sends the Connect message.
         *
         * @return the result when the built {@link Mqtt3Connect} is sent by the parent.
         */
        @NotNull P send();
    }
}

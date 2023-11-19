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

package com.hivemq.mqtt.client2.mqtt3.message.connect;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for {@link Mqtt3ConnectRestrictions}.
 *
 * @author Yannick Weber
 * @since 1.3
 */
@ApiStatus.NonExtendable
public interface Mqtt3ConnectRestrictionsBuilder
        extends Mqtt3ConnectRestrictionsBuilderBase<Mqtt3ConnectRestrictionsBuilder> {

    /**
     * Builds the {@link Mqtt3ConnectRestrictions}.
     *
     * @return the built {@link Mqtt3ConnectRestrictions}.
     */
    @CheckReturnValue
    @NotNull Mqtt3ConnectRestrictions build();

    /**
     * Builder for {@link Mqtt3ConnectRestrictions} that are applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt3ConnectRestrictions} are applied to the parent.
     */
    @ApiStatus.NonExtendable
    interface Nested<P> extends Mqtt3ConnectRestrictionsBuilderBase<Nested<P>> {

        /**
         * Builds the {@link Mqtt3ConnectRestrictions} and applies them to the parent.
         *
         * @return the result when the built {@link Mqtt3ConnectRestrictions} are applied to the parent.
         */
        @NotNull P applyRestrictions();
    }
}

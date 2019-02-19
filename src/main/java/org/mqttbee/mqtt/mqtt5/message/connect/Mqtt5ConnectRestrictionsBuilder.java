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

package org.mqttbee.mqtt.mqtt5.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * Builder for {@link Mqtt5ConnectRestrictions}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5ConnectRestrictionsBuilder
        extends Mqtt5ConnectRestrictionsBuilderBase<Mqtt5ConnectRestrictionsBuilder> {

    /**
     * Switches to the child builder for {@link Mqtt5ConnectRestrictions.ForClient}.
     *
     * @return the child builder.
     */
    @NotNull ForClient forClient();

    /**
     * Builds the {@link Mqtt5ConnectRestrictions}.
     *
     * @return the built {@link Mqtt5ConnectRestrictions}.
     */
    @NotNull Mqtt5ConnectRestrictions build();

    /**
     * Builder for {@link Mqtt5ConnectRestrictions.ForClient} as part of {@link Mqtt5ConnectRestrictions}.
     */
    @DoNotImplement
    interface ForClient
            extends Mqtt5ConnectRestrictionsBuilderBase.ForClient<Mqtt5ConnectRestrictionsBuilder.ForClient> {

        /**
         * Switches to the parent builder for {@link Mqtt5ConnectRestrictions}.
         *
         * @return the parent builder.
         */
        @NotNull Mqtt5ConnectRestrictionsBuilder forServer();

        /**
         * Builds the {@link Mqtt5ConnectRestrictions} with the {@link Mqtt5ConnectRestrictions.ForClient}.
         *
         * @return the built {@link Mqtt5ConnectRestrictions}.
         */
        @NotNull Mqtt5ConnectRestrictions build();
    }

    /**
     * Builder for a {@link Mqtt5ConnectRestrictions} that are applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5ConnectRestrictions} is applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends Mqtt5ConnectRestrictionsBuilderBase<Nested<P>> {

        /**
         * Switches to the child builder for {@link Mqtt5ConnectRestrictions.ForClient}.
         *
         * @return the child builder.
         */
        @NotNull ForClient<P> forClient();

        /**
         * Builds the {@link Mqtt5ConnectRestrictions} and applies them to the parent.
         *
         * @return the result when the built {@link Mqtt5ConnectRestrictions} are applied to the parent.
         */
        @NotNull P applyRestrictions();

        /**
         * Builder for {@link Mqtt5ConnectRestrictions.ForClient} as part of {@link Mqtt5ConnectRestrictions} that are
         * applied to a parent.
         *
         * @param <P> the type of the result when the built {@link Mqtt5ConnectRestrictions} is applied to the parent.
         */
        @DoNotImplement
        interface ForClient<P> extends Mqtt5ConnectRestrictionsBuilderBase.ForClient<Nested.ForClient<P>> {

            /**
             * Switches to the parent builder for {@link Mqtt5ConnectRestrictions}.
             *
             * @return the parent builder.
             */
            @NotNull Nested<P> forServer();

            /**
             * Builds the {@link Mqtt5ConnectRestrictions} with the {@link Mqtt5ConnectRestrictions.ForClient} and
             * applies them to the parent.
             *
             * @return the result when the built {@link Mqtt5ConnectRestrictions} are applied to the parent.
             */
            @NotNull P applyRestrictions();
        }
    }
}

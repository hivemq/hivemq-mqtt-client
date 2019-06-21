/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.mqtt.mqtt5.datatypes;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Builder base for {@link Mqtt5UserProperties}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5UserPropertiesBuilderBase<B extends Mqtt5UserPropertiesBuilderBase<B>> {

    /**
     * Adds a {@link Mqtt5UserProperty User Property}.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the builder.
     */
    @NotNull B add(@NotNull String name, @NotNull String value);

    /**
     * Adds a {@link Mqtt5UserProperty User Property}.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the builder.
     */
    @NotNull B add(@NotNull MqttUtf8String name, @NotNull MqttUtf8String value);

    /**
     * Adds a {@link Mqtt5UserProperty User Property}.
     *
     * @param userProperty the User Property.
     * @return the builder.
     */
    @NotNull B add(@NotNull Mqtt5UserProperty userProperty);

    /**
     * Adds {@link Mqtt5UserProperty User Properties}.
     *
     * @param userProperties the User Properties.
     * @return the builder.
     * @since 1.2
     */
    @NotNull B addAll(@NotNull Mqtt5UserProperty @NotNull ... userProperties);

    /**
     * Adds a collection of {@link Mqtt5UserProperty User Properties}.
     *
     * @param userProperties the collection of User Properties.
     * @return the builder.
     * @since 1.2
     */
    @NotNull B addAll(@NotNull Collection<@NotNull Mqtt5UserProperty> userProperties);

    /**
     * Adds a stream of {@link Mqtt5UserProperty User Properties}.
     *
     * @param userProperties the stream of User Properties.
     * @return the builder.
     * @since 1.2
     */
    @NotNull B addAll(@NotNull Stream<@NotNull Mqtt5UserProperty> userProperties);
}

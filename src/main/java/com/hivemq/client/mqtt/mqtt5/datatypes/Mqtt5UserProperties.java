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

package com.hivemq.client.mqtt.mqtt5.datatypes;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

/**
 * Collection of {@link Mqtt5UserProperty User Properties}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5UserProperties {

    /**
     * @return an empty collection of User Properties.
     */
    static @NotNull Mqtt5UserProperties of() {
        return MqttUserPropertiesImpl.NO_USER_PROPERTIES;
    }

    /**
     * Creates a collection of User Properties of individual User Properties.
     *
     * @param userProperties the individual User Properties.
     * @return the created collection of User Properties.
     */
    static @NotNull Mqtt5UserProperties of(final @NotNull Mqtt5UserProperty @NotNull ... userProperties) {
        return MqttChecks.userProperties(userProperties);
    }

    /**
     * Creates a collection of User Properties of a list of User Properties.
     *
     * @param userProperties the list of User Properties.
     * @return the created collection of User Properties.
     */
    static @NotNull Mqtt5UserProperties of(final @NotNull Collection<@NotNull Mqtt5UserProperty> userProperties) {
        return MqttChecks.userProperties(userProperties);
    }

    /**
     * Creates a builder for a collection of User Properties.
     *
     * @return the created builder for a collection of User Properties.
     */
    static @NotNull Mqtt5UserPropertiesBuilder builder() {
        return new MqttUserPropertiesImplBuilder.Default();
    }

    /**
     * @return the User Properties as an immutable list.
     */
    @Unmodifiable @NotNull List<@NotNull ? extends Mqtt5UserProperty> asList();

    /**
     * @return a builder for extending this collection of User Properties.
     */
    @NotNull Mqtt5UserPropertiesBuilder extend();
}

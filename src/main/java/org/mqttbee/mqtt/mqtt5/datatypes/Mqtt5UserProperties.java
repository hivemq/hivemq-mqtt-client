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

package org.mqttbee.mqtt.mqtt5.datatypes;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import org.mqttbee.internal.mqtt.util.MqttChecks;

import java.util.List;

/**
 * Collection of {@link Mqtt5UserProperty User Properties}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
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
    static @NotNull Mqtt5UserProperties of(final @NotNull List<@NotNull Mqtt5UserProperty> userProperties) {
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
    @Immutable @NotNull List<@NotNull ? extends Mqtt5UserProperty> asList();

    /**
     * @return a builder for extending this collection of User Properties.
     */
    @NotNull Mqtt5UserPropertiesBuilder extend();
}

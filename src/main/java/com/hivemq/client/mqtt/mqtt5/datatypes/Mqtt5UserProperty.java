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

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT User Property according to the MQTT 5 specification.
 * <p>
 * A User Property consists of a name and value UTF-8 encoded String Pair.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5UserProperty extends Comparable<Mqtt5UserProperty> {

    /**
     * Creates an User Property of the given name and value.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the created User Property.
     */
    static @NotNull Mqtt5UserProperty of(final @NotNull String name, final @NotNull String value) {
        return MqttUserPropertyImpl.of(name, value);
    }

    /**
     * Creates an User Property of the given name and value.
     *
     * @param name  the name of the User Property.
     * @param value the value of the User Property.
     * @return the created User Property.
     */
    static @NotNull Mqtt5UserProperty of(final @NotNull MqttUtf8String name, final @NotNull MqttUtf8String value) {
        return MqttChecks.userProperty(name, value);
    }

    /**
     * @return the name of this User Property.
     */
    @NotNull MqttUtf8String getName();

    /**
     * @return the value of this User Property.
     */
    @NotNull MqttUtf8String getValue();
}

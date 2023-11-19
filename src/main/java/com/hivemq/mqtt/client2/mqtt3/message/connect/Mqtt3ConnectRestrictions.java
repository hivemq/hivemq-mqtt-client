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

import com.hivemq.mqtt.client2.internal.datatypes.MqttVariableByteInteger;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnectRestrictionsBuilder;
import com.hivemq.mqtt.client2.internal.util.UnsignedDataTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Restrictions applied by the client in an {@link Mqtt3Connect MQTT 3 Connect message}.
 *
 * @author Yannick Weber
 * @since 1.3
 */
@ApiStatus.NonExtendable
public interface Mqtt3ConnectRestrictions {

    /**
     * Creates a builder for Connect restrictions.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt3ConnectRestrictionsBuilder builder() {
        return new MqttConnectRestrictionsBuilder.Default();
    }

    /**
     * Returns the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
     * concurrently. The default is
     * {@link com.hivemq.mqtt.client2.mqtt5.message.connect.Mqtt5ConnectRestrictions#DEFAULT_SEND_MAXIMUM
     * DEFAULT_SEND_MAXIMUM}.
     *
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
     *         concurrently.
     */
    @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendMaximum();

    /**
     * Returns the maximum packet size the client sends to the server. The default is
     * {@link com.hivemq.mqtt.client2.mqtt5.message.connect.Mqtt5ConnectRestrictions#DEFAULT_SEND_MAXIMUM_PACKET_SIZE
     * DEFAULT_SEND_MAXIMUM_PACKET_SIZE}.
     *
     * @return the maximum packet size the client sends to the server.
     */
    @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getSendMaximumPacketSize();

    /**
     * Creates a builder for extending this Connect restrictions.
     *
     * @return the created builder.
     */
    @NotNull Mqtt3ConnectRestrictionsBuilder extend();
}

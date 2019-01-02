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
import org.mqttbee.internal.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.internal.mqtt.message.connect.MqttConnectRestrictionsBuilder;

/**
 * Restrictions from the client in the MQTT 5 CONNECT packet.
 */
@DoNotImplement
public interface Mqtt5ConnectRestrictions {

    /**
     * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts concurrently.
     */
    int DEFAULT_RECEIVE_MAXIMUM = 65_535;
    /**
     * The default maximum packet size the client accepts from the server which indicates that the packet size is not
     * limited beyond the restrictions of the encoding.
     */
    int DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
    /**
     * The default maximum amount of topic aliases the client accepts from the server.
     */
    int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;

    static @NotNull Mqtt5ConnectRestrictionsBuilder builder() {
        return new MqttConnectRestrictionsBuilder.Default();
    }

    /**
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts concurrently. The
     *         default is {@link #DEFAULT_RECEIVE_MAXIMUM}.
     */
    int getReceiveMaximum();

    /**
     * @return the maximum packet size the client accepts from the server. The default is {@link
     *         #DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT}.
     */
    int getMaximumPacketSize();

    /**
     * @return the maximum amount of topic aliases the client accepts from the server. The default is {@link
     *         #DEFAULT_TOPIC_ALIAS_MAXIMUM}.
     */
    int getTopicAliasMaximum();

}

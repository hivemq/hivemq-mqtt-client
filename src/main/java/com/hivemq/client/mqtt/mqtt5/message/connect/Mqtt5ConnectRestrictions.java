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

package com.hivemq.client.mqtt.mqtt5.message.connect;

import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictionsBuilder;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Restrictions set by the client in an {@link Mqtt5Connect MQTT 5 Connect message}.
 * <p>
 * These restrictions consist of:
 * <ul>
 *   <li>Restrictions for the server.
 * <p>
 *     These are used to form the {@link com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig.RestrictionsForServer
 *     Mqtt5ClientConnectionConfig.RestrictionsForServer}
 *   <li>Restrictions for the client set by the client itself.
 * <p>
 *     These restrictions are used in conjunction with the
 *     {@link Mqtt5ConnAckRestrictions}
 *     to form the {@link com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig.RestrictionsForClient
 *     Mqtt5ClientConnectionConfig.RestrictionsForClient}.
 * </ul>
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ConnectRestrictions {

    /**
     * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server
     * concurrently.
     */
    int DEFAULT_RECEIVE_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    /**
     * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
     * concurrently.
     */
    int DEFAULT_SEND_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    /**
     * The default maximum packet size the client accepts from the server. By default the packet size is not limited
     * beyond the restrictions of the encoding.
     */
    int DEFAULT_MAXIMUM_PACKET_SIZE = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
    /**
     * The default maximum packet size the client sends to the server. By default the packet size is not limited beyond
     * the restrictions of the encoding.
     */
    int DEFAULT_SEND_MAXIMUM_PACKET_SIZE = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
    /**
     * The default maximum amount of topic aliases the client accepts from the server.
     */
    int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
    /**
     * The default maximum amount of topic aliases the client sends to the server.
     */
    int DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM = 16;
    /**
     * Default whether the client accepts problem information from the server.
     */
    boolean DEFAULT_REQUEST_PROBLEM_INFORMATION = true;
    /**
     * Default whether the client requests problem information from the server.
     */
    boolean DEFAULT_REQUEST_RESPONSE_INFORMATION = false;

    /**
     * Creates a builder for Connect restrictions.
     *
     * @return the created builder.
     */
    static @NotNull Mqtt5ConnectRestrictionsBuilder builder() {
        return new MqttConnectRestrictionsBuilder.Default();
    }

    /**
     * Returns the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server
     * concurrently. The default is {@link #DEFAULT_RECEIVE_MAXIMUM}.
     *
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server
     *         concurrently.
     */
    @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getReceiveMaximum();

    /**
     * Returns the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
     * concurrently. The default is {@link #DEFAULT_SEND_MAXIMUM}.
     * <p>
     * The actual amount a connected client will use is determined by the minimum of this value and {@link
     * Mqtt5ConnAckRestrictions#getReceiveMaximum() MqttConnAckRestrictions#getReceiveMaximum()}.
     *
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
     *         concurrently.
     */
    @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendMaximum();

    /**
     * Returns the maximum packet size the client accepts from the server. The default is {@link
     * #DEFAULT_MAXIMUM_PACKET_SIZE}.
     *
     * @return the maximum packet size the client accepts from the server.
     */
    @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getMaximumPacketSize();

    /**
     * Returns the maximum packet size the client sends to the server. The default is {@link
     * #DEFAULT_SEND_MAXIMUM_PACKET_SIZE}.
     * <p>
     * The actual size a connected client will use is determined by the minimum of this value and {@link
     * Mqtt5ConnAckRestrictions#getMaximumPacketSize() MqttConnAckRestrictions#getMaximumPacketSize()}.
     *
     * @return the maximum packet size the client sends to the server.
     */
    @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getSendMaximumPacketSize();

    /**
     * Returns the maximum amount of topic aliases the client accepts from the server. The default is {@link
     * #DEFAULT_TOPIC_ALIAS_MAXIMUM}.
     *
     * @return the maximum amount of topic aliases the client accepts from the server.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getTopicAliasMaximum();

    /**
     * Returns the maximum amount of topic aliases the client sends to the server. The default is {@link
     * #DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM}.
     * <p>
     * The actual amount a connected client will use is determined by the minimum of this value and {@link
     * Mqtt5ConnAckRestrictions#getTopicAliasMaximum() MqttConnAckRestrictions#getTopicAliasMaximum()}.
     *
     * @return the maximum amount of topic aliases the client sends to the server.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendTopicAliasMaximum();

    /**
     * Returns whether the client requests problem information from the server. The default is {@link
     * #DEFAULT_REQUEST_PROBLEM_INFORMATION}.
     *
     * @return whether the client requests problem information from the server.
     */
    boolean isRequestProblemInformation();

    /**
     * Returns whether the client requests response information from the server. The default is {@link
     * #DEFAULT_REQUEST_RESPONSE_INFORMATION}.
     *
     * @return whether the client requests response information from the server.
     */
    boolean isRequestResponseInformation();

    /**
     * Creates a builder for extending this Connect restrictions.
     *
     * @return the created builder.
     */
    @NotNull Mqtt5ConnectRestrictionsBuilder extend();
}

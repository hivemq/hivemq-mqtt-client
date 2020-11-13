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

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.NotNull;

/**
 * Restrictions for the client set by the server in an {@link Mqtt5ConnAck MQTT 5 ConnAck message}.
 * <p>
 * These restrictions are used in conjunction with the {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions
 * Mqtt5ConnectRestrictions} to form the {@link com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig.RestrictionsForClient
 * Mqtt5ClientConnectionConfig.RestrictionsForClient}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5ConnAckRestrictions {

    /**
     * The default maximum amount of not acknowledged publishes with QoS 1 or 2 the server accepts concurrently.
     */
    int DEFAULT_RECEIVE_MAXIMUM = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    /**
     * The default maximum packet size the server accepts from the client which indicates that the packet size is not
     * limited beyond the restrictions of the encoding.
     */
    int DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;
    /**
     * The default maximum amount of topic aliases the server accepts from the client.
     */
    int DEFAULT_TOPIC_ALIAS_MAXIMUM = 0;
    /**
     * The default maximum QoS the server accepts from the client.
     */
    @NotNull MqttQos DEFAULT_MAXIMUM_QOS = MqttQos.EXACTLY_ONCE;
    /**
     * The default for whether the server accepts retained messages.
     */
    boolean DEFAULT_RETAIN_AVAILABLE = true;
    /**
     * The default for whether the server accepts wildcard subscriptions.
     */
    boolean DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE = true;
    /**
     * The default for whether the server accepts shared subscriptions.
     */
    boolean DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE = true;
    /**
     * The default for whether the server accepts subscription identifiers.
     */
    boolean DEFAULT_SUBSCRIPTION_IDENTIFIERS_AVAILABLE = true;

    /**
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the server accepts concurrently. The
     *         default is {@link #DEFAULT_RECEIVE_MAXIMUM}.
     */
    int getReceiveMaximum();

    /**
     * @return the maximum packet size the server accepts from the client. The default is {@link
     *         #DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT}.
     */
    int getMaximumPacketSize();

    /**
     * @return the maximum amount of topic aliases the server accepts from the client. The default is {@link
     *         #DEFAULT_TOPIC_ALIAS_MAXIMUM}.
     */
    int getTopicAliasMaximum();

    /**
     * @return the maximum QoS the server accepts from the client. The default is {@link #DEFAULT_MAXIMUM_QOS}.
     */
    @NotNull MqttQos getMaximumQos();

    /**
     * @return whether the server accepts retained messages. The default is {@link #DEFAULT_RETAIN_AVAILABLE}.
     */
    boolean isRetainAvailable();

    /**
     * @return whether the server accepts wildcard subscriptions. The default is {@link
     *         #DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE}.
     */
    boolean isWildcardSubscriptionAvailable();

    /**
     * @return whether the server accepts shared subscriptions. The default is {@link #DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE}.
     */
    boolean isSharedSubscriptionAvailable();

    /**
     * @return whether the server accepts subscription identifiers. The default is {@link
     *         #DEFAULT_SUBSCRIPTION_IDENTIFIERS_AVAILABLE}.
     */
    boolean areSubscriptionIdentifiersAvailable();
}

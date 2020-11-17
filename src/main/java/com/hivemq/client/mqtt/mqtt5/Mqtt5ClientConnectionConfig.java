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

package com.hivemq.client.mqtt.mqtt5;

import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Optional;

/**
 * Connection configuration of an {@link Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ClientConnectionConfig extends MqttClientConnectionConfig {

    /**
     * @return the session expiry interval in seconds.
     */
    @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE) long getSessionExpiryInterval();

    /**
     * @return the optional enhanced auth mechanism that is used for enhanced authentication and/or authorization.
     */
    @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism();

    /**
     * @return the restrictions for messages the client receives.
     */
    @NotNull RestrictionsForServer getRestrictionsForServer();

    /**
     * @return the restrictions for messages the client sends.
     */
    @NotNull RestrictionsForClient getRestrictionsForClient();

    /**
     * Restrictions for messages a {@link Mqtt5Client} receives.
     */
    @ApiStatus.NonExtendable
    interface RestrictionsForServer {

        /**
         * Returns the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server
         * concurrently.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#getReceiveMaximum
         * Mqtt5ConnectRestrictions#getReceiveMaximum()}.
         *
         * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server
         *         concurrently.
         */
        @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getReceiveMaximum();

        /**
         * Returns the maximum packet size the client accepts from the server.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#getMaximumPacketSize
         * Mqtt5ConnectRestrictions#getMaximumPacketSize()}.
         *
         * @return the maximum packet size the client accepts from the server.
         */
        @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getMaximumPacketSize();

        /**
         * Returns the maximum amount of topic aliases the client accepts from the server.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#getTopicAliasMaximum
         * Mqtt5ConnectRestrictions#getTopicAliasMaximum()}.
         *
         * @return the maximum amount of topic aliases the client accepts from the server.
         */
        @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getTopicAliasMaximum();

        /**
         * Returns whether the client requested problem information from the server.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#isRequestProblemInformation
         * Mqtt5ConnectRestrictions#isRequestProblemInformation()}.
         *
         * @return whether the client requested problem information from the server.
         */
        boolean isProblemInformationRequested();

        /**
         * Returns whether the client requested response information from the server.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#isRequestResponseInformation
         * Mqtt5ConnectRestrictions#isRequestResponseInformation()}.
         *
         * @return whether the client requested response information from the server.
         */
        boolean isResponseInformationRequested();
    }

    /**
     * Restrictions for messages a {@link Mqtt5Client} sends.
     */
    @ApiStatus.NonExtendable
    interface RestrictionsForClient {

        /**
         * Returns the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
         * concurrently.
         * <p>
         * The value is determined by the minimum of {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#getSendMaximum
         * Mqtt5ConnectRestrictions#getSendMaximum()} and {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#getReceiveMaximum()
         * MqttConnAckRestrictions#getReceiveMaximum()}.
         *
         * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server
         *         concurrently.
         */
        @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendMaximum();

        /**
         * Returns the maximum packet size the client sends to the server.
         * <p>
         * The value is determined by the minimum of {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#getSendMaximumPacketSize
         * Mqtt5ConnectRestrictions#getSendMaximumPacketSize()} and {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#getMaximumPacketSize()
         * MqttConnAckRestrictions#getMaximumPacketSize()}.
         *
         * @return the maximum packet size the client sends to the server.
         */
        @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getSendMaximumPacketSize();

        /**
         * Returns the maximum amount of topic aliases the client sends to the server.
         * <p>
         * The value is determined by the minimum of {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions#getSendTopicAliasMaximum
         * Mqtt5ConnectRestrictions#getSendTopicAliasMaximum()} and {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#getTopicAliasMaximum()
         * MqttConnAckRestrictions#getTopicAliasMaximum()}.
         *
         * @return the maximum amount of topic aliases the client sends to the server.
         */
        @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendTopicAliasMaximum();

        /**
         * Returns the maximum {@link MqttQos QoS} the server accepts from the client.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#getMaximumQos()
         * MqttConnAckRestrictions#getMaximumQos()}.
         *
         * @return the maximum QoS the server accepts from the client.
         */
        @NotNull MqttQos getMaximumQos();

        /**
         * Returns whether the server accepts retained messages from the client.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#isRetainAvailable()
         * MqttConnAckRestrictions#isRetainAvailable()}.
         *
         * @return whether the server accepts retained messages from the client.
         */
        boolean isRetainAvailable();

        /**
         * Returns whether the server accepts wildcard subscriptions from the client.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#isWildcardSubscriptionAvailable()
         * MqttConnAckRestrictions#isWildcardSubscriptionAvailable()}.
         *
         * @return whether the server accepts wildcard subscriptions.
         */
        boolean isWildcardSubscriptionAvailable();

        /**
         * Returns whether the server accepts shared subscriptions from the client.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#isSharedSubscriptionAvailable()
         * MqttConnAckRestrictions#isSharedSubscriptionAvailable()}.
         *
         * @return whether the server accepts shared subscriptions from the client.
         */
        boolean isSharedSubscriptionAvailable();

        /**
         * Returns whether the server accepts subscription identifiers from the client.
         * <p>
         * The value is determined by {@link com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions#areSubscriptionIdentifiersAvailable()
         * MqttConnAckRestrictions#areSubscriptionIdentifiersAvailable()}.
         *
         * @return whether the server accepts subscription identifiers from the client.
         */
        boolean areSubscriptionIdentifiersAvailable();
    }
}

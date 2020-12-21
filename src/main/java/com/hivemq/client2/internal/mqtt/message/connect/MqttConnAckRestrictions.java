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

package com.hivemq.client2.internal.mqtt.message.connect;

import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttConnAckRestrictions implements Mqtt5ConnAckRestrictions {

    public static final @NotNull MqttConnAckRestrictions DEFAULT =
            new MqttConnAckRestrictions(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT,
                    DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_MAXIMUM_QOS, DEFAULT_RETAIN_AVAILABLE,
                    DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE, DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE,
                    DEFAULT_SUBSCRIPTION_IDENTIFIERS_AVAILABLE);

    private final @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int receiveMaximum;
    private final @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int maximumPacketSize;
    private final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int topicAliasMaximum;
    private final @NotNull MqttQos maximumQos;
    private final boolean retainAvailable;
    private final boolean wildcardSubscriptionAvailable;
    private final boolean sharedSubscriptionAvailable;
    private final boolean subscriptionIdentifiersAvailable;

    public MqttConnAckRestrictions(
            final @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int receiveMaximum,
            final @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int maximumPacketSize,
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int topicAliasMaximum,
            final @NotNull MqttQos maximumQos,
            final boolean retainAvailable,
            final boolean wildcardSubscriptionAvailable,
            final boolean sharedSubscriptionAvailable,
            final boolean subscriptionIdentifiersAvailable) {

        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.maximumQos = maximumQos;
        this.retainAvailable = retainAvailable;
        this.wildcardSubscriptionAvailable = wildcardSubscriptionAvailable;
        this.sharedSubscriptionAvailable = sharedSubscriptionAvailable;
        this.subscriptionIdentifiersAvailable = subscriptionIdentifiersAvailable;
    }

    @Override
    public @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public @NotNull MqttQos getMaximumQos() {
        return maximumQos;
    }

    @Override
    public boolean isRetainAvailable() {
        return retainAvailable;
    }

    @Override
    public boolean isWildcardSubscriptionAvailable() {
        return wildcardSubscriptionAvailable;
    }

    @Override
    public boolean isSharedSubscriptionAvailable() {
        return sharedSubscriptionAvailable;
    }

    @Override
    public boolean areSubscriptionIdentifiersAvailable() {
        return subscriptionIdentifiersAvailable;
    }

    private @NotNull String toAttributeString() {
        return "receiveMaximum=" + receiveMaximum + ", maximumPacketSize=" + maximumPacketSize +
                ", topicAliasMaximum=" + topicAliasMaximum + ", maximumQos=" + maximumQos + ", retainAvailable=" +
                retainAvailable + ", wildcardSubscriptionAvailable=" + wildcardSubscriptionAvailable +
                ", sharedSubscriptionAvailable=" + sharedSubscriptionAvailable + ", subscriptionIdentifiersAvailable=" +
                subscriptionIdentifiersAvailable;
    }

    @Override
    public @NotNull String toString() {
        return "MqttConnAckRestrictions{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttConnAckRestrictions)) {
            return false;
        }
        final MqttConnAckRestrictions that = (MqttConnAckRestrictions) o;

        return (receiveMaximum == that.receiveMaximum) && (maximumPacketSize == that.maximumPacketSize) &&
                (topicAliasMaximum == that.topicAliasMaximum) && (maximumQos == that.maximumQos) &&
                (retainAvailable == that.retainAvailable) &&
                (wildcardSubscriptionAvailable == that.wildcardSubscriptionAvailable) &&
                (sharedSubscriptionAvailable == that.sharedSubscriptionAvailable) &&
                (subscriptionIdentifiersAvailable == that.subscriptionIdentifiersAvailable);
    }

    @Override
    public int hashCode() {
        int result = receiveMaximum;
        result = 31 * result + maximumPacketSize;
        result = 31 * result + topicAliasMaximum;
        result = 31 * result + maximumQos.hashCode();
        result = 31 * result + Boolean.hashCode(retainAvailable);
        result = 31 * result + Boolean.hashCode(wildcardSubscriptionAvailable);
        result = 31 * result + Boolean.hashCode(sharedSubscriptionAvailable);
        result = 31 * result + Boolean.hashCode(subscriptionIdentifiersAvailable);
        return result;
    }
}

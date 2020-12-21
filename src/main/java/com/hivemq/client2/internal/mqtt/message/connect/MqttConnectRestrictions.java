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
import com.hivemq.client2.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttConnectRestrictions implements Mqtt5ConnectRestrictions {

    public static final @NotNull MqttConnectRestrictions DEFAULT =
            new MqttConnectRestrictions(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_SEND_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE,
                    DEFAULT_SEND_MAXIMUM_PACKET_SIZE, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM,
                    DEFAULT_REQUEST_PROBLEM_INFORMATION, DEFAULT_REQUEST_RESPONSE_INFORMATION);

    private final @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int receiveMaximum;
    private final @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int sendMaximum;
    private final @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int maximumPacketSize;
    private final @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int sendMaximumPacketSize;
    private final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int topicAliasMaximum;
    private final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int sendTopicAliasMaximum;
    private final boolean requestProblemInformation;
    private final boolean requestResponseInformation;

    public MqttConnectRestrictions(
            final @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int receiveMaximum,
            final @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int sendMaximum,
            final @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int maximumPacketSize,
            final @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int sendMaximumPacketSize,
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int topicAliasMaximum,
            final @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int sendTopicAliasMaximum,
            final boolean requestProblemInformation,
            final boolean requestResponseInformation) {

        this.receiveMaximum = receiveMaximum;
        this.sendMaximum = sendMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.sendMaximumPacketSize = sendMaximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.sendTopicAliasMaximum = sendTopicAliasMaximum;
        this.requestProblemInformation = requestProblemInformation;
        this.requestResponseInformation = requestResponseInformation;
    }

    @Override
    public @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public @Range(from = 1, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendMaximum() {
        return sendMaximum;
    }

    @Override
    public @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public @Range(from = 1, to = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) int getSendMaximumPacketSize() {
        return sendMaximumPacketSize;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public @Range(from = 0, to = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE) int getSendTopicAliasMaximum() {
        return sendTopicAliasMaximum;
    }

    @Override
    public boolean isRequestProblemInformation() {
        return requestProblemInformation;
    }

    @Override
    public boolean isRequestResponseInformation() {
        return requestResponseInformation;
    }

    @Override
    public @NotNull MqttConnectRestrictionsBuilder.Default extend() {
        return new MqttConnectRestrictionsBuilder.Default(this);
    }

    private @NotNull String toAttributeString() {
        return "receiveMaximum=" + receiveMaximum + ", sendMaximum=" + sendMaximum + ", maximumPacketSize=" +
                maximumPacketSize + ", sendMaximumPacketSize=" + sendMaximumPacketSize + ", topicAliasMaximum=" +
                topicAliasMaximum + ", sendTopicAliasMaximum=" + sendTopicAliasMaximum +
                ", requestProblemInformation=" + requestProblemInformation + ", requestResponseInformation=" +
                requestResponseInformation;
    }

    @Override
    public @NotNull String toString() {
        return "MqttConnectRestrictions{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttConnectRestrictions)) {
            return false;
        }
        final MqttConnectRestrictions that = (MqttConnectRestrictions) o;

        return (receiveMaximum == that.receiveMaximum) && (sendMaximum == that.sendMaximum) &&
                (maximumPacketSize == that.maximumPacketSize) &&
                (sendMaximumPacketSize == that.sendMaximumPacketSize) &&
                (topicAliasMaximum == that.topicAliasMaximum) &&
                (sendTopicAliasMaximum == that.sendTopicAliasMaximum) &&
                (requestProblemInformation == that.requestProblemInformation) &&
                (requestResponseInformation == that.requestResponseInformation);
    }

    @Override
    public int hashCode() {
        int result = receiveMaximum;
        result = 31 * result + sendMaximum;
        result = 31 * result + maximumPacketSize;
        result = 31 * result + sendMaximumPacketSize;
        result = 31 * result + topicAliasMaximum;
        result = 31 * result + sendTopicAliasMaximum;
        result = 31 * result + Boolean.hashCode(requestProblemInformation);
        result = 31 * result + Boolean.hashCode(requestResponseInformation);
        return result;
    }
}

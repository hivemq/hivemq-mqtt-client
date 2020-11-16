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

package com.hivemq.client.internal.mqtt.message.connect;

import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private final int receiveMaximum;
    private final int sendMaximum;
    private final int maximumPacketSize;
    private final int sendMaximumPacketSize;
    private final int topicAliasMaximum;
    private final int sendTopicAliasMaximum;
    private final boolean requestProblemInformation;
    private final boolean requestResponseInformation;

    public MqttConnectRestrictions(
            final int receiveMaximum,
            final int sendMaximum,
            final int maximumPacketSize,
            final int sendMaximumPacketSize,
            final int topicAliasMaximum,
            final int sendTopicAliasMaximum,
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
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getSendMaximum() {
        return sendMaximum;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public int getSendMaximumPacketSize() {
        return sendMaximumPacketSize;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public int getSendTopicAliasMaximum() {
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

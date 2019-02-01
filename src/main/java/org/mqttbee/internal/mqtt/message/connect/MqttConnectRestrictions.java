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

package org.mqttbee.internal.mqtt.message.connect;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnectRestrictions implements Mqtt5ConnectRestrictions {

    public static final @NotNull MqttConnectRestrictions DEFAULT =
            new MqttConnectRestrictions(DEFAULT_RECEIVE_MAXIMUM, DEFAULT_SEND_MAXIMUM, DEFAULT_MAXIMUM_PACKET_SIZE,
                    DEFAULT_SEND_MAXIMUM_PACKET_SIZE, DEFAULT_TOPIC_ALIAS_MAXIMUM, DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM,
                    DEFAULT_REQUEST_PROBLEM_INFORMATION, DEFAULT_SEND_PROBLEM_INFORMATION,
                    DEFAULT_REQUEST_RESPONSE_INFORMATION);

    private final int receiveMaximum;
    private final int sendMaximum;
    private final int maximumPacketSize;
    private final int sendMaximumPacketSize;
    private final int topicAliasMaximum;
    private final int sendTopicAliasMaximum;
    private final boolean requestProblemInformation;
    private final boolean sendProblemInformation;
    private final boolean requestResponseInformation;

    public MqttConnectRestrictions(
            final int receiveMaximum, final int sendMaximum, final int maximumPacketSize,
            final int sendMaximumPacketSize, final int topicAliasMaximum, final int sendTopicAliasMaximum,
            final boolean requestProblemInformation, final boolean sendProblemInformation,
            final boolean requestResponseInformation) {

        this.receiveMaximum = receiveMaximum;
        this.sendMaximum = sendMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.sendMaximumPacketSize = sendMaximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.sendTopicAliasMaximum = sendTopicAliasMaximum;
        this.requestProblemInformation = requestProblemInformation;
        this.sendProblemInformation = sendProblemInformation;
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
    public boolean isSendProblemInformation() {
        return sendProblemInformation;
    }

    @Override
    public boolean isRequestResponseInformation() {
        return requestResponseInformation;
    }
}

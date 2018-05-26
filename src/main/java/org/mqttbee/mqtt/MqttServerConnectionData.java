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

package org.mqttbee.mqtt;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ServerConnectionData;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.MqttTopicAliasMapping;

/**
 * @author Silvio Giebl
 */
public class MqttServerConnectionData implements Mqtt5ServerConnectionData, Mqtt3ServerConnectionData {

    public static int getMaximumPacketSize(@NotNull final Channel channel) {
        final MqttServerConnectionData serverConnectionData = MqttClientData.from(channel).getRawServerConnectionData();
        return (serverConnectionData == null) ? MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT :
                serverConnectionData.getMaximumPacketSize();
    }

    @Nullable
    public static MqttTopicAliasMapping getTopicAliasMapping(@NotNull final Channel channel) {
        final MqttServerConnectionData serverConnectionData = MqttClientData.from(channel).getRawServerConnectionData();
        return (serverConnectionData == null) ? null : serverConnectionData.getTopicAliasMapping();
    }

    private final int receiveMaximum;
    private final MqttTopicAliasMapping topicAliasMapping;
    private final int maximumPacketSize;
    private final MqttQoS maximumQoS;
    private final boolean isRetainAvailable;
    private final boolean isWildcardSubscriptionAvailable;
    private final boolean isSubscriptionIdentifierAvailable;
    private final boolean isSharedSubscriptionAvailable;

    public MqttServerConnectionData(
            final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize,
            final MqttQoS maximumQoS, final boolean isRetainAvailable, final boolean isWildcardSubscriptionAvailable,
            final boolean isSubscriptionIdentifierAvailable, final boolean isSharedSubscriptionAvailable) {
        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMapping = topicAliasMaximum == 0 ? null : new MqttTopicAliasMapping(topicAliasMaximum);
        this.maximumQoS = maximumQoS;
        this.isRetainAvailable = isRetainAvailable;
        this.isWildcardSubscriptionAvailable = isWildcardSubscriptionAvailable;
        this.isSubscriptionIdentifierAvailable = isSubscriptionIdentifierAvailable;
        this.isSharedSubscriptionAvailable = isSharedSubscriptionAvailable;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return (topicAliasMapping == null) ? 0 : topicAliasMapping.getTopicAliasMaximum();
    }

    @Nullable
    public MqttTopicAliasMapping getTopicAliasMapping() {
        return topicAliasMapping;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @NotNull
    @Override
    public MqttQoS getMaximumQoS() {
        return maximumQoS;
    }

    @Override
    public boolean isRetainAvailable() {
        return isRetainAvailable;
    }

    @Override
    public boolean isWildcardSubscriptionAvailable() {
        return isWildcardSubscriptionAvailable;
    }

    @Override
    public boolean isSubscriptionIdentifierAvailable() {
        return isSubscriptionIdentifierAvailable;
    }

    @Override
    public boolean isSharedSubscriptionAvailable() {
        return isSharedSubscriptionAvailable;
    }

}

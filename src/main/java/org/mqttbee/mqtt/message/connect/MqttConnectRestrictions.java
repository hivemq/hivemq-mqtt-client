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

package org.mqttbee.mqtt.message.connect;

import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;

/** @author Silvio Giebl */
@Immutable
public class MqttConnectRestrictions implements Mqtt5ConnectRestrictions {

    @NotNull
    public static final MqttConnectRestrictions DEFAULT =
            new MqttConnectRestrictions(
                    DEFAULT_RECEIVE_MAXIMUM,
                    DEFAULT_TOPIC_ALIAS_MAXIMUM,
                    DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);

    private final int receiveMaximum;
    private final int topicAliasMaximum;
    private final int maximumPacketSize;

    public MqttConnectRestrictions(
            final int receiveMaximum, final int topicAliasMaximum, final int maximumPacketSize) {

        this.receiveMaximum = receiveMaximum;
        this.topicAliasMaximum = topicAliasMaximum;
        this.maximumPacketSize = maximumPacketSize;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }
}

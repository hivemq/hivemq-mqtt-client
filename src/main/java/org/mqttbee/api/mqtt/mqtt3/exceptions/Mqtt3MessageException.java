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

package org.mqttbee.api.mqtt.mqtt3.exceptions;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import org.mqttbee.mqtt.message.ping.MqttPingReq;
import org.mqttbee.mqtt.message.ping.MqttPingResp;
import org.mqttbee.mqtt.message.ping.mqtt3.Mqtt3PingReqView;
import org.mqttbee.mqtt.message.ping.mqtt3.Mqtt3PingRespView;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.puback.mqtt3.Mqtt3PubAckView;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubcomp.mqtt3.Mqtt3PubCompView;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrec.mqtt3.Mqtt3PubRecView;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.message.publish.pubrel.mqtt3.Mqtt3PubRelView;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
public class Mqtt3MessageException extends Exception {

    @NotNull
    private static Mqtt3Message viewOf(@NotNull final Mqtt5Message mqtt5Message) {
        if (mqtt5Message instanceof MqttConnect) {
            return new Mqtt3ConnectView((MqttConnect) mqtt5Message);
        } else if (mqtt5Message instanceof MqttConnAck) {
            return new Mqtt3ConnAckView((MqttConnAck) mqtt5Message);
        } else if (mqtt5Message instanceof MqttPublish) {
            return new Mqtt3PublishView((MqttPublish) mqtt5Message);
        } else if (mqtt5Message instanceof MqttPubAck) {
            return Mqtt3PubAckView.INSTANCE;
        } else if (mqtt5Message instanceof MqttPubRec) {
            return Mqtt3PubRecView.INSTANCE;
        } else if (mqtt5Message instanceof MqttPubRel) {
            return Mqtt3PubRelView.INSTANCE;
        } else if (mqtt5Message instanceof MqttPubComp) {
            return Mqtt3PubCompView.INSTANCE;
        } else if (mqtt5Message instanceof MqttSubscribe) {
            return new Mqtt3SubscribeView((MqttSubscribe) mqtt5Message);
        } else if (mqtt5Message instanceof MqttSubAck) {
            return new Mqtt3SubAckView((MqttSubAck) mqtt5Message);
        } else if (mqtt5Message instanceof MqttUnsubscribe) {
            return new Mqtt3UnsubscribeView((MqttUnsubscribe) mqtt5Message);
        } else if (mqtt5Message instanceof MqttUnsubAck) {
            return Mqtt3UnsubAckView.INSTANCE;
        } else if (mqtt5Message instanceof MqttPingReq) {
            return Mqtt3PingReqView.INSTANCE;
        } else if (mqtt5Message instanceof MqttPingResp) {
            return Mqtt3PingRespView.INSTANCE;
        } else if (mqtt5Message instanceof MqttDisconnect) {
            return Mqtt3DisconnectView.INSTANCE;
        }
        throw new IllegalStateException();
    }

    private final Mqtt3Message mqtt3Message;

    public Mqtt3MessageException(@NotNull final Mqtt5MessageException mqtt5MessageException) {
        super(mqtt5MessageException.getMessage(), mqtt5MessageException.getCause());
        this.mqtt3Message = viewOf(mqtt5MessageException.getMqttMessage());
    }

    @NotNull
    public Mqtt3Message getMqttMessage() {
        return mqtt3Message;
    }

}

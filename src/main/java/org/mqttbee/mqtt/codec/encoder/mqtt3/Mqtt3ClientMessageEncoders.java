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

package org.mqttbee.mqtt.codec.encoder.mqtt3;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoders;
import org.mqttbee.mqtt.codec.encoder.MqttPingReqEncoder;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ClientMessageEncoders implements MqttMessageEncoders {

    private final MqttMessageEncoder[] encoders;

    @Inject
    Mqtt3ClientMessageEncoders(
            final Mqtt3ConnectEncoder connAckEncoder, final Mqtt3PublishEncoder publishEncoder,
            final Mqtt3PubAckEncoder pubAckEncoder, final Mqtt3PubRecEncoder pubRecEncoder,
            final Mqtt3PubRelEncoder pubRelEncoder, final Mqtt3PubCompEncoder pubCompEncoder,
            final Mqtt3SubscribeEncoder subAckEncoder, final Mqtt3UnsubscribeEncoder unsubAckEncoder,
            final MqttPingReqEncoder pingRespEncoder, final Mqtt3DisconnectEncoder disconnectEncoder) {

        encoders = new MqttMessageEncoder[Mqtt3MessageType.values().length];
        encoders[Mqtt3MessageType.CONNACK.getCode()] = connAckEncoder;
        encoders[Mqtt3MessageType.PUBLISH.getCode()] = publishEncoder;
        encoders[Mqtt3MessageType.PUBACK.getCode()] = pubAckEncoder;
        encoders[Mqtt3MessageType.PUBREC.getCode()] = pubRecEncoder;
        encoders[Mqtt3MessageType.PUBREL.getCode()] = pubRelEncoder;
        encoders[Mqtt3MessageType.PUBCOMP.getCode()] = pubCompEncoder;
        encoders[Mqtt3MessageType.SUBACK.getCode()] = subAckEncoder;
        encoders[Mqtt3MessageType.UNSUBACK.getCode()] = unsubAckEncoder;
        encoders[Mqtt3MessageType.PINGRESP.getCode()] = pingRespEncoder;
        encoders[Mqtt3MessageType.DISCONNECT.getCode()] = disconnectEncoder;
    }

    @Nullable
    @Override
    public MqttMessageEncoder get(final int code) {
        if (code < 0 || code >= encoders.length) {
            return null;
        }
        return encoders[code];
    }

}

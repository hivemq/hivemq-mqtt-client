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

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoders;
import org.mqttbee.mqtt.codec.encoder.MqttPingReqEncoder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of encoders for MQTT 5 messages a client can send.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ClientMessageEncoders implements MqttMessageEncoders {

    private final MqttMessageEncoder[] encoders;

    @Inject
    Mqtt5ClientMessageEncoders(
            final Mqtt5ConnectEncoder connectEncoder, final Mqtt5PublishEncoder publishEncoder,
            final Mqtt5PubAckEncoder pubAckEncoder, final Mqtt5PubRecEncoder pubRecEncoder,
            final Mqtt5PubRelEncoder pubRelEncoder, final Mqtt5PubCompEncoder pubCompEncoder,
            final Mqtt5SubscribeEncoder subscribeEncoder, final Mqtt5UnsubscribeEncoder unsubscribeEncoder,
            final MqttPingReqEncoder pingReqEncoder, final Mqtt5DisconnectEncoder disconnectEncoder,
            final Mqtt5AuthEncoder authEncoder) {

        encoders = new MqttMessageEncoder[Mqtt5MessageType.values().length];
        encoders[Mqtt5MessageType.CONNECT.getCode()] = connectEncoder;
        encoders[Mqtt5MessageType.PUBLISH.getCode()] = publishEncoder;
        encoders[Mqtt5MessageType.PUBACK.getCode()] = pubAckEncoder;
        encoders[Mqtt5MessageType.PUBREC.getCode()] = pubRecEncoder;
        encoders[Mqtt5MessageType.PUBREL.getCode()] = pubRelEncoder;
        encoders[Mqtt5MessageType.PUBCOMP.getCode()] = pubCompEncoder;
        encoders[Mqtt5MessageType.SUBSCRIBE.getCode()] = subscribeEncoder;
        encoders[Mqtt5MessageType.UNSUBSCRIBE.getCode()] = unsubscribeEncoder;
        encoders[Mqtt5MessageType.PINGREQ.getCode()] = pingReqEncoder;
        encoders[Mqtt5MessageType.DISCONNECT.getCode()] = disconnectEncoder;
        encoders[Mqtt5MessageType.AUTH.getCode()] = authEncoder;
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

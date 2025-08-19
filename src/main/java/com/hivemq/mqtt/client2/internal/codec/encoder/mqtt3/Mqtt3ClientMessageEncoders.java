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

package com.hivemq.mqtt.client2.internal.codec.encoder.mqtt3;

import com.hivemq.mqtt.client2.internal.codec.encoder.MqttMessageEncoders;
import com.hivemq.mqtt.client2.internal.codec.encoder.MqttPingReqEncoder;
import com.hivemq.mqtt.client2.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.NotNull;

/**
 * Collection of encoders for MQTT 3 messages a client can send.
 *
 * @author Silvio Giebl
 */
public class Mqtt3ClientMessageEncoders extends MqttMessageEncoders {

    public static final @NotNull Mqtt3ClientMessageEncoders INSTANCE = new Mqtt3ClientMessageEncoders();

    private Mqtt3ClientMessageEncoders() {
        encoders[Mqtt3MessageType.CONNECT.getCode()] = Mqtt3ConnectEncoder.INSTANCE;
        encoders[Mqtt3MessageType.PUBLISH.getCode()] = Mqtt3PublishEncoder.INSTANCE;
        encoders[Mqtt3MessageType.PUBACK.getCode()] = Mqtt3PubAckEncoder.INSTANCE;
        encoders[Mqtt3MessageType.PUBREC.getCode()] = Mqtt3PubRecEncoder.INSTANCE;
        encoders[Mqtt3MessageType.PUBREL.getCode()] = Mqtt3PubRelEncoder.INSTANCE;
        encoders[Mqtt3MessageType.PUBCOMP.getCode()] = Mqtt3PubCompEncoder.INSTANCE;
        encoders[Mqtt3MessageType.SUBSCRIBE.getCode()] = Mqtt3SubscribeEncoder.INSTANCE;
        encoders[Mqtt3MessageType.UNSUBSCRIBE.getCode()] = Mqtt3UnsubscribeEncoder.INSTANCE;
        encoders[Mqtt3MessageType.PINGREQ.getCode()] = MqttPingReqEncoder.INSTANCE;
        encoders[Mqtt3MessageType.DISCONNECT.getCode()] = Mqtt3DisconnectEncoder.INSTANCE;
    }
}

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

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3;

import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoders;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttPingRespDecoder;
import com.hivemq.mqtt.client2.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.NotNull;

/**
 * Collection of decoders for MQTT 3 messages a client can receive.
 *
 * @author Silvio Giebl
 */
public class Mqtt3ClientMessageDecoders extends MqttMessageDecoders {

    public static final @NotNull Mqtt3ClientMessageDecoders INSTANCE = new Mqtt3ClientMessageDecoders();

    private Mqtt3ClientMessageDecoders() {
        decoders[Mqtt3MessageType.CONNACK.getCode()] = Mqtt3ConnAckDecoder.INSTANCE;
        decoders[Mqtt3MessageType.PUBLISH.getCode()] = Mqtt3PublishDecoder.INSTANCE;
        decoders[Mqtt3MessageType.PUBACK.getCode()] = Mqtt3PubAckDecoder.INSTANCE;
        decoders[Mqtt3MessageType.PUBREC.getCode()] = Mqtt3PubRecDecoder.INSTANCE;
        decoders[Mqtt3MessageType.PUBREL.getCode()] = Mqtt3PubRelDecoder.INSTANCE;
        decoders[Mqtt3MessageType.PUBCOMP.getCode()] = Mqtt3PubCompDecoder.INSTANCE;
        decoders[Mqtt3MessageType.SUBACK.getCode()] = Mqtt3SubAckDecoder.INSTANCE;
        decoders[Mqtt3MessageType.UNSUBACK.getCode()] = Mqtt3UnsubAckDecoder.INSTANCE;
        decoders[Mqtt3MessageType.PINGRESP.getCode()] = MqttPingRespDecoder.INSTANCE;
    }
}

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

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5;

import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoders;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttPingRespDecoder;
import com.hivemq.mqtt.client2.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;

/**
 * Collection of decoders for MQTT 5 messages a client can receive.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ClientMessageDecoders extends MqttMessageDecoders {

    public static final @NotNull Mqtt5ClientMessageDecoders INSTANCE = new Mqtt5ClientMessageDecoders();

    private Mqtt5ClientMessageDecoders() {
        decoders[Mqtt5MessageType.CONNACK.getCode()] = Mqtt5ConnAckDecoder.INSTANCE;
        decoders[Mqtt5MessageType.PUBLISH.getCode()] = Mqtt5PublishDecoder.INSTANCE;
        decoders[Mqtt5MessageType.PUBACK.getCode()] = Mqtt5PubAckDecoder.INSTANCE;
        decoders[Mqtt5MessageType.PUBREC.getCode()] = Mqtt5PubRecDecoder.INSTANCE;
        decoders[Mqtt5MessageType.PUBREL.getCode()] = Mqtt5PubRelDecoder.INSTANCE;
        decoders[Mqtt5MessageType.PUBCOMP.getCode()] = Mqtt5PubCompDecoder.INSTANCE;
        decoders[Mqtt5MessageType.SUBACK.getCode()] = Mqtt5SubAckDecoder.INSTANCE;
        decoders[Mqtt5MessageType.UNSUBACK.getCode()] = Mqtt5UnsubAckDecoder.INSTANCE;
        decoders[Mqtt5MessageType.PINGRESP.getCode()] = MqttPingRespDecoder.INSTANCE;
        decoders[Mqtt5MessageType.DISCONNECT.getCode()] = Mqtt5DisconnectDecoder.INSTANCE;
        decoders[Mqtt5MessageType.AUTH.getCode()] = Mqtt5AuthDecoder.INSTANCE;
    }
}

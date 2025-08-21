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

import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoder;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttPingRespDecoder;
import com.hivemq.mqtt.client2.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Collection of decoders for MQTT 5 messages a client can receive.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ClientMessageDecoders {

    public static final @Nullable MqttMessageDecoder @NotNull [] INSTANCE = new MqttMessageDecoder[16];

    static {
        INSTANCE[Mqtt5MessageType.CONNACK.getCode()] = Mqtt5ConnAckDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBLISH.getCode()] = Mqtt5PublishDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBACK.getCode()] = Mqtt5PubAckDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBREC.getCode()] = Mqtt5PubRecDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBREL.getCode()] = Mqtt5PubRelDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBCOMP.getCode()] = Mqtt5PubCompDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.SUBACK.getCode()] = Mqtt5SubAckDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.UNSUBACK.getCode()] = Mqtt5UnsubAckDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PINGRESP.getCode()] = MqttPingRespDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.DISCONNECT.getCode()] = Mqtt5DisconnectDecoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.AUTH.getCode()] = Mqtt5AuthDecoder.INSTANCE;
    }
}

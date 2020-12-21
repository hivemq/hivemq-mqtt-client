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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client2.internal.mqtt.codec.decoder.MqttPingRespDecoder;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of decoders for MQTT 5 messages a client can receive.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ClientMessageDecoders extends MqttMessageDecoders {

    @Inject
    Mqtt5ClientMessageDecoders(
            final @NotNull Mqtt5ConnAckDecoder connAckDecoder,
            final @NotNull Mqtt5PublishDecoder publishDecoder,
            final @NotNull Mqtt5PubAckDecoder pubAckDecoder,
            final @NotNull Mqtt5PubRecDecoder pubRecDecoder,
            final @NotNull Mqtt5PubRelDecoder pubRelDecoder,
            final @NotNull Mqtt5PubCompDecoder pubCompDecoder,
            final @NotNull Mqtt5SubAckDecoder subAckDecoder,
            final @NotNull Mqtt5UnsubAckDecoder unsubAckDecoder,
            final @NotNull MqttPingRespDecoder pingRespDecoder,
            final @NotNull Mqtt5DisconnectDecoder disconnectDecoder,
            final @NotNull Mqtt5AuthDecoder authDecoder) {

        decoders[Mqtt5MessageType.CONNACK.getCode()] = connAckDecoder;
        decoders[Mqtt5MessageType.PUBLISH.getCode()] = publishDecoder;
        decoders[Mqtt5MessageType.PUBACK.getCode()] = pubAckDecoder;
        decoders[Mqtt5MessageType.PUBREC.getCode()] = pubRecDecoder;
        decoders[Mqtt5MessageType.PUBREL.getCode()] = pubRelDecoder;
        decoders[Mqtt5MessageType.PUBCOMP.getCode()] = pubCompDecoder;
        decoders[Mqtt5MessageType.SUBACK.getCode()] = subAckDecoder;
        decoders[Mqtt5MessageType.UNSUBACK.getCode()] = unsubAckDecoder;
        decoders[Mqtt5MessageType.PINGRESP.getCode()] = pingRespDecoder;
        decoders[Mqtt5MessageType.DISCONNECT.getCode()] = disconnectDecoder;
        decoders[Mqtt5MessageType.AUTH.getCode()] = authDecoder;
    }
}

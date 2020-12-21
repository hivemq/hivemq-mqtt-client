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

package com.hivemq.client2.internal.mqtt.codec.decoder.mqtt3;

import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client2.internal.mqtt.codec.decoder.MqttPingRespDecoder;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of decoders for MQTT 3 messages a client can receive.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ClientMessageDecoders extends MqttMessageDecoders {

    @Inject
    Mqtt3ClientMessageDecoders(
            final @NotNull Mqtt3ConnAckDecoder connAckDecoder,
            final @NotNull Mqtt3PublishDecoder publishDecoder,
            final @NotNull Mqtt3PubAckDecoder pubAckDecoder,
            final @NotNull Mqtt3PubRecDecoder pubRecDecoder,
            final @NotNull Mqtt3PubRelDecoder pubRelDecoder,
            final @NotNull Mqtt3PubCompDecoder pubCompDecoder,
            final @NotNull Mqtt3SubAckDecoder subAckDecoder,
            final @NotNull Mqtt3UnsubAckDecoder unsubAckDecoder,
            final @NotNull MqttPingRespDecoder pingRespDecoder) {

        decoders[Mqtt3MessageType.CONNACK.getCode()] = connAckDecoder;
        decoders[Mqtt3MessageType.PUBLISH.getCode()] = publishDecoder;
        decoders[Mqtt3MessageType.PUBACK.getCode()] = pubAckDecoder;
        decoders[Mqtt3MessageType.PUBREC.getCode()] = pubRecDecoder;
        decoders[Mqtt3MessageType.PUBREL.getCode()] = pubRelDecoder;
        decoders[Mqtt3MessageType.PUBCOMP.getCode()] = pubCompDecoder;
        decoders[Mqtt3MessageType.SUBACK.getCode()] = subAckDecoder;
        decoders[Mqtt3MessageType.UNSUBACK.getCode()] = unsubAckDecoder;
        decoders[Mqtt3MessageType.PINGRESP.getCode()] = pingRespDecoder;
    }
}

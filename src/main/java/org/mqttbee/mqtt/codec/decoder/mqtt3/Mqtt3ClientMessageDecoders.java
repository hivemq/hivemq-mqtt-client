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

package org.mqttbee.mqtt.codec.decoder.mqtt3;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoders;
import org.mqttbee.mqtt.codec.decoder.MqttPingRespDecoder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of decoders for MQTT 3 messages a client can receive.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ClientMessageDecoders implements MqttMessageDecoders {

    private final MqttMessageDecoder[] decoders;

    @Inject
    Mqtt3ClientMessageDecoders(
            final Mqtt3ConnAckDecoder connAckDecoder, final Mqtt3PublishDecoder publishDecoder,
            final Mqtt3PubAckDecoder pubAckDecoder, final Mqtt3PubRecDecoder pubRecDecoder,
            final Mqtt3PubRelDecoder pubRelDecoder, final Mqtt3PubCompDecoder pubCompDecoder,
            final Mqtt3SubAckDecoder subAckDecoder, final Mqtt3UnsubAckDecoder unsubAckDecoder,
            final MqttPingRespDecoder pingRespDecoder) {

        decoders = new MqttMessageDecoder[Mqtt3MessageType.values().length];
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

    @Nullable
    @Override
    public MqttMessageDecoder get(final int code) {
        if (code < 0 || code >= decoders.length) {
            return null;
        }
        return decoders[code];
    }

}

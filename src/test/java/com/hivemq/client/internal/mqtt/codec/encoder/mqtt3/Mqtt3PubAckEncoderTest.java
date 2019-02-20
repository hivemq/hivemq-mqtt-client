/*
 * Copyright 2018 The HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client.internal.mqtt.message.publish.puback.MqttPubAck;
import com.hivemq.client.internal.mqtt.message.publish.puback.mqtt3.Mqtt3PubAckView;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;

class Mqtt3PubAckEncoderTest extends AbstractMqtt3EncoderTest {

    Mqtt3PubAckEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt3MessageType.PUBACK.getCode()] = new Mqtt3PubAckEncoder();
        }}, true);
    }

    @Test
    void matchesPaho() throws MqttException {
        final int id = 42;
        final MqttPubAck beeMessage = Mqtt3PubAckView.delegate(id);
        final org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck pahoMessage =
                new org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck(id);

        encode(beeMessage, bytesOf(pahoMessage));
    }
}
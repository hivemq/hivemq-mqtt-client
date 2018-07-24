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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.puback.mqtt3.Mqtt3PubAckView;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PubAckEncoderTest extends AbstractMqtt3EncoderTest {

    Mqtt3PubAckEncoderTest() {
        super(code -> new Mqtt3PubAckEncoder(), true);
    }

    @Test
    public void matchesPaho() throws MqttException {
        final int id = 42;
        org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck pahoMessage
                = new org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck(id);
        final MqttPubAck beeMessage = Mqtt3PubAckView.delegate(id);
        assertArrayEquals(bytesOf(pahoMessage), bytesOf(beeMessage));
    }
}
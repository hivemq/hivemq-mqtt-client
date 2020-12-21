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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPubRel;
import com.hivemq.client2.internal.mqtt.message.publish.mqtt3.Mqtt3PubRelView;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.junit.jupiter.api.Test;

class Mqtt3PubRelEncoderTest extends AbstractMqtt3EncoderTest {

    Mqtt3PubRelEncoderTest() {
        super(new MqttMessageEncoders() {{
            encoders[Mqtt3MessageType.PUBREL.getCode()] = new Mqtt3PubRelEncoder();
        }}, true);
    }

    @Test
    void matchesPaho() throws MqttException {
        final int id = 42;
        final MqttPubRel beePubRel = Mqtt3PubRelView.delegate(id);
        final MqttPublish pahoPublish = new MqttPublish("some/topic/string", new MqttMessage(new byte[]{42}));
        pahoPublish.setMessageId(id);
        final org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel pahoPubRel =
                new org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel(new MqttPubRec(pahoPublish));

        encode(beePubRel, bytesOf(pahoPubRel));
    }
}
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

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.message.publish.pubrel.mqtt3.Mqtt3PubRelView;

import static org.junit.Assert.assertArrayEquals;

class Mqtt3PubRelEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt3PubRelEncoderTest() {
        super(code -> new Mqtt3PubRelEncoder(), true);
    }

    @Test
    void encode() {
        final int id = 1;
        final byte msb = (byte) (id >>> 8);
        final byte lsb = (byte) id;
        final byte[] expected = {0x62, 0x02, msb, lsb};
        final MqttPubRel pubRel = Mqtt3PubRelView.delegate(id);
        encode(expected, pubRel);
    }

    @Test
    void encodedRemainingLength() {
    }

    private void encode(final byte[] expected, final MqttPubRel pubRel) {
        channel.writeOutbound(pubRel);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        assertArrayEquals(expected, actual);
    }

}
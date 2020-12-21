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

import com.hivemq.client2.internal.mqtt.codec.decoder.AbstractMqttDecoderTest;
import com.hivemq.client2.internal.mqtt.codec.decoder.MqttPingRespDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class Mqtt3ClientMessageDecodersTest {

    @Test
    void test() {
        final Mqtt3ConnAckDecoder connAckDecoder = new Mqtt3ConnAckDecoder();
        final Mqtt3PublishDecoder publishDecoder = new Mqtt3PublishDecoder();
        final Mqtt3PubAckDecoder pubAckDecoder = new Mqtt3PubAckDecoder();
        final Mqtt3PubRecDecoder pubRecDecoder = new Mqtt3PubRecDecoder();
        final Mqtt3PubRelDecoder pubRelDecoder = new Mqtt3PubRelDecoder();
        final Mqtt3PubCompDecoder pubCompDecoder = new Mqtt3PubCompDecoder();
        final Mqtt3SubAckDecoder subAckDecoder = new Mqtt3SubAckDecoder();
        final Mqtt3UnsubAckDecoder unsubAckDecoder = new Mqtt3UnsubAckDecoder();
        final MqttPingRespDecoder pingRespDecoder = AbstractMqttDecoderTest.createPingRespDecoder();

        final Mqtt3ClientMessageDecoders clientMessageDecoders =
                new Mqtt3ClientMessageDecoders(connAckDecoder, publishDecoder, pubAckDecoder, pubRecDecoder,
                        pubRelDecoder, pubCompDecoder, subAckDecoder, unsubAckDecoder, pingRespDecoder);

        assertNull(clientMessageDecoders.get(-1));
        assertNull(clientMessageDecoders.get(0));
        assertNull(clientMessageDecoders.get(1));
        assertSame(connAckDecoder, clientMessageDecoders.get(2));
        assertSame(publishDecoder, clientMessageDecoders.get(3));
        assertSame(pubAckDecoder, clientMessageDecoders.get(4));
        assertSame(pubRecDecoder, clientMessageDecoders.get(5));
        assertSame(pubRelDecoder, clientMessageDecoders.get(6));
        assertSame(pubCompDecoder, clientMessageDecoders.get(7));
        assertNull(clientMessageDecoders.get(8));
        assertSame(subAckDecoder, clientMessageDecoders.get(9));
        assertNull(clientMessageDecoders.get(10));
        assertSame(unsubAckDecoder, clientMessageDecoders.get(11));
        assertNull(clientMessageDecoders.get(12));
        assertSame(pingRespDecoder, clientMessageDecoders.get(13));
        assertNull(clientMessageDecoders.get(14));
        assertNull(clientMessageDecoders.get(15));
    }

}
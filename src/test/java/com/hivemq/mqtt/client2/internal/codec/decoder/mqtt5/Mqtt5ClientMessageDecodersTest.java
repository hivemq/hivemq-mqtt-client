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

import com.hivemq.mqtt.client2.internal.codec.decoder.AbstractMqttDecoderTest;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttPingRespDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Silvio Giebl
 */
class Mqtt5ClientMessageDecodersTest {

    @Test
    void test() {
        final Mqtt5ConnAckDecoder connAckDecoder = Mqtt5ConnAckDecoder.INSTANCE;
        final Mqtt5PublishDecoder publishDecoder = Mqtt5PublishDecoder.INSTANCE;
        final Mqtt5PubAckDecoder pubAckDecoder = Mqtt5PubAckDecoder.INSTANCE;
        final Mqtt5PubRecDecoder pubRecDecoder = Mqtt5PubRecDecoder.INSTANCE;
        final Mqtt5PubRelDecoder pubRelDecoder = Mqtt5PubRelDecoder.INSTANCE;
        final Mqtt5PubCompDecoder pubCompDecoder = Mqtt5PubCompDecoder.INSTANCE;
        final Mqtt5SubAckDecoder subAckDecoder = Mqtt5SubAckDecoder.INSTANCE;
        final Mqtt5UnsubAckDecoder unsubAckDecoder = Mqtt5UnsubAckDecoder.INSTANCE;
        final MqttPingRespDecoder pingRespDecoder = AbstractMqttDecoderTest.createPingRespDecoder();
        final Mqtt5DisconnectDecoder disconnectDecoder = Mqtt5DisconnectDecoder.INSTANCE;
        final Mqtt5AuthDecoder authDecoder = Mqtt5AuthDecoder.INSTANCE;

        final Mqtt5ClientMessageDecoders clientMessageDecoders = Mqtt5ClientMessageDecoders.INSTANCE;

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
        assertSame(disconnectDecoder, clientMessageDecoders.get(14));
        assertSame(authDecoder, clientMessageDecoders.get(15));
        assertNull(clientMessageDecoders.get(16));
    }

}
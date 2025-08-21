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
        assertNull(Mqtt5ClientMessageDecoders.INSTANCE[0]);
        assertNull(Mqtt5ClientMessageDecoders.INSTANCE[1]);
        assertSame(Mqtt5ConnAckDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[2]);
        assertSame(Mqtt5PublishDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[3]);
        assertSame(Mqtt5PubAckDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[4]);
        assertSame(Mqtt5PubRecDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[5]);
        assertSame(Mqtt5PubRelDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[6]);
        assertSame(Mqtt5PubCompDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[7]);
        assertNull(Mqtt5ClientMessageDecoders.INSTANCE[8]);
        assertSame(Mqtt5SubAckDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[9]);
        assertNull(Mqtt5ClientMessageDecoders.INSTANCE[10]);
        assertSame(Mqtt5UnsubAckDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[11]);
        assertNull(Mqtt5ClientMessageDecoders.INSTANCE[12]);
        assertSame(MqttPingRespDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[13]);
        assertSame(Mqtt5DisconnectDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[14]);
        assertSame(Mqtt5AuthDecoder.INSTANCE, Mqtt5ClientMessageDecoders.INSTANCE[15]);
    }
}

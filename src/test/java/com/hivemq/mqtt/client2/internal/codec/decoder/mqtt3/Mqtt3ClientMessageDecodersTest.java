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

package com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3;

import com.hivemq.mqtt.client2.internal.codec.decoder.MqttPingRespDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class Mqtt3ClientMessageDecodersTest {

    @Test
    void test() {
        assertNull(Mqtt3ClientMessageDecoders.INSTANCE[0]);
        assertNull(Mqtt3ClientMessageDecoders.INSTANCE[1]);
        assertSame(Mqtt3ConnAckDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[2]);
        assertSame(Mqtt3PublishDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[3]);
        assertSame(Mqtt3PubAckDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[4]);
        assertSame(Mqtt3PubRecDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[5]);
        assertSame(Mqtt3PubRelDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[6]);
        assertSame(Mqtt3PubCompDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[7]);
        assertNull(Mqtt3ClientMessageDecoders.INSTANCE[8]);
        assertSame(Mqtt3SubAckDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[9]);
        assertNull(Mqtt3ClientMessageDecoders.INSTANCE[10]);
        assertSame(Mqtt3UnsubAckDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[11]);
        assertNull(Mqtt3ClientMessageDecoders.INSTANCE[12]);
        assertSame(MqttPingRespDecoder.INSTANCE, Mqtt3ClientMessageDecoders.INSTANCE[13]);
    }
}

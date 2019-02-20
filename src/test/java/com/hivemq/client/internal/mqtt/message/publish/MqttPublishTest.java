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

package com.hivemq.client.internal.mqtt.message.publish;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Christian Hoff
 */
public class MqttPublishTest {

    @Test
    public void getPayloadAsBytes() {
        final byte[] payload = {1, 2, 3, 4, 5};
        final MqttPublish publish = new MqttPublishBuilder.Default().topic("topic").payload(payload).build();
        assertArrayEquals(payload, publish.getPayloadAsBytes());
    }

    @Test
    public void getPayloadAsBytes_payloadIsNull() {
        final MqttPublish publish = new MqttPublishBuilder.Default().topic("topic").payload((byte[]) null).build();
        assertNotNull(publish.getPayloadAsBytes());
        assertEquals(0, publish.getPayloadAsBytes().length);
    }
}

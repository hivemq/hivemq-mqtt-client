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

package com.hivemq.client.internal.mqtt.message.publish.mqtt3;

import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Christian Hoff
 */
public class Mqtt3PublishViewTest {

    @Test
    public void getPayloadAsBytes_delegates() {
        final byte[] payload = {1, 2, 3, 4, 5};
        final MqttPublish spyPublish = spy(new MqttPublishBuilder.Default().topic("topic").payload(payload).build());
        final Mqtt3PublishView publishView = Mqtt3PublishView.of(spyPublish);
        assertArrayEquals(payload, publishView.getPayloadAsBytes());
        verify(spyPublish).getPayloadAsBytes();
    }

    @Test
    public void getPayloadAsBytes_null_delegates() {
        final MqttPublish spyPublish =
                spy(new MqttPublishBuilder.Default().topic("topic").payload((byte[]) null).build());
        final Mqtt3PublishView publishView = Mqtt3PublishView.of(spyPublish);
        assertNotNull(publishView.getPayloadAsBytes());
        assertEquals(0, publishView.getPayloadAsBytes().length);
        verify(spyPublish, times(2)).getPayloadAsBytes();
    }
}

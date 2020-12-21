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

package com.hivemq.client2.internal.mqtt.message.publish.mqtt3;

import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublishBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Christian Hoff
 * @author Silvio Giebl
 */
class Mqtt3PublishViewTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(Mqtt3PublishView.class).suppress(Warning.STRICT_INHERITANCE).verify();
    }

    @Test
    void getPayloadAsBytes_delegates() {
        final byte[] payload = {1, 2, 3, 4, 5};
        final MqttPublish spyPublish = spy(new MqttPublishBuilder.Default().topic("topic").payload(payload).build());
        final Mqtt3PublishView publishView = Mqtt3PublishView.of(spyPublish);
        assertArrayEquals(payload, publishView.getPayloadAsBytes());
        verify(spyPublish).getPayloadAsBytes();
    }

    @Test
    void getPayloadAsBytes_null_delegates() {
        final MqttPublish spyPublish =
                spy(new MqttPublishBuilder.Default().topic("topic").payload((byte[]) null).build());
        final Mqtt3PublishView publishView = Mqtt3PublishView.of(spyPublish);
        assertNotNull(publishView.getPayloadAsBytes());
        assertEquals(0, publishView.getPayloadAsBytes().length);
        verify(spyPublish, times(2)).getPayloadAsBytes();
    }
}

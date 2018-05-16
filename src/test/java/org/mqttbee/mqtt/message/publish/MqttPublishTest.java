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

package org.mqttbee.mqtt.message.publish;

import org.junit.Test;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.TopicAliasUsage.HAS_NOT;
import static org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl.NO_USER_PROPERTIES;

/**
 * @author Christian Hoff
 */
public class MqttPublishTest {

    public static MqttPublish createPublishFromPayload(final ByteBuffer payload) {
        return new MqttPublish(requireNonNull(MqttTopicImpl.from("topic")), payload, MqttQoS.AT_MOST_ONCE, false,
            MqttPublish.MESSAGE_EXPIRY_INTERVAL_INFINITY, Mqtt5PayloadFormatIndicator.UNSPECIFIED, null, null, null,
                HAS_NOT, NO_USER_PROPERTIES);
    }

    @Test
    public void getPayloadAsBytes() {
        byte[] expectedPayload = {1, 2, 3, 4, 5};
        final MqttPublish publish = createPublishFromPayload(ByteBuffer.wrap(expectedPayload));
        assertArrayEquals(expectedPayload, publish.getPayloadAsBytes());
    }

    @Test
    public void getPayloadAsBytes_payloadIsNull() {
        final MqttPublish publish = createPublishFromPayload(null);
        assertEquals(0, publish.getPayloadAsBytes().length);
    }

}

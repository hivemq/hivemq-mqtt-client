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

package com.hivemq.client2.internal.mqtt.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
class MqttPropertyTest {

    @Test
    public void test_all_properties() {
        assertEquals(0x01, MqttProperty.PAYLOAD_FORMAT_INDICATOR);
        assertEquals(0x02, MqttProperty.MESSAGE_EXPIRY_INTERVAL);
        assertEquals(0x03, MqttProperty.CONTENT_TYPE);
        assertEquals(0x08, MqttProperty.RESPONSE_TOPIC);
        assertEquals(0x09, MqttProperty.CORRELATION_DATA);
        assertEquals(0x0B, MqttProperty.SUBSCRIPTION_IDENTIFIER);
        assertEquals(0x11, MqttProperty.SESSION_EXPIRY_INTERVAL);
        assertEquals(0x12, MqttProperty.ASSIGNED_CLIENT_IDENTIFIER);
        assertEquals(0x13, MqttProperty.SERVER_KEEP_ALIVE);
        assertEquals(0x15, MqttProperty.AUTHENTICATION_METHOD);
        assertEquals(0x16, MqttProperty.AUTHENTICATION_DATA);
        assertEquals(0x17, MqttProperty.REQUEST_PROBLEM_INFORMATION);
        assertEquals(0x18, MqttProperty.WILL_DELAY_INTERVAL);
        assertEquals(0x19, MqttProperty.REQUEST_RESPONSE_INFORMATION);
        assertEquals(0x1A, MqttProperty.RESPONSE_INFORMATION);
        assertEquals(0x1C, MqttProperty.SERVER_REFERENCE);
        assertEquals(0x1F, MqttProperty.REASON_STRING);
        assertEquals(0x21, MqttProperty.RECEIVE_MAXIMUM);
        assertEquals(0x22, MqttProperty.TOPIC_ALIAS_MAXIMUM);
        assertEquals(0x23, MqttProperty.TOPIC_ALIAS);
        assertEquals(0x24, MqttProperty.MAXIMUM_QOS);
        assertEquals(0x25, MqttProperty.RETAIN_AVAILABLE);
        assertEquals(0x26, MqttProperty.USER_PROPERTY);
        assertEquals(0x27, MqttProperty.MAXIMUM_PACKET_SIZE);
        assertEquals(0x28, MqttProperty.WILDCARD_SUBSCRIPTION_AVAILABLE);
        assertEquals(0x29, MqttProperty.SUBSCRIPTION_IDENTIFIERS_AVAILABLE);
        assertEquals(0x2A, MqttProperty.SHARED_SUBSCRIPTION_AVAILABLE);
    }

}
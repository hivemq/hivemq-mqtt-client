/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.api.mqtt.datatypes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class MqttQoSTest {

    @Test
    public void test_getCode_atMostOnce() {
        assertEquals(0, MqttQoS.AT_MOST_ONCE.getCode());
    }

    @Test
    public void test_getCode_atLeastOnce() {
        assertEquals(1, MqttQoS.AT_LEAST_ONCE.getCode());
    }

    @Test
    public void test_getCode_exactlyOnce() {
        assertEquals(2, MqttQoS.EXACTLY_ONCE.getCode());
    }

    @Test
    public void test_fromCode_0() {
        assertSame(MqttQoS.AT_MOST_ONCE, MqttQoS.fromCode(0));
    }

    @Test
    public void test_fromCode_1() {
        assertSame(MqttQoS.AT_LEAST_ONCE, MqttQoS.fromCode(1));
    }

    @Test
    public void test_fromCode_2() {
        assertSame(MqttQoS.EXACTLY_ONCE, MqttQoS.fromCode(2));
    }

    @Test
    public void test_fromCode_3() {
        assertNull(MqttQoS.fromCode(3));
    }

    @Test
    public void test_fromCode_negative() {
        assertNull(MqttQoS.fromCode(-1));
    }

}
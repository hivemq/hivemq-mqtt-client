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

package com.hivemq.client2.mqtt.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttQosTest {

    @Test
    public void test_getCode_atMostOnce() {
        assertEquals(0, MqttQos.AT_MOST_ONCE.getCode());
    }

    @Test
    public void test_getCode_atLeastOnce() {
        assertEquals(1, MqttQos.AT_LEAST_ONCE.getCode());
    }

    @Test
    public void test_getCode_exactlyOnce() {
        assertEquals(2, MqttQos.EXACTLY_ONCE.getCode());
    }

    @Test
    public void test_fromCode_0() {
        assertSame(MqttQos.AT_MOST_ONCE, MqttQos.fromCode(0));
    }

    @Test
    public void test_fromCode_1() {
        assertSame(MqttQos.AT_LEAST_ONCE, MqttQos.fromCode(1));
    }

    @Test
    public void test_fromCode_2() {
        assertSame(MqttQos.EXACTLY_ONCE, MqttQos.fromCode(2));
    }

    @Test
    public void test_fromCode_3() {
        assertNull(MqttQos.fromCode(3));
    }

    @Test
    public void test_fromCode_negative() {
        assertNull(MqttQos.fromCode(-1));
    }

}
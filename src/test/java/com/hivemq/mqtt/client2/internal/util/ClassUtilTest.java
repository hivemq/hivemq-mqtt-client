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

package com.hivemq.mqtt.client2.internal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Silvio Giebl
 */
class ClassUtilTest {

    @Test
    void isAvailable_true() {
        assertTrue(ClassUtil.isAvailable("java.lang.String"));
        assertTrue(ClassUtil.isAvailable("com.hivemq.mqtt.client2.internal.util.ClassUtilTest"));
    }

    @Test
    void isAvailable_unknownClass() {
        assertFalse(ClassUtil.isAvailable("com.hivemq.mqtt.client2.UnknownClass"));
    }
}
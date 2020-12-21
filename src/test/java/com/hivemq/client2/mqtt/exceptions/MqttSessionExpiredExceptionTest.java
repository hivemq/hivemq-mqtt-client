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

package com.hivemq.client2.mqtt.exceptions;

import com.hivemq.client2.internal.util.AsyncRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttSessionExpiredExceptionTest {

    @Test
    void constructor_messageAndCause() {
        final MqttSessionExpiredException exception =
                new MqttSessionExpiredException("message", new RuntimeException("cause"));
        assertEquals("message", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("cause", exception.getCause().getMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void noStackTrace() {
        final MqttSessionExpiredException exception =
                new MqttSessionExpiredException("message", new RuntimeException("cause"));
        assertEquals(0, exception.getStackTrace().length);
        final MqttSessionExpiredException thrownException =
                assertThrows(MqttSessionExpiredException.class, () -> { throw exception; });
        assertEquals(0, thrownException.getStackTrace().length);
    }

    @Test
    void fillInStackTrace_newStackTrace() {
        final MqttSessionExpiredException exception =
                new MqttSessionExpiredException("message", new RuntimeException("cause"));
        assertEquals(0, exception.getStackTrace().length);
        final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
        assertTrue(filledException instanceof MqttSessionExpiredException);
        assertTrue(filledException.getStackTrace().length > 0);
        assertEquals("fillInStackTrace_newStackTrace", filledException.getStackTrace()[0].getMethodName());
    }
}
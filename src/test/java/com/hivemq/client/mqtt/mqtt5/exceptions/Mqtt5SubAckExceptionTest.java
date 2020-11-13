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

package com.hivemq.client.mqtt.mqtt5.exceptions;

import com.hivemq.client.internal.util.AsyncRuntimeException;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Silvio Giebl
 */
class Mqtt5SubAckExceptionTest {

    @Test
    void constructor() {
        final Mqtt5SubAck subAck = mock(Mqtt5SubAck.class);
        final Mqtt5SubAckException exception = new Mqtt5SubAckException(subAck, "message");
        assertEquals("message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(subAck, exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void noStackTrace() {
        final Mqtt5SubAck subAck = mock(Mqtt5SubAck.class);
        final Mqtt5SubAckException exception = new Mqtt5SubAckException(subAck, "message");
        assertEquals(0, exception.getStackTrace().length);
        final Mqtt5SubAckException thrownException =
                assertThrows(Mqtt5SubAckException.class, () -> { throw exception; });
        assertEquals(0, thrownException.getStackTrace().length);
    }

    @Test
    void fillInStackTrace_newStackTrace() {
        final Mqtt5SubAck subAck = mock(Mqtt5SubAck.class);
        final Mqtt5SubAckException exception = new Mqtt5SubAckException(subAck, "message");
        assertEquals(0, exception.getStackTrace().length);
        final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
        assertTrue(filledException instanceof Mqtt5SubAckException);
        assertTrue(filledException.getStackTrace().length > 0);
        assertEquals("fillInStackTrace_newStackTrace", filledException.getStackTrace()[0].getMethodName());
    }
}
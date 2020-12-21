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

package com.hivemq.client2.mqtt.mqtt5.exceptions;

import com.hivemq.client2.internal.util.AsyncRuntimeException;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubAck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Silvio Giebl
 */
class Mqtt5PubAckExceptionTest {

    @Test
    void constructor() {
        final Mqtt5PubAck pubAck = mock(Mqtt5PubAck.class);
        final Mqtt5PubAckException exception = new Mqtt5PubAckException(pubAck, "message");
        assertEquals("message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(pubAck, exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void noStackTrace() {
        final Mqtt5PubAck pubAck = mock(Mqtt5PubAck.class);
        final Mqtt5PubAckException exception = new Mqtt5PubAckException(pubAck, "message");
        assertEquals(0, exception.getStackTrace().length);
        final Mqtt5PubAckException thrownException =
                assertThrows(Mqtt5PubAckException.class, () -> { throw exception; });
        assertEquals(0, thrownException.getStackTrace().length);
    }

    @Test
    void fillInStackTrace_newStackTrace() {
        final Mqtt5PubAck pubAck = mock(Mqtt5PubAck.class);
        final Mqtt5PubAckException exception = new Mqtt5PubAckException(pubAck, "message");
        assertEquals(0, exception.getStackTrace().length);
        final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
        assertTrue(filledException instanceof Mqtt5PubAckException);
        assertTrue(filledException.getStackTrace().length > 0);
        assertEquals("fillInStackTrace_newStackTrace", filledException.getStackTrace()[0].getMethodName());
    }
}
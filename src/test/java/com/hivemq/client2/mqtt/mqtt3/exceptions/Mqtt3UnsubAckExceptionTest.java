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

package com.hivemq.client2.mqtt.mqtt3.exceptions;

import com.hivemq.client2.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client2.internal.util.AsyncRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class Mqtt3UnsubAckExceptionTest {

    @Test
    void constructor() {
        final Mqtt3UnsubAckException exception = new Mqtt3UnsubAckException(Mqtt3UnsubAckView.INSTANCE, null, null);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
        assertNotNull(exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void constructor_message() {
        final Mqtt3UnsubAckException exception =
                new Mqtt3UnsubAckException(Mqtt3UnsubAckView.INSTANCE, "message", null);
        assertEquals("message", exception.getMessage());
        assertNull(exception.getCause());
        assertNotNull(exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void constructor_cause() {
        final RuntimeException cause = new RuntimeException("cause");
        final Mqtt3UnsubAckException exception = new Mqtt3UnsubAckException(Mqtt3UnsubAckView.INSTANCE, null, cause);
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void constructor_message_cause() {
        final RuntimeException cause = new RuntimeException("cause");
        final Mqtt3UnsubAckException exception =
                new Mqtt3UnsubAckException(Mqtt3UnsubAckView.INSTANCE, "message", cause);
        assertEquals("message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void noStackTrace() {
        final RuntimeException cause = new RuntimeException("cause");
        final Mqtt3UnsubAckException exception =
                new Mqtt3UnsubAckException(Mqtt3UnsubAckView.INSTANCE, "message", cause);
        assertEquals(0, exception.getStackTrace().length);
        final Mqtt3UnsubAckException thrownException =
                assertThrows(Mqtt3UnsubAckException.class, () -> { throw exception; });
        assertEquals(0, thrownException.getStackTrace().length);
    }

    @Test
    void fillInStackTrace_newStackTrace() {
        final RuntimeException cause = new RuntimeException("cause");
        final Mqtt3UnsubAckException exception =
                new Mqtt3UnsubAckException(Mqtt3UnsubAckView.INSTANCE, "message", cause);
        assertEquals(0, exception.getStackTrace().length);
        final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
        assertTrue(filledException instanceof Mqtt3UnsubAckException);
        assertTrue(filledException.getStackTrace().length > 0);
        assertEquals("fillInStackTrace_newStackTrace", filledException.getStackTrace()[0].getMethodName());
    }
}